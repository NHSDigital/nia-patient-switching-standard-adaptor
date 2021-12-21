package uk.nhs.adaptors.pss.gpc.utils;

import java.util.Collections;

import javax.jms.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class QueueUtils {
    @Qualifier("jmsTemplatePssQueue")
    private final JmsTemplate pssJmsTemplate;

    public Message receiveMessage() {
        return pssJmsTemplate.receive();
    }

    public int countPendingMessages() {
        Integer totalPendingMessages = pssJmsTemplate.browse((session, browser) -> Collections.list(browser.getEnumeration()).size());
        return totalPendingMessages == null ? 0 : totalPendingMessages;
    }
}
