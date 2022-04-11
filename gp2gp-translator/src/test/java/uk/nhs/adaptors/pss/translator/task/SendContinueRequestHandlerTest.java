package uk.nhs.adaptors.pss.translator.task;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.nhs.adaptors.connector.model.MigrationStatus;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.ContinueRequestService;
import uk.nhs.adaptors.pss.translator.service.MhsClientService;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class SendContinueRequestHandlerTest {
    private static final String NHS_NUMBER = "9446363101";
    private static final String CONVERSATION_ID = "6E242658-3D8E-11E3-A7DC-172BDA00FA67";
    private static final String LOOSING_ODS_CODE = "B83002"; //to odds code
    private static final String WINNING_ODS_CODE = "C81007"; //from odds code
    private static final String TO_ASID = "715373337545";
    private static final String FROM_ASID = "276827251543";
    private static final String MCCI_IN010000UK13_CREATIONTIME = "20220407194614";

    @Mock
    private MhsRequestBuilder requestBuilder;

    @Mock
    private ContinueRequestService continueRequestService;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private MhsClientService mhsClientService;

    @Mock
    private ContinueRequestData data;

    @InjectMocks
    private SendContinueRequestHandler sendContinueRequestHandler;

    @Test
    public void When_PrepareAndSendRequest_ToMhsAndGetError_Expect_ThrowError() {

        ContinueRequestData continueRequestData = ContinueRequestData.builder()
                .conversationId(CONVERSATION_ID)
                .fromAsid(FROM_ASID)
                .toAsid(TO_ASID)
                .nhsNumber(NHS_NUMBER)
                .fromOdsCode(WINNING_ODS_CODE)
                .toOdsCode(LOOSING_ODS_CODE)
                .mcciIN010000UK13creationTime(MCCI_IN010000UK13_CREATIONTIME)
                .build();

        when(mhsClientService.send(any())).thenThrow(WebClientResponseException.class);

        assertThrows(WebClientResponseException.class, () -> {
            sendContinueRequestHandler.prepareAndSendRequest(continueRequestData);
        });
    }

    @Test
    public void When_MHSClientService_SendThrowsErrors_Expect_MigrationStatusLogAddStatusIsCalledWithContinueRequestError()
            throws WebClientResponseException {

        ContinueRequestData continueRequestData = ContinueRequestData.builder()
                .conversationId(CONVERSATION_ID)
                .fromAsid(FROM_ASID)
                .toAsid(TO_ASID)
                .nhsNumber(NHS_NUMBER)
                .fromOdsCode(WINNING_ODS_CODE)
                .toOdsCode(LOOSING_ODS_CODE)
                .mcciIN010000UK13creationTime(MCCI_IN010000UK13_CREATIONTIME)
                .build();

        when(mhsClientService.send(any())).thenThrow(WebClientResponseException.class);

        try {
            sendContinueRequestHandler.prepareAndSendRequest(continueRequestData);
        } catch (Exception e) {
        }

        verify(migrationStatusLogService).addMigrationStatusLog(MigrationStatus.CONTINUE_REQUEST_ERROR, CONVERSATION_ID);
    }

    @Test
    public void When_PrepareAndSendRequest_IsCalled_Expect_NoErrors() {

        ContinueRequestData continueRequestData = ContinueRequestData
                .builder()
                .conversationId(CONVERSATION_ID)
                .fromAsid(FROM_ASID)
                .toAsid(TO_ASID)
                .nhsNumber(NHS_NUMBER)
                .fromOdsCode(WINNING_ODS_CODE)
                .toOdsCode(LOOSING_ODS_CODE)
                .mcciIN010000UK13creationTime(MCCI_IN010000UK13_CREATIONTIME)
                .build();

        sendContinueRequestHandler.prepareAndSendRequest(continueRequestData);
    }

    @Test
    public void When_ParametersCorrect_Expect_MHSClientServiceSendIsCalled() {

        ContinueRequestData continueRequestData = ContinueRequestData
                .builder()
                .conversationId(CONVERSATION_ID)
                .fromAsid(FROM_ASID)
                .toAsid(TO_ASID)
                .nhsNumber(NHS_NUMBER)
                .fromOdsCode(WINNING_ODS_CODE)
                .toOdsCode(LOOSING_ODS_CODE)
                .mcciIN010000UK13creationTime(MCCI_IN010000UK13_CREATIONTIME)
                .build();

        sendContinueRequestHandler.prepareAndSendRequest(continueRequestData);
        verify(mhsClientService).send(any());
    }

    @Test
    public void When_ParametersCorrect_Expect_MigrationStatusLogServiceAddMigrationStatusLogIsCalled() {

        ContinueRequestData continueRequestData = ContinueRequestData
                .builder()
                .conversationId(CONVERSATION_ID)
                .fromAsid(FROM_ASID)
                .toAsid(TO_ASID)
                .nhsNumber(NHS_NUMBER)
                .fromOdsCode(WINNING_ODS_CODE)
                .toOdsCode(LOOSING_ODS_CODE)
                .mcciIN010000UK13creationTime(MCCI_IN010000UK13_CREATIONTIME)
                .build();

        sendContinueRequestHandler.prepareAndSendRequest(continueRequestData);
        verify(migrationStatusLogService).addMigrationStatusLog(MigrationStatus.CONTINUE_REQUEST_ACCEPTED, CONVERSATION_ID);
    }
}
