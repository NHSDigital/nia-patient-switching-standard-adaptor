package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.RCMRMT030101UK04Location;
import org.hl7.v3.RCMRMT030101UK04Place;

import uk.nhs.adaptors.pss.translator.util.AddressUtil;
import uk.nhs.adaptors.pss.translator.util.TelecomUtil;

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
            var telecom = getLocationTelecom(locatedPlace);
            var address = getLocationAddress(locatedPlace);

            return createLocation(id, identifier, name, telecom, address);
        }

        return createLocation(id, identifier, null, null, null);
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
                .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
                .setValue(id);
        return identifier;
    }

    private String getName(RCMRMT030101UK04Place locatedPlace) {
        var name = locatedPlace.getName();
        return name != null ? name : UNKNOWN_NAME;
    }

    private ContactPoint getLocationTelecom(RCMRMT030101UK04Place locatedPlace) {
        var telecom = locatedPlace.getTelecom().stream().findFirst();
        if (telecom.isPresent()) {
            return TelecomUtil.mapTelecom(telecom.get());
        }

        return null;
    }

    private Address getLocationAddress(RCMRMT030101UK04Place locatedPlace) {
        var address = locatedPlace.getAddr();
        if (address != null) {
            return AddressUtil.mapAddress(address);
        }

        return null;
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
