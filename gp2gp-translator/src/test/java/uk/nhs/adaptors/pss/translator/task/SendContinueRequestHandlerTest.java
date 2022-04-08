package uk.nhs.adaptors.pss.translator.task;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MigrationStatusLogService;
import uk.nhs.adaptors.pss.translator.mhs.MhsRequestBuilder;
import uk.nhs.adaptors.pss.translator.model.ContinueRequestData;
import uk.nhs.adaptors.pss.translator.service.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class SendContinueRequestHandlerTest {
    private static final String NHS_NUMBER = "9446363101";
    private static final String CONVERSATION_ID = "6E242658-3D8E-11E3-A7DC-172BDA00FA67";
    private static final String LOOSING_ODS_CODE = "B83002"; //to odds code
    private static final String WINNING_ODS_CODE = "C81007"; //from odds code
    private static final String TO_ASID = "715373337545";
    private static final String FROM_ASID = "276827251543";
    private static final String MCCI_IN010000UK13creationTime = "20220407194614";



    @Mock
    private MhsRequestBuilder requestBuilder;

    @Mock
    private ContinueRequestService continueRequestService;

    @Mock
    private MigrationStatusLogService migrationStatusLogService;

    @Mock
    private MhsClientService mhsClientService;

    @InjectMocks
    private SendContinueRequestHandler sendContinueRequestHandler;


    //////////////////////////////////////////////////////////////


    //When_SendNackMessage_WithValidParameters_Expect_ShouldParseMessageDataCorrectly  //need to change
    //test error is thrown
    @Test
    public void When_PrepareAndSendRequest_ToMhsAndGetError_Expect_ThrowError() {

        ContinueRequestData continueRequestData = ContinueRequestData.builder()
                .conversationId(CONVERSATION_ID)
                .fromAsid(FROM_ASID)
                .toAsid(TO_ASID)
                .nhsNumber(NHS_NUMBER)
                .fromOdsCode(WINNING_ODS_CODE)
                .toOdsCode(LOOSING_ODS_CODE)
                .mcciIN010000UK13creationTime(MCCI_IN010000UK13creationTime)
                .build();


        ///sendContinueRequestHandler.prepareAndSendRequest();
        PatientMigrationRequest migrationRequest =
                PatientMigrationRequest.builder()
                        .loosingPracticeOdsCode(LOOSING_ODS_CODE)
                        .winningPracticeOdsCode(WINNING_ODS_CODE)
                        .build();

        assertThrows(WebClientResponseException.class, () -> {
            sendContinueRequestHandler.prepareAndSendRequest(continueRequestData);
        });
    }

    //test parameters
    @Test
    public void PrepareAndSendRequest_WhenParametersCorrect_ExpectNoErrors (){

        ContinueRequestData continueRequestData = ContinueRequestData
                .builder()
                .conversationId(CONVERSATION_ID)
                .fromAsid(FROM_ASID)
                .toAsid(TO_ASID)
                .nhsNumber(NHS_NUMBER)
                .fromOdsCode(WINNING_ODS_CODE)
                .toOdsCode(LOOSING_ODS_CODE)
                .mcciIN010000UK13creationTime(MCCI_IN010000UK13creationTime)
                .build();


        //verify(LOGGER).info("Got response from MHS - 202 Accepted");;
        //assert last line of method /////////////////////////////////
    }

    //test parameters
    @Test
    public void PrepareAndSendRequest_WhenParametersIncorrect_ExpectErrors (){


    }




}
