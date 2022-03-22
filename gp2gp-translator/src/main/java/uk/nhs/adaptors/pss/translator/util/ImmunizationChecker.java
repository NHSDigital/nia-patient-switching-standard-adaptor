package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.CD;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImmunizationChecker implements iImmunizationChecker {
    private final ImmunizationSnomedCTDao immunizationSnomedDao;

    @Override
    public boolean isImmunization(CD code) {
        if (code.hasCodeSystem()) {
            var immunizationCode = immunizationSnomedDao.getImmunizationSnomednUsingConceptId(code.getCodeSystem());
            return immunizationCode != null;
        }
        return false;
    }
}
