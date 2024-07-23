package uk.nhs.adaptors.pss.translator;

import org.hl7.v3.CV;
import org.mockito.ArgumentMatcher;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class NopatMatcher implements ArgumentMatcher<Optional<CV>> {
    private static final String NOPAT = "NOPAT";

    @Override
    public boolean matches(Optional<CV> argument) {
        if (Objects.isNull(argument) || argument.isEmpty()) {
            return false;
        }

        return isNopatPresent(argument.get());
    }

    private boolean isNopatPresent(CV cv) {
        return cv.getCode().equals(NOPAT);
    }
}