package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.ReferralRequest.ReferralPriority;
import static org.hl7.fhir.dstu3.model.ReferralRequest.ReferralPriorityEnumFactory;
import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllRequestStatements;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.ArrayList;
import java.util.Collections;
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
import org.hl7.v3.RCMRMT030101UK04Component4;
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
    private static final Map<String, String> PRIORITY_CODES = Map.of(
            "394848005", "routine",
            "394849002", "urgent",
            "88694003", "asap"
    );

    private CodeableConceptMapper codeableConceptMapper;

    public List<ReferralRequest> mapResources(RCMRMT030101UK04EhrExtract ehrExtract,
                                              Patient patient,
                                              List<Encounter> encounters,
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
        var identifier = buildIdentifier(id, practiseCode);
        var agent = ParticipantReferenceUtil.getParticipantReference(requestStatement.getParticipant(), ehrComposition);
        var authoredOn = getAuthoredOn(requestStatement.getAvailabilityTime());
        var referralPriority = getReferralPriorityFromEhrComposition(ehrComposition);

        referralRequest.setId(id);
        referralRequest.setMeta(generateMeta(META_PROFILE));
        referralRequest.getIdentifier().add(identifier);
        referralRequest.setStatus(ReferralRequestStatus.UNKNOWN);
        referralRequest.setIntent(ReferralCategory.ORDER);
        referralRequest.getRequester().setAgent(agent);
        referralRequest.setAuthoredOnElement(authoredOn);
        referralRequest.setNote(getNotes(requestStatement));
        referralRequest.setSubject(new Reference(patient));
        referralRequest.setPriority(referralPriority);

        setReferralRequestContext(referralRequest, ehrComposition, encounters);
        setReferralRequestRecipient(referralRequest, requestStatement.getResponsibleParty());
        setReferralRequestReasonCode(referralRequest, requestStatement.getCode());

        return referralRequest;
    }

    private void setReferralRequestContext(ReferralRequest referralRequest,
                                           RCMRMT030101UK04EhrComposition ehrComposition,
                                           List<Encounter> encounters) {

        encounters
                .stream()
                .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
                .findFirst()
                .map(Reference::new)
                .ifPresent(referralRequest::setContext);
    }
    private void setReferralRequestRecipient(ReferralRequest referralRequest, RCMRMT030101UK04ResponsibleParty3 responsibleParty) {
        if (!hasIdValue(responsibleParty)) {
            return;
        }

        var agentRefRoot = responsibleParty.getAgentRef().getId().getRoot();
        var recipient = new Reference(PRACTITIONER_REFERENCE.formatted(agentRefRoot));
        referralRequest.getRecipient().add(recipient);
    }

    private void setReferralRequestReasonCode(ReferralRequest referralRequest, CD code) {
        if (code == null) {
            return;
        }

        var reasonCode = codeableConceptMapper.mapToCodeableConcept(code);
        DegradedCodeableConcepts.addDegradedEntryIfRequired(reasonCode, DegradedCodeableConcepts.DEGRADED_REFERRAL);
        referralRequest.getReasonCode().add(reasonCode);
    }

    private DateTimeType getAuthoredOn(TS availabilityTime) {
        if (availabilityTime != null && availabilityTime.hasValue()) {
            return DateFormatUtil.parseToDateTimeType(availabilityTime.getValue());
        }
        return null;
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
        if (!hasEffectiveTimeValue(effectiveTime)) {
            return StringUtils.EMPTY;
        }

        var effectiveTimeValue = DateFormatUtil.parseToDateTimeType(effectiveTime.getCenter().getValue());
        return ACTION_DATE_PREFIX + effectiveTimeValue.asStringValue();
    }

    private ReferralPriority getReferralPriorityFromEhrComposition(RCMRMT030101UK04EhrComposition ehrComposition) {

        if (ehrComposition == null || ehrComposition.getComponent().isEmpty()) {
            return null;
        }

        var topLevelComponents = ehrComposition.getComponent();

        var priorityCode = getPriorityCodesFromTopLevelComponents(topLevelComponents);

        if (priorityCode == null) {
            priorityCode = getReferralPriorityCodeFromChildComponents(topLevelComponents.get(0));
        }

        return new ReferralPriorityEnumFactory()
                .fromCode(priorityCode);
    }

    private String getPriorityCodesFromTopLevelComponents(List<RCMRMT030101UK04Component4> components) {
        return components
                .stream()
                .filter(ReferralRequestMapper::hasPriorityCode)
                .map(component4 -> component4.getRequestStatement().getPriorityCode().getCode())
                .findFirst()
                .map(this::getReferralPriorityCode)
                .orElse(null);
    }

    private String getReferralPriorityCode(String priorityCode) {
        if (PRIORITY_CODES.containsKey(priorityCode)) {
            return PRIORITY_CODES.get(priorityCode);
        }
        throw new IllegalArgumentException("Unknown ReferralPriority code '" + priorityCode + "'");
    }

    private String getReferralPriorityCodeFromChildComponents(RCMRMT030101UK04Component4 topComponent) {
        if (!topComponent.hasCompoundStatement()) {
            return null;
        }

        var childComponents = topComponent.getCompoundStatement().getComponent();

        if (childComponents == null) {
            return null;
        }

        // We pass the child component to a recursive function, so it finds the priority code for each nested child
        var priorityCode = getPriorityCode(childComponents);
        return getReferralPriorityCode(priorityCode);
    }

    private String getPriorityCode(List<RCMRMT030101UK04Component02> components) {

        var priorityCode = components
                .stream()
                .map(RCMRMT030101UK04Component02::getRequestStatement)
                .filter(Objects::nonNull)
                .map(RCMRMT030101UK04RequestStatement::getPriorityCode)
                .filter(Objects::nonNull)
                .map(CD::getCode)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null);

        if (priorityCode != null) {
            return priorityCode;
        }

        var childComponents = components
                .stream()
                .filter(ReferralRequestMapper::hasComponentInCompoundStatement)
                .map(c -> c.getCompoundStatement().getComponent())
                .filter(c -> !c.isEmpty())
                .findFirst()
                .orElse(Collections.emptyList());

        return childComponents.isEmpty()
                ? null
                : getPriorityCode(childComponents);
    }

    private boolean hasIdValue(RCMRMT030101UK04ResponsibleParty3 responsibleParty) {
        return responsibleParty != null
                && responsibleParty.getTypeCode().stream().anyMatch(RESP_PARTY_TYPE_CODE::equals)
                && responsibleParty.getAgentRef() != null
                && responsibleParty.getAgentRef().getId() != null;
    }

    private boolean hasEffectiveTimeValue(IVLTS effectiveTime) {
        return effectiveTime != null
                && effectiveTime.getCenter() != null
                && effectiveTime.getCenter().getValue() != null;
    }

    private static boolean hasPriorityCode(RCMRMT030101UK04Component4 component) {
        return component.getRequestStatement() != null
                && component.getRequestStatement().getPriorityCode() != null
                && component.getRequestStatement().getPriorityCode().getCode() != null;
    }

    private static boolean hasComponentInCompoundStatement(RCMRMT030101UK04Component02 component) {
        return component.hasCompoundStatement()
                && component.getCompoundStatement().getComponent() != null
                && !component.getCompoundStatement().getComponent().isEmpty();
    }

    private boolean isNotSelfReferral(RCMRMT030101UK04RequestStatement requestStatement) {
        for (CR qualifier : requestStatement.getCode().getQualifier()) {
            if (qualifier.getValue().getCode().equals(SELF_REFERRAL)) {
                return false;
            }
        }
        return true;
    }
}