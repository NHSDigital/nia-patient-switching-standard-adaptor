package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.RCMRMT030101UKObservationStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;
import uk.nhs.adaptors.connector.model.ImmunizationSnomedCT;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class DatabaseImmunizationChecker implements ImmunizationChecker {
    private final ImmunizationSnomedCTDao immunizationSnomedDao;

    @Override
    public boolean isImmunization(RCMRMT030101UKObservationStatement observationStatement) {
        ImmunizationSnomedCT immunizationCode = null;

        if (!observationStatement.getCode().getTranslation().isEmpty()) {
            immunizationCode = immunizationSnomedDao
                    .getImmunizationSnomednUsingConceptId(observationStatement.getCode().getTranslation().get(0).getCode());
        }

        if (immunizationCode == null) {
            immunizationCode = immunizationSnomedDao.getImmunizationSnomednUsingConceptId(observationStatement.getCode().getCode());
        }

        return immunizationCode != null;
    }
}
