package uk.nhs.adaptors.pss.translator.task;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.dao.PatientMigrationRequestDao;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.model.InboundMessage;
import uk.nhs.adaptors.pss.translator.service.BundleMapperService;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class COPCMessageHandler {
    private final MigrationStatusLogService migrationStatusLogService;
    private final PatientMigrationRequestDao migrationRequestDao;
    private final FhirParser fhirParser;
    private final BundleMapperService bundleMapperService;
    private final ObjectMapper objectMapper;

    public void handleMessage(InboundMessage inboundMessage, String conversationId) throws JAXBException, JsonProcessingException {
        // This area is for the processing of large message parts which should come through in the attachments and
        // externalAttachments arrays, the only thing you will need to extract out are the CID and MID Ids from the XML.
    }

}
