package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllRequestStatements;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralCategory;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.hl7.v3.CD;
import org.hl7.v3.CR;
import org.hl7.v3.CV;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.hl7.v3.RCMRMT030101UK04ResponsibleParty3;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@AllArgsConstructor
public class ReferralRequestMapper extends AbstractMapper<ReferralRequest> {
    private static final String META_PROFILE = "ReferralRequest-1";
    private static final String PRIORITY_PREFIX = "Priority: ";
    private static final String ACTION_DATE_PREFIX = "Action Date: ";
    private static final String PRACTITIONER_REFERENCE = "Practitioner/%s";
    private static final String RESP_PARTY_TYPE_CODE = "RESP";
    private static final String SELF_REFERRAL = "SelfReferral";

    private static Map<String, String> priorityCodes = Map.of(
            "394848005", "routine",
            "394849002", "urgent",
            "88694003", "stat"
    );

    private CodeableConceptMapper codeableConceptMapper;
    private ObservationMapper observationMapper;

    public List<ReferralRequest> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {

        return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllRequestStatements(component)
                .filter(Objects::nonNull)
                .filter(this::isNotSelfReferral)
                .map(requestStatement -> mapToReferralRequest(composition, requestStatement, patient, encounters, practiseCode)))
            .toList();
    }

    public ReferralRequest mapToReferralRequest(RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04RequestStatement requestStatement, Patient patient, List<Encounter> encounters, String practiseCode) {
        var referralRequest = new ReferralRequest();
        var id = requestStatement.getId().get(0).getRoot();

        referralRequest.setId(id);
        referralRequest.setMeta(generateMeta(META_PROFILE));
        referralRequest.getIdentifier().add(buildIdentifier(id, practiseCode));
        referralRequest.setStatus(ReferralRequestStatus.UNKNOWN);
        referralRequest.setIntent(ReferralCategory.ORDER);
        referralRequest.getRequester().setAgent(ParticipantReferenceUtil.getParticipantReference(requestStatement.getParticipant(),
            ehrComposition));
        referralRequest.setAuthoredOnElement(getAuthoredOn(requestStatement.getAvailabilityTime()));
        referralRequest.setNote(getNotes(requestStatement));
        referralRequest.setSubject(new Reference(patient));

        setReferralRequestContext(referralRequest, ehrComposition, encounters);
        setReferralRequestRecipient(referralRequest, requestStatement.getResponsibleParty());
        setReferralRequestReasonCode(referralRequest, requestStatement.getCode());

        var referralPriority =
                new ReferralRequest
                .ReferralPriorityEnumFactory()
                .fromCode(
                        getPriorityCodeFromEhrComposition(ehrComposition)
                );

        referralRequest.setPriority(referralPriority);
        
        return referralRequest;
    }

    private void setReferralRequestReasonCode(ReferralRequest referralRequest, CD code) {
        if (code != null) {
            var reasonCode = codeableConceptMapper.mapToCodeableConcept(code);
            if (!reasonCode.hasCoding()) {
                reasonCode.addCoding(DegradedCodeableConcepts.DEGRADED_REFERRAL);
            }
            referralRequest.getReasonCode().add(reasonCode);
        }
    }

    private void setReferralRequestRecipient(ReferralRequest referralRequest, RCMRMT030101UK04ResponsibleParty3 responsibleParty) {
        if (responsiblePartyAgentRefHasIdValue(responsibleParty)) {
            referralRequest.getRecipient().add(new Reference(PRACTITIONER_REFERENCE.formatted(
                responsibleParty.getAgentRef().getId().getRoot())));
        }
    }

    private void setReferralRequestContext(ReferralRequest referralRequest, RCMRMT030101UK04EhrComposition ehrComposition,
        List<Encounter> encounters) {

        encounters
            .stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .findFirst()
            .map(Reference::new)
            .ifPresent(referralRequest::setContext);
    }

    private DateTimeType getAuthoredOn(TS availabilityTime) {
        if (availabilityTime != null && availabilityTime.hasValue()) {
            return DateFormatUtil.parseToDateTimeType(availabilityTime.getValue());
        }
        return null;
    }

    private boolean responsiblePartyAgentRefHasIdValue(RCMRMT030101UK04ResponsibleParty3 responsibleParty) {
        return responsibleParty != null
            && responsibleParty.getTypeCode().stream().anyMatch(RESP_PARTY_TYPE_CODE::equals)
            && responsibleParty.getAgentRef() != null
            && responsibleParty.getAgentRef().getId() != null;
    }

    private boolean isNotSelfReferral(RCMRMT030101UK04RequestStatement requestStatement) {
        for (CR qualifier : requestStatement.getCode().getQualifier()) {
            if (qualifier.getValue().getCode().equals(SELF_REFERRAL)) {
                return false;
            }
        }
        return true;
    }

    private List<Annotation> getNotes(RCMRMT030101UK04RequestStatement requestStatement) {
        var priority = getPriorityText(requestStatement.getPriorityCode());
        var actionDate = getActionDateText(requestStatement.getEffectiveTime());
        var text = requestStatement.getText();

        var notes = new ArrayList<Annotation>();

        if (StringUtils.isNotEmpty(priority)) {
            notes.add(new Annotation().setText(priority));
        }

        if (StringUtils.isNotEmpty(actionDate)) {
            notes.add(new Annotation().setText(actionDate));
        }

        if (StringUtils.isNotEmpty(text)) {
            notes.add(new Annotation().setText(text));
        }

        return notes;
    }

    private String getPriorityText(CV priorityCode) {
        if (priorityCode != null) {
            if (StringUtils.isNotEmpty(priorityCode.getOriginalText())) {
                return PRIORITY_PREFIX + priorityCode.getOriginalText();
            } else if (StringUtils.isNotEmpty(priorityCode.getDisplayName())) {
                return PRIORITY_PREFIX + priorityCode.getDisplayName();
            }
        }
        return StringUtils.EMPTY;
    }

    private String getActionDateText(IVLTS effectiveTime) {
        if (hasEffectiveTimeValue(effectiveTime)) {
            return ACTION_DATE_PREFIX + DateFormatUtil.parseToDateTimeType(effectiveTime.getCenter().getValue()).asStringValue();
        }

        return StringUtils.EMPTY;
    }

    private boolean hasEffectiveTimeValue(IVLTS effectiveTime) {
        return effectiveTime != null && effectiveTime.getCenter() != null && effectiveTime.getCenter().getValue() != null;
    }

    private String getPriorityCodeFromEhrComposition(RCMRMT030101UK04EhrComposition ehrComposition) {

        String priorityCode = null;

        boolean isComponentNotNullInEhrComposition = ehrComposition != null
                && ehrComposition.getComponent() != null
                && !(ehrComposition.getComponent().isEmpty());

        boolean componentIsNotNull = isComponentNotNullInEhrComposition
                && ehrComposition.getComponent().get(0).getCompoundStatement() != null
                && ehrComposition.getComponent().get(0).getCompoundStatement().getComponent() != null;

        if (isComponentNotNullInEhrComposition) {

            var priorityCodeList = ehrComposition
                    .getComponent()
                    .stream()
                    .filter(component4 ->
                            component4.getRequestStatement() != null
                            && component4.getRequestStatement().getPriorityCode() != null
                            && component4.getRequestStatement().getPriorityCode().getCode() != null
                    )
                    .map(component4 -> component4.getRequestStatement().getPriorityCode().getCode())
                    .toList();

            if ((!priorityCodeList.isEmpty()) && StringUtils.isNotEmpty(priorityCodeList.get(0))) {
                priorityCode = priorityCodeList.get(0);
            } else {

                if (componentIsNotNull) {
                    /*
                        if there is no priority code in the Top Component.
                        we pass the child component to a recursive function,
                        so it finds the priority code on each child inside the children
                     */
                    priorityCode = getPriorityCode(
                            ehrComposition
                            .getComponent()
                            .get(0).getCompoundStatement()
                            .getComponent()
                    );
                }
            }
        }

        if (priorityCode == null) {
            return null;
        }

        if (priorityCodes.containsKey(priorityCode)) {
            return priorityCodes.get(priorityCode);
        }

        throw new IllegalArgumentException("Unknown ReferralPriority code '" + priorityCode + "'");
    }


    private String getPriorityCode(List<RCMRMT030101UK04Component02> component) {
        var priorityCodeList = component
                .stream()
                .filter(component02 -> component02.getRequestStatement() != null)
                .map(component02 -> component02.getRequestStatement().getPriorityCode().getCode())
                .toList();

        if (!(priorityCodeList.isEmpty()) &&  StringUtils.isNotEmpty(priorityCodeList.get(0))) {
            return priorityCodeList.get(0);
        }

        var componentList = component
                .stream()
                .filter(component02 -> component02.getCompoundStatement() != null)
                .filter(
                        component02 -> component02.getCompoundStatement().getComponent() != null
                        && !(component02.getCompoundStatement().getComponent().isEmpty())
                )
                .toList();

        if (!componentList.isEmpty()) {
            return getPriorityCode(componentList.get(0).getCompoundStatement().getComponent());
        } else {
            return null;
        }
    }
}