package uk.nhs.adaptors.pss.translator.amqp;

import static java.nio.charset.StandardCharsets.UTF_8;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.apache.commons.codec.binary.Base64;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.springframework.stereotype.Service;

@Service
public class JmsReader {
    public String readMessage(Message message) throws JMSException {
        if (message instanceof JmsTextMessage jmsTextMessage) {
            return readTextMessage(jmsTextMessage);
        }
        if (message instanceof JmsBytesMessage jmsBytesMessage) {
            return readBytesMessage(jmsBytesMessage);
        }
        if (message != null) {
            return message.getBody(String.class);
        }
        return null;
    }

    private String readBytesMessage(JmsBytesMessage message) throws JMSException {
        byte[] bytes = new byte[(int) message.getBodyLength()];
        message.readBytes(bytes);
        return Base64.isBase64(bytes) ? new String(Base64.decodeBase64(bytes), UTF_8) : new String(bytes, UTF_8);
    }

    private String readTextMessage(JmsTextMessage message) throws JMSException {
        var text = message.getText();
        return Base64.isBase64(text) ? new String(Base64.decodeBase64(text), UTF_8) : text;
    }
}
