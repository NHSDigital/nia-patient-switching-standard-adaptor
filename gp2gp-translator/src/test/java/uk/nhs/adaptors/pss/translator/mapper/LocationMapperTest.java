package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04Location;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

public class LocationMapperTest {
    private static final String XML_RESOURCES_BASE = "XML/Location/";

    private final LocationMapper locationMapper = new LocationMapper();

    @Test
    public void mapLocationWithTelecomAndAddressData() {
        var ehrComposition = unmarshallCodeElement("address_and_telecom_example.xml");

        Location outputLocation = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot());

        assertThat(outputLocation).isNotNull();
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrComposition unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrComposition.class);
    }
}
