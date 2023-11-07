package uk.nhs.adaptors.pss.gpc.controller.header;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpHeaders {

    public static final String TO_ASID = "to-asid";
    public static final String FROM_ASID = "from-asid";
    public static final String TO_ODS = "to-ods";
    public static final String FROM_ODS = "from-ods";
    public static final String CONVERSATION_ID = "conversationId";
    public static final String CONFIRMATION_RESPONSE = "confirmationResponse";

}
