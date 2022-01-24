package uk.nhs.adaptors.pss.translator.mapper;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.RCMRMT030101UK04Location;
import org.hl7.v3.RCMRMT030101UK04Place;
import org.hl7.v3.CsTelecommunicationAddressUse;
import org.hl7.v3.TEL;
import org.hl7.v3.AD;

import java.util.List;

public class LocationMapper {
    private static final String UNKNOWN_NAME = "Unknown";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Location-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String LOCATION_ID_EXTENSION = "-LOC";
    private static final String WORK_PLACE = "WP";
    private static final int TELECOM_RANK = 1;
    private static final int TEL_PREFIX_INT = 4;


    public Location mapToLocation(RCMRMT030101UK04Location location, String rootId) {

        var id = rootId + LOCATION_ID_EXTENSION;
        var identifier = getIdentifier(id);

        if (location.getLocatedEntity() != null && location.getLocatedEntity().getLocatedPlace() != null) {
            var locatedPlace = location.getLocatedEntity().getLocatedPlace();
            var name = getName(locatedPlace);
            var telecom = getTelecom(locatedPlace);
            var address = getAddress(locatedPlace);

            return createLocation(id, identifier, name, telecom, address);
        }

        return createLocation(id, identifier, null, null, null);
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
                .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL
                .setValue(id);
        return identifier;
    }

    private String getName(RCMRMT030101UK04Place locatedPlace) {
        var name = locatedPlace.getName();
        return name != null ? name : UNKNOWN_NAME;
    }

    private ContactPoint getTelecom(RCMRMT030101UK04Place locatedPlace) {
        var telecom = locatedPlace.getTelecom().stream().findFirst();
        if (telecom.isPresent()) {
            var contactPoint = new ContactPoint();
            return contactPoint
                    .setValue(getValue(telecom.get()))
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setUse(ContactPoint.ContactPointUse.WORK)
                    .setRank(TELECOM_RANK);
        }

        return null;
    }

    private String getValue(TEL telecom) {
        var value = telecom.getValue();
        if (value != null) {
            return isWorkPlaceValue(telecom.getUse()) ? stripTelPrefix(value) : value;
        }

        return null;
    }

    private boolean isWorkPlaceValue(List<CsTelecommunicationAddressUse> use) {
        if (!use.isEmpty()) {
            return use.stream().findFirst().get().value().equals(WORK_PLACE);
        }

        return false;
    }

    private String stripTelPrefix(String value) {
        return value.substring(TEL_PREFIX_INT); // strips the 'tel:' prefix on phone numbers
    }

    private Address getAddress(RCMRMT030101UK04Place locatedPlace) {
        var address = locatedPlace.getAddr();
        if (isValidAddress(address)) {
            var mappedAddress = new Address();
            if (address.getStreetAddressLine() != null) {
                address.getStreetAddressLine()
                        .forEach(addressLine -> mappedAddress.addLine(addressLine));
            }
            if (StringUtils.isNotEmpty(address.getPostalCode())) {
                mappedAddress.setPostalCode(address.getPostalCode());
            }

            return mappedAddress;
        }
        return null;
    }

    private boolean isValidAddress(AD address) {
        if (address != null && address.getUse() != null) {
            var use = address.getUse().stream().findFirst();
            return use.isPresent() && use.get().value().equals(WORK_PLACE);
        }

        return false;
    }

    private Location createLocation(String id, Identifier identifier, String name, ContactPoint telecom, Address address) {
        var location = new Location();
        location.getIdentifier().add(identifier);
        location.getMeta().getProfile().add(new UriType(META_PROFILE));
        location.getTelecom().add(telecom);
        location.setName(name)
                .setAddress(address)
                .setStatus(Location.LocationStatus.ACTIVE)
                .setId(id);
        return location;
    }
}
