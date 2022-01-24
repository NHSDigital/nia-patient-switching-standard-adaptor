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
    private static final String LOCATION_ID_EXTENSION = "-LOC";
    private static final String UNKNOWN_NAME = "Unknown";

    private final LocationMapper locationMapper = new LocationMapper();

    @Test
    public void mapLocationWithTelecomAndAddressData() {
        var ehrComposition = unmarshallCodeElement("address_and_telecom_example.xml");

        Location outputLocation = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot());

        assertThat(outputLocation).isNotNull();
    }

    @Test
    public void mapLocationWithKnownName() {
        var ehrComposition = unmarshallCodeElement("known_name_example.xml");

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot());

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);

        assertThat(location.getName()).isEqualTo(ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName());
    }

    @Test
    public void mapLocationWithUnknownName() {
        var ehrComposition = unmarshallCodeElement("unknown_name_example.xml");

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot());

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThat(location.getName()).isEqualTo(UNKNOWN_NAME);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrComposition unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrComposition.class);
    }
}
