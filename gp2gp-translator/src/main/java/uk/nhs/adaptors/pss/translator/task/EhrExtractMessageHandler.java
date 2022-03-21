package uk.nhs.adaptors.pss.translator.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.model.ACKMessageData;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;

import javax.xml.bind.JAXBException;

import static uk.nhs.adaptors.connector.model.MigrationStatus.*;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractMessageHandler {
    private final MigrationStatusLogService migrationStatusLogService;
    private final FhirParser fhirParser;
    private final BundleMapperService bundleMapperService;
    private final ObjectMapper objectMapper;
    private final SendACKMessageHandler sendACKMessageHandler;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, JsonProcessingException {
        RCMRIN030000UK06Message payload = unmarshallString(inboundMessage.getPayload(), RCMRIN030000UK06Message.class);
        migrationStatusLogService.addMigrationStatusLog(EHR_EXTRACT_RECEIVED, conversationId);

        var bundle = bundleMapperService.mapToBundle(payload);
        migrationStatusLogService.updatePatientMigrationRequestAndAddMigrationStatusLog(
            conversationId,
            fhirParser.encodeToJson(bundle),
            objectMapper.writeValueAsString(inboundMessage),
            EHR_EXTRACT_TRANSLATED
        );
    }

    boolean sendNackMessage(RCMRIN030000UK06Message payload, String conservationId) {
        migrationStatusLogService.addMigrationStatusLog(ERROR, conservationId);
        return sendACKMessageHandler.prepareAndSendMessage(prepareAckMessageData(
                false,
                payload,
                conservationId
                ));
    }

    boolean sendAckMessage(RCMRIN030000UK06Message payload, String conservationId) {
        // TODO - to be completed as part of NIAD-2035
        return sendACKMessageHandler.prepareAndSendMessage(prepareAckMessageData(
                true,
                payload,
                conservationId
        ));
    }

    private ACKMessageData prepareAckMessageData(boolean acknowledge, RCMRIN030000UK06Message payload, String conversationId) {
        String ackType = acknowledge ? "AA" : "AE";

        String toOdsCode = parseToOdsCode(payload);
        String messageRef = parseMessageRef(payload);
        String toAsid = parseToAsid(payload);
        String fromAsid = parseFromAsid(payload);

        return ACKMessageData.builder()
                .conversationId(conversationId)
                .ackType(ackType)
                .toOdsCode(toOdsCode)
                .messageRef(messageRef)
                .toAsid(toAsid)
                .fromAsid(fromAsid)
                .build();
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
}
