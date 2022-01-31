package uk.nhs.adaptors.pss.translator.mapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestStatus;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralCategory;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CV;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.hl7.v3.RCMRMT030101UK04ResponsibleParty3;
import org.hl7.v3.TS;

import uk.nhs.adaptors.pss.translator.utils.DateFormatUtil;

public class ReferralRequestMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProcedureRequest-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String PRIORITY_PREFIX = "Priority: ";
    private static final String ACTION_DATE_PREFIX = "Action Date: ";
    private static final String PRACTITIONER_REFERENCE_PREFIX = "Practitioner/";
    
    public ReferralRequest mapToReferralRequest(RCMRMT030101UK04RequestStatement requestStatement) {
        // TODO: id - DONE
        // TODO: meta - DONE
        // TODO: identifier - DONE
        // TODO: status - DONE
        // TODO: intent - DONE
        // TODO: priority - DONE
        // TODO: subject
        // TODO: context
        // TODO: authoredOn - DONE
        // TODO: note/action date - DONE
        // TODO: requester
        // TODO: recipient - DONE
        // TODO: reasonCode - DONE
        // TODO: note - DONE

        var id = requestStatement.getId().get(0).getRoot();
        var identifier = getIdentifier(id);
        var notes = getNotes(requestStatement);
        var reasonCode = new CodeableConceptMapper().mapToCodeableConcept(requestStatement.getCode());
        var authoredOn = getAuthoredOn(requestStatement.getAvailabilityTime());
        var recipient = getRecipient(requestStatement.getResponsibleParty());

        /**
         * TODO: Implement future referencing
         * - subject: references a global patient resource for the transaction
         * - context: references an encounter resource if it has been generated from the ehrComposition
         * - requester: references practitioner who administered the referral request
         */
        
         return createRequestStatement(id, identifier, notes, reasonCode, authoredOn, recipient);
    }
    
    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL
            .setValue(id);
        return identifier;
    }

    private Date getAuthoredOn(TS availabilityTime) {
        if (availabilityTime != null) {
            return DateFormatUtil.parse(availabilityTime.getValue()).getValue();
        }

        return null;
    }

    private Reference getRecipient(RCMRMT030101UK04ResponsibleParty3 responsibleParty) {
        if (responsibleParty != null
            && responsibleParty.getAgentRef() != null
            && responsibleParty.getAgentRef().getId() != null) {
            return new Reference(PRACTITIONER_REFERENCE_PREFIX +
                responsibleParty.getAgentRef().getId().getRoot());
        }
        return null;
    }

    private List<Annotation> getNotes(RCMRMT030101UK04RequestStatement requestStatement) {
        var priority = getPriorityText(requestStatement.getPriorityCode());
        var actionDate = getActionDateText(requestStatement.getEffectiveTime());
        var text = requestStatement.getText(); // TODO: line breaks?
        
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
        if (effectiveTime != null && effectiveTime.getCenter() != null && effectiveTime.getCenter().getValue() != null) {
            var actionDate = DateFormatUtil.parse(effectiveTime.getCenter().getValue()).getValue();
            var dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return ACTION_DATE_PREFIX + dateFormat.format(actionDate);
        }

        return StringUtils.EMPTY;
    }

    private ReferralRequest createRequestStatement(
        String id,
        Identifier identifier,
        List<Annotation> notes,
        CodeableConcept reasonCode,
        Date authoredOn,
        Reference recipient) {
        var referralRequest = new ReferralRequest();

        referralRequest.setId(id);
        referralRequest.getMeta().getProfile().add(new UriType(META_PROFILE));
        referralRequest.getIdentifier().add(identifier);
        referralRequest.setStatus(ReferralRequestStatus.UNKNOWN);
        referralRequest.setIntent(ReferralCategory.ORDER); //todo: is this right?????
        
        if (authoredOn != null){
            referralRequest.setAuthoredOn(authoredOn);
        }
        
        if (!notes.isEmpty()) {
            referralRequest.setNote(notes);
        }
        
        if (reasonCode != null) {
            referralRequest.getReasonCode().add(reasonCode);
        }
        
        if(recipient != null) {
            referralRequest.getRecipient().add(recipient);
        }

        return referralRequest;
    }
}
