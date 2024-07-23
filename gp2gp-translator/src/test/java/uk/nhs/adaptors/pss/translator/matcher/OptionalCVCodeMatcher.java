package uk.nhs.adaptors.pss.translator.matcher;

import org.hl7.v3.CV;
import org.mockito.ArgumentMatcher;

import java.util.Objects;
import java.util.Optional;

public class OptionalCVCodeMatcher implements ArgumentMatcher<Optional<CV>> {
    private final String expectedCode;

    public OptionalCVCodeMatcher(String expectedCode) {
        this.expectedCode = expectedCode;
    }

    @Override
    public boolean matches(Optional<CV> argument) {
        if (Objects.isNull(argument) || argument.isEmpty()) {
            return false;
        }

        return argument.get().getCode().equals(expectedCode);
    }
}
