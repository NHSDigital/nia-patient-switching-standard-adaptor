
package org.msg_header_2_0;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * &lt;p&gt;Java class for messageStatus.type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * &lt;pre&gt;
 * &amp;lt;simpleType name="messageStatus.type"&amp;gt;
 *   &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&amp;gt;
 *     &amp;lt;enumeration value="UnAuthorized"/&amp;gt;
 *     &amp;lt;enumeration value="NotRecognized"/&amp;gt;
 *     &amp;lt;enumeration value="Received"/&amp;gt;
 *     &amp;lt;enumeration value="Processed"/&amp;gt;
 *     &amp;lt;enumeration value="Forwarded"/&amp;gt;
 *   &amp;lt;/restriction&amp;gt;
 * &amp;lt;/simpleType&amp;gt;
 * &lt;/pre&gt;
 * 
 */
@XmlType(name = "messageStatus.type")
@XmlEnum
public enum MessageStatusType {

    @XmlEnumValue("UnAuthorized")
    UN_AUTHORIZED("UnAuthorized"),
    @XmlEnumValue("NotRecognized")
    NOT_RECOGNIZED("NotRecognized"),
    @XmlEnumValue("Received")
    RECEIVED("Received"),
    @XmlEnumValue("Processed")
    PROCESSED("Processed"),
    @XmlEnumValue("Forwarded")
    FORWARDED("Forwarded");
    private final String value;

    MessageStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MessageStatusType fromValue(String v) {
        for (MessageStatusType c: MessageStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
