package uk.nhs.adaptors.pss.translator.util;

import java.util.List;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.v3.CsTelecommunicationAddressUse;
import org.hl7.v3.TEL;

public class TelecomUtil {
    private static final String WORK_PLACE = "WP";
    private static final int TELECOM_RANK = 1;
    private static final int TEL_PREFIX_INT = 4;

    public static ContactPoint mapTelecom(TEL telecom) {
        var contactPoint = new ContactPoint();
        return contactPoint
            .setValue(getTelecomValue(telecom))
            .setSystem(ContactPoint.ContactPointSystem.PHONE)
            .setUse(ContactPoint.ContactPointUse.WORK)
            .setRank(TELECOM_RANK);
    }

    private static String getTelecomValue(TEL telecom) {
        var value = telecom.getValue();
        if (value != null) {
            return isWorkPlaceValue(telecom.getUse()) ? stripTelPrefix(value) : value;
        }

        return null;
    }

    private static boolean isWorkPlaceValue(List<CsTelecommunicationAddressUse> use) {
        return use.stream()
            .anyMatch(addressUse -> WORK_PLACE.equals(addressUse.value()));
    }

    private static String stripTelPrefix(String value) {
        return value.substring(TEL_PREFIX_INT); // strips the 'tel:' prefix on phone numbers
    }
}
