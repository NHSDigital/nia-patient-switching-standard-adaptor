package uk.nhs.adaptors.pss.translator.amqp;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.codec.binary.Base64;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

public class JmsReader {

    public static String readMessage(Message message) throws JMSException {
        if (message instanceof JmsTextMessage) {
            return readTextMessage((JmsTextMessage) message);
        }
        if (message instanceof JmsBytesMessage) {
            return readBytesMessage((JmsBytesMessage) message);
        }
        if (message != null) {
            return message.getBody(String.class);
        }
        return null;
    }

    private static String readBytesMessage(JmsBytesMessage message) throws JMSException {
        byte[] bytes = new byte[(int) message.getBodyLength()];
        message.readBytes(bytes);
        return Base64.isBase64(bytes) ? new String(Base64.decodeBase64(bytes)) : new String(bytes, UTF_8);
    }

    private static String readTextMessage(JmsTextMessage message) throws JMSException {
        var text = message.getText();
        return Base64.isBase64(text) ? new String(Base64.decodeBase64(text)) : text;
    }
}
