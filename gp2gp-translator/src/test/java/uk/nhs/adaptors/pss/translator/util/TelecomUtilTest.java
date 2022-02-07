package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.v3.TEL;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class TelecomUtilTest {
    private static final String XML_RESOURCES_BASE = "xml/Telecom/";
    private static final int TELECOM_RANK = 1;

    @Test
    public void mapWpTelecom() {
        var inputTelecom = unmarshallTelecomElement("wp_telecom_example.xml");

        var telecom = TelecomUtil.mapTelecom(inputTelecom);

        assertThat(telecom.getSystem()).isEqualTo(ContactPoint.ContactPointSystem.PHONE);
        assertThat(telecom.getUse()).isEqualTo(ContactPoint.ContactPointUse.WORK);
        assertThat(telecom.getRank()).isEqualTo(TELECOM_RANK);
        assertThat(telecom.getValue()).isEqualTo("01234567890");
    }

    @Test
    public void mapNonWpTelecom() {
        var inputTelecom = unmarshallTelecomElement("non_wp_telecom_example.xml");

        var telecom = TelecomUtil.mapTelecom(inputTelecom);

        assertThat(telecom.getSystem()).isEqualTo(ContactPoint.ContactPointSystem.PHONE);
        assertThat(telecom.getUse()).isEqualTo(ContactPoint.ContactPointUse.WORK);
        assertThat(telecom.getRank()).isEqualTo(TELECOM_RANK);
        assertThat(telecom.getValue()).isEqualTo("01234567890");
    }

    @SneakyThrows
    private TEL unmarshallTelecomElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), TEL.class);
    }
}
