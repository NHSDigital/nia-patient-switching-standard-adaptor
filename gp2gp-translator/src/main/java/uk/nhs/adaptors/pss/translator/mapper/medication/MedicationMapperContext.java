package uk.nhs.adaptors.pss.translator.mapper.medication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.hl7.v3.CD;
import org.springframework.stereotype.Component;

@Component
public class MedicationMapperContext {

    private final ThreadLocal<Map<String, String>> medicationIds = ThreadLocal.withInitial(HashMap::new);

    public String getMedicationId(CD code) {
        var key = buildKey(code);
        var value = medicationIds.get().getOrDefault(key, StringUtils.EMPTY);

        if (StringUtils.isNotBlank(value)) {
            return value;
        } else {
            var newId = UUID.randomUUID().toString();
            medicationIds.get().put(key, newId);
            return newId;
        }
    }

    public void reset() {
        medicationIds.get().clear();
    }

    public boolean contains(CD code) {
        return medicationIds.get().containsKey(buildKey(code));
    }

    private static String buildKey(CD code) {
        return keyBuilder(CD::hasCode, CD::getCode, code)
            + keyBuilder(CD::hasOriginalText, CD::getOriginalText, code)
            + keyBuilder(CD::hasDisplayName, CD::getDisplayName, code);
    }

    private static String keyBuilder(Function<CD, Boolean> checker, Function<CD, String> getter, CD code) {
        return checker.apply(code) ? getter.apply(code) : null;
    }

}
