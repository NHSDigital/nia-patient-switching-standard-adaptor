package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04Location;
import org.hl7.v3.RCMRMT030101UK04Place;

import java.util.List;
import java.util.UUID;

public class LocationMapper {
    private static final String UNKNOWN_NAME = "Unknown";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Location-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String LOCATION_ID_EXTENSION = "-LOC";
    private static final int TELECOM_RANK = 1;


    public Location mapToLocation(RCMRMT030101UK04Location location, String rootId) {

        var id = rootId + LOCATION_ID_EXTENSION;
        var identifier = getIdentifier(id);

        var locatedPlace = location.getLocatedEntity().getLocatedPlace();
        var name = getName(locatedPlace);
        var telecom = getTelecom(locatedPlace);
        var address = getAddress(locatedPlace);

        return createLocation(id, identifier, name, telecom, address);
    }

    private Identifier getIdentifier(String id) {
        var identifier = new Identifier();
        identifier
                .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL
                .setValue(id);

        return identifier;
    }

    private Address getAddress(RCMRMT030101UK04Place locatedPlace) {
        return new Address();
    }

    private ContactPoint getTelecom(RCMRMT030101UK04Place locatedPlace) {
        if (locatedPlace.getTelecom() != null) {
            var telecom = new ContactPoint();
            return telecom
                .setValue(locatedPlace.getTelecom().get(0).getValue())
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setUse(ContactPoint.ContactPointUse.WORK)
                .setRank(TELECOM_RANK);
        }

        return null;
    }

    private String getName(RCMRMT030101UK04Place locatedPlace) {
        var name = locatedPlace.getName();
        return name != null ? locatedPlace.getName() : UNKNOWN_NAME;
    }

    private Location createLocation(String id, Identifier identifier, String name, ContactPoint telecom,
                                    Address address) {
        var location = new Location();
        location.getIdentifier().add(identifier);
        location.getMeta().getProfile().add(new UriType(META_PROFILE));
        location.getTelecom().add(telecom);
        location.setAddress(address);
        location.setName(name)
                .setStatus(Location.LocationStatus.ACTIVE)
                .setId(id);
        return location;
    }
}
