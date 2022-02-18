package uk.nhs.adaptors.common.service;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MDCService {
    public static final String MDC_CONVERSATION_ID_KEY = "ConversationId";

    public void applyConversationId(String id) {
        MDC.put(MDC_CONVERSATION_ID_KEY, id);
    }

    public String getConversationId() {
        return MDC.get(MDC_CONVERSATION_ID_KEY);
    }

    public void resetAllMdcKeys() {
        MDC.clear();
    }
}
