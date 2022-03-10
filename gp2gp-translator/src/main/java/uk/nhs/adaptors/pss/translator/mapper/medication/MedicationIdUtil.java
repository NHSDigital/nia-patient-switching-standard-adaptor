package uk.nhs.adaptors.pss.translator.mapper.medication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.hl7.v3.CD;

public class MedicationIdUtil {

    private static final ThreadLocal<Map<String, String>> MEDICATION_IDS = ThreadLocal.withInitial(HashMap::new);

    public static String getMedicationId(CD code) {
        var key = buildKey(code);
        var value = MEDICATION_IDS.get().getOrDefault(key, StringUtils.EMPTY);

        if (StringUtils.isNotBlank(value)) {
            return value;
        } else {
            var newId = UUID.randomUUID().toString();
            MEDICATION_IDS.get().put(key, newId);
            return newId;
        }
    }

    public static boolean contains(CD code) {
        return MEDICATION_IDS.get().containsKey(buildKey(code));
    }

    public static void resetMedicationMaps() {
        MEDICATION_IDS.get().clear();
    }

    private static String buildKey(CD code) {
        return keyBuilder(CD::hasCode, CD::getCode, code)
            + keyBuilder(CD::hasOriginalText, CD::getOriginalText, code)
            + keyBuilder(CD::hasDisplayName, CD::getDisplayName, code);
    }

    private static String keyBuilder(Function<CD, Boolean> checker, Function<CD, String> getter, CD code) {
        if (checker.apply(code)) {
            return getter.apply(code);
        }
        return null;
    }

}
