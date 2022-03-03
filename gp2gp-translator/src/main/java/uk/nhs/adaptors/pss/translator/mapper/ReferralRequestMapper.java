package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.ArrayList;
import java.util.List;

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
import org.hl7.v3.CV;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.hl7.v3.RCMRMT030101UK04ResponsibleParty3;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@AllArgsConstructor
public class ReferralRequestMapper {
    private static final String META_PROFILE = "ReferralRequest-1";
    private static final String PRIORITY_PREFIX = "Priority: ";
    private static final String ACTION_DATE_PREFIX = "Action Date: ";
    private static final String PRACTITIONER_REFERENCE = "Practitioner/%s";
    private static final String RESP_PARTY_TYPE_CODE = "RESP";

    private CodeableConceptMapper codeableConceptMapper;

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

        return referralRequest;
    }

    private void setReferralRequestReasonCode(ReferralRequest referralRequest, CD code) {
        if (code != null) {
            referralRequest.getReasonCode().add(codeableConceptMapper.mapToCodeableConcept(code));
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
            .ifPresent(encounter -> referralRequest.setContext(new Reference(encounter)));
    }

    private DateTimeType getAuthoredOn(TS availabilityTime) {
        if (availabilityTime != null) {
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
}
