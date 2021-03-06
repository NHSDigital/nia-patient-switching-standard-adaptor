package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class LocationMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Location/";
    private static final String LOCATION_ID_EXTENSION = "-LOC";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Location-1";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String UNKNOWN_NAME = "Unknown";
    private static final int TELECOM_RANK = 1;
    private static final int TEL_PREFIX_INT = 4;

    private final LocationMapper locationMapper = new LocationMapper();

    @Test
    public void mapLocationWithValidData() {
        var ehrComposition = unmarshallCodeElement("full_valid_location_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(location.getName()).isEqualTo(ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName());
        assertTelecom(location.getTelecomFirstRep(), ehrComposition.getLocation().getLocatedEntity().getLocatedPlace()
                .getTelecom().get(0).getValue().substring(TEL_PREFIX_INT));
        assertThat(location.getAddress().getLine().toString()).isEqualTo(ehrComposition.getLocation().getLocatedEntity()
                .getLocatedPlace().getAddr().getStreetAddressLine().toString());
        assertThat(location.getAddress().getPostalCode()).isEqualTo(ehrComposition.getLocation().getLocatedEntity()
                .getLocatedPlace().getAddr().getPostalCode());
    }

    @Test
    public void mapLocationWithNoLocatedEntity() {
        var ehrComposition = unmarshallCodeElement("no_located_entity_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(locationId);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
    }

    @Test
    public void mapLocationWithNoLocatedPlace() {
        var ehrComposition = unmarshallCodeElement("no_located_place_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(locationId);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
    }

    @Test
    public void mapLocationWithKnownName() {
        var ehrComposition = unmarshallCodeElement("known_name_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(location.getName()).isEqualTo(ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName());
    }

    @Test
    public void mapLocationWithUnknownName() {
        var ehrComposition = unmarshallCodeElement("unknown_name_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(location.getName()).isEqualTo(UNKNOWN_NAME);
    }

    @Test
    public void mapLocationWithTelecomUseWP() {
        var ehrComposition = unmarshallCodeElement("wp_telecom_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(location.getName()).isEqualTo(ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName());
        assertTelecom(location.getTelecomFirstRep(), ehrComposition.getLocation().getLocatedEntity().getLocatedPlace()
                .getTelecom().get(0).getValue().substring(TEL_PREFIX_INT));
    }

    @Test
    public void mapLocationWithTelecomNoWP() {
        var ehrComposition = unmarshallCodeElement("not_wp_telecom_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(location.getName()).isEqualTo(ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName());
        assertTelecom(location.getTelecomFirstRep(), ehrComposition.getLocation().getLocatedEntity().getLocatedPlace()
                .getTelecom().get(0).getValue());
    }

    @Test
    public void mapLocationWithAddressAndPostcode() {
        var ehrComposition = unmarshallCodeElement("address_with_postcode_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(location.getName()).isEqualTo(ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName());
        assertThat(location.getAddress().getLine().toString()).isEqualTo(ehrComposition.getLocation().getLocatedEntity()
                .getLocatedPlace().getAddr().getStreetAddressLine().toString());
        assertThat(location.getAddress().getPostalCode()).isEqualTo(ehrComposition.getLocation().getLocatedEntity()
                .getLocatedPlace().getAddr().getPostalCode());
    }

    @Test
    public void mapLocationWithAddressNoPostCode() {
        var ehrComposition = unmarshallCodeElement("address_without_postcode_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(location.getName()).isEqualTo(ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName());
        assertThat(location.getAddress().getLine().toString()).isEqualTo(ehrComposition.getLocation().getLocatedEntity()
                .getLocatedPlace().getAddr().getStreetAddressLine().toString());
        assertThat(location.getAddress().getPostalCode()).isNull();
    }

    @Test
    public void mapLocationWithAddressNoWP() {
        var ehrComposition = unmarshallCodeElement("address_without_wp_example.xml");
        var locationId = ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION;

        Location location = locationMapper.mapToLocation(ehrComposition.getLocation(), ehrComposition.getId().getRoot(), PRACTISE_CODE);

        assertThat(location.getId()).isEqualTo(ehrComposition.getId().getRoot() + LOCATION_ID_EXTENSION);
        assertThatIdentifierIsValid(location.getIdentifierFirstRep(), locationId);
        assertThat(location.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
        assertThat(location.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(location.getName()).isEqualTo(ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName());
        assertThat(location.getAddress().isEmpty());
    }

    private void assertThatIdentifierIsValid(Identifier identifier, String id) {
        assertThat(identifier.getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(identifier.getValue()).isEqualTo(id);
    }

    private void assertTelecom(ContactPoint mappedTelecom, String value) {
        assertThat(mappedTelecom.getSystem()).isEqualTo(ContactPoint.ContactPointSystem.PHONE);
        assertThat(mappedTelecom.getUse()).isEqualTo(ContactPoint.ContactPointUse.WORK);
        assertThat(mappedTelecom.getRank()).isEqualTo(TELECOM_RANK);
        assertThat(mappedTelecom.getValue()).isEqualTo(value);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrComposition unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrComposition.class);
    }
}
