package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralCategory;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.CV;
import org.hl7.v3.II;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04AgentRef;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04Participant;
import org.hl7.v3.RCMRMT030101UK04Participant2;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.hl7.v3.RCMRMT030101UK04ResponsibleParty3;
import org.hl7.v3.TS;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@AllArgsConstructor
public class ReferralRequestMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ReferralRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String PRIORITY_PREFIX = "Priority: ";
    private static final String ACTION_DATE_PREFIX = "Action Date: ";
    private static final String PRACTITIONER_REFERENCE_PREFIX = "Practitioner/";
    private static final String RESP_PARTY_TYPE_CODE = "RESP";
    private static final String PRF_TYPE_CODE = "PRF";
    private static final String PPRF_TYPE_CODE = "PPRF";

    private CodeableConceptMapper codeableConceptMapper;

    public ReferralRequest mapToReferralRequest(RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04RequestStatement requestStatement) {
        var id = requestStatement.getId().get(0).getRoot();
        var identifier = getIdentifier(id);
        var notes = getNotes(requestStatement);
        var reasonCode = getReasonCode(requestStatement.getCode());
        var authoredOn = getAuthoredOn(requestStatement.getAvailabilityTime());
        var recipient = getRecipient(requestStatement.getResponsibleParty());
        var requester = ParticipantReferenceUtil.getParticipantReference(requestStatement.getParticipant(), ehrComposition, false);

        /**
         * TODO: Known future implementations to this mapper
         * - subject: references a global patient resource for the transaction (NIAD-2024)
         * - context: references an encounter resource if it has been generated from the ehrComposition (NIAD-2025)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        return createRequestStatement(id, identifier, notes, reasonCode, authoredOn, recipient, requester);
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
        return identifier;
    }

    private CodeableConcept getReasonCode(CD reasonCode) {
        if (reasonCode != null) {
            return codeableConceptMapper.mapToCodeableConcept(reasonCode);
        }
        return null;
    }

    private Date getAuthoredOn(TS availabilityTime) {
        if (availabilityTime != null) {
            return DateFormatUtil.parse(availabilityTime.getValue()).getValue();
        }
        return null;
    }

    private Reference getRecipient(RCMRMT030101UK04ResponsibleParty3 responsibleParty) {
        if (responsiblePartyAgentRefHasIdValue(responsibleParty)) {
            return new Reference(PRACTITIONER_REFERENCE_PREFIX + responsibleParty.getAgentRef().getId().getRoot());
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
            return ACTION_DATE_PREFIX + DateFormatUtil.parse(effectiveTime.getCenter().getValue()).asStringValue();
        }

        return StringUtils.EMPTY;
    }

    private boolean hasEffectiveTimeValue(IVLTS effectiveTime) {
        return effectiveTime != null && effectiveTime.getCenter() != null && effectiveTime.getCenter().getValue() != null;
    }

    private ReferralRequest createRequestStatement(String id, Identifier identifier, List<Annotation> notes, CodeableConcept reasonCode,
        Date authoredOn, Reference recipient, Reference requester) {
        var referralRequest = new ReferralRequest();

        referralRequest.setId(id);
        referralRequest.getMeta().getProfile().add(new UriType(META_PROFILE));
        referralRequest.getIdentifier().add(identifier);
        referralRequest.setStatus(ReferralRequestStatus.UNKNOWN);
        referralRequest.setIntent(ReferralCategory.ORDER);
        referralRequest.getRequester().setAgent(requester);
        referralRequest.setAuthoredOn(authoredOn);
        referralRequest.setNote(notes);
        referralRequest.getReasonCode().add(reasonCode);
        referralRequest.getRecipient().add(recipient);

        return referralRequest;
    }
}
