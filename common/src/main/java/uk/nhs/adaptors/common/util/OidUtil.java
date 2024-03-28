package uk.nhs.adaptors.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OidUtil {

    private static final String URN_PREFIX = "urn:oid:";
    public static Optional<String> tryParseToUrn(String value) {
        if (isOid(value)) {
            return Optional.of(URN_PREFIX + value);
        }

        return Optional.empty();
    }

    public static boolean isOid(String value) {
        //regex taken from https://hl7.org/fhir/STU3/datatypes.html#oid
        return !StringUtils.isBlank(value) && value.matches("[0-2](\\.[1-9]\\d*)+");
    }
}
