package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.RCMRMT030101UKObservationStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DatabaseImmunizationChecker implements ImmunizationChecker {
    private final ImmunizationSnomedCTDao immunizationSnomedDao;

    @Override
    public boolean isImmunization(RCMRMT030101UKObservationStatement observationStatement) {
        final boolean translationIsPresent = !observationStatement.getCode().getTranslation().isEmpty();

        if (translationIsPresent) {
            final boolean isImmunization = isTranslationCodeImmunization(observationStatement);

            if (isImmunization) {
                return true;
            }
        }

        return isCodeImmunization(observationStatement);
    }

    private boolean isTranslationCodeImmunization(RCMRMT030101UKObservationStatement observationStatement) {
        final String code = observationStatement.getCode()
            .getTranslation()
            .getFirst()
            .getCode();

        return immunizationSnomedDao.getImmunizationSnomednUsingConceptId(code) != null;
    }

    private boolean isCodeImmunization(RCMRMT030101UKObservationStatement observationStatement) {
        final String code = observationStatement.getCode().getCode();

        return immunizationSnomedDao.getImmunizationSnomednUsingConceptId(code) != null;
    }
}