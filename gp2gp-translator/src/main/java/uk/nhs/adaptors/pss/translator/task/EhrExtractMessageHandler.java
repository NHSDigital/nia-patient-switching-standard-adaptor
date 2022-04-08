package uk.nhs.adaptors.pss.translator.task;

import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_RECEIVED;
import static uk.nhs.adaptors.connector.model.MigrationStatus.EHR_EXTRACT_TRANSLATED;
import static uk.nhs.adaptors.pss.translator.model.NACKReason.EHR_EXTRACT_CANNOT_BE_PROCESSED;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import javax.xml.bind.JAXBException;

import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.model.NACKMessageData;
import uk.nhs.adaptors.pss.translator.model.NACKReason;
import uk.nhs.adaptors.pss.translator.service.AttachmentHandlerService;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractMessageHandler {
    private final MigrationStatusLogService migrationStatusLogService;
    private final FhirParser fhirParser;
    private final BundleMapperService bundleMapperService;
    private final ObjectMapper objectMapper;
    private final AttachmentHandlerService attachmentHandlerService;
    private final SendNACKMessageHandler sendNACKMessageHandler;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, JsonProcessingException {

        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);

        try {
            var bundle = bundleMapperService.mapToBundle(payload);
            attachmentHandlerService.storeAttachments(inboundMessage.getAttachments(), conversationId);
            migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
                conversationId,
                fhirParser.encodeToJson(bundle),
                objectMapper.writeValueAsString(inboundMessage),
                EHR_EXTRACT_TRANSLATED
            );
        } catch (JsonProcessingException ex) {
            sendNackMessage(EHR_EXTRACT_CANNOT_BE_PROCESSED, payload, conversationId);
            throw ex;
        }
    }

    public boolean sendNackMessage(NACKReason reason, RCMRIN030000UK06Message payload, String conversationId) {

        LOGGER.debug("Sending NACK message with acknowledgement code [{}]", reason.getCode());

        migrationStatusLogService.addMigrationStatusLog(reason.getMigrationStatus(), conversationId);

        return sendNACKMessageHandler.prepareAndSendMessage(prepareNackMessageData(
            reason,
            payload,
            conversationId
        ));
    }

    private NACKMessageData prepareNackMessageData(NACKReason reason, RCMRIN030000UK06Message payload,
        String conversationId) {

        String toOdsCode = parseToOdsCode(payload);
        String messageRef = parseMessageRef(payload);
        String toAsid = parseToAsid(payload);
        String fromAsid = parseFromAsid(payload);
        String nackCode = reason.getCode();

        return NACKMessageData.builder()
            .conversationId(conversationId)
            .nackCode(nackCode)
            .toOdsCode(toOdsCode)
            .messageRef(messageRef)
            .toAsid(toAsid)
            .fromAsid(fromAsid)
            .build();
    }

    private String parseToOdsCode(RCMRIN030000UK06Message payload) {
        return payload.getControlActEvent()
            .getSubject()
            .getEhrExtract()
            .getAuthor()
            .getAgentOrgSDS()
            .getAgentOrganizationSDS()
            .getId()
            .getExtension();
    }

    private String parseMessageRef(RCMRIN030000UK06Message payload) {
        return payload.getId().getRoot();
    }

    private String parseToAsid(RCMRIN030000UK06Message payload) {
        return payload.getCommunicationFunctionSnd()
            .getDevice()
            .getId()
            .get(0)
            .getExtension();
    }

    private String parseFromAsid(RCMRIN030000UK06Message payload) {
        return payload.getCommunicationFunctionRcv()
            .get(0)
            .getDevice()
            .getId()
            .get(0)
            .getExtension();
    }

    //TODO: this method is related to the large messaging epic and should be called after saving translated Boundle resource.
    //  Can be used during implementation of NIAD-2045
    private boolean sendContinueRequest(RCMRIN030000UK06Message payload, String conversationId, String patientNhsNumber) {
        // TODO: Should call
        //  sendContinueRequestHandler.prepareAndSendRequest(prepareContinueRequestData(payload, conversationId, patientNhsNumber));
        return true;
    }

    //TODO: this method is only used inside sendContinueRequest() method above
    private ContinueRequestData prepareContinueRequestData(
        RCMRIN030000UK06Message payload, String conversationId, String patientNhsNumber) {

        var fromAsid = parseFromAsid(payload);
        var toAsid = parseToAsid(payload);
        var toOdsCode = parseToOdsCode(payload);

        return ContinueRequestData.builder()
            .conversationId(conversationId)
            .nhsNumber(patientNhsNumber)
            .fromAsid(fromAsid)
            .toAsid(toAsid)
            .toOdsCode(toOdsCode)
            .build();
    }
}
