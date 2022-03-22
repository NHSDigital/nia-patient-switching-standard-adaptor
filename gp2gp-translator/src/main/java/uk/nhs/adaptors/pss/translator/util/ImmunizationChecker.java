package uk.nhs.adaptors.pss.translator.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class ImmunizationChecker implements ImmunizationCheckerInterface {
    private final ImmunizationSnomedCTDao immunizationSnomedDao;

    @Override
    public boolean isImmunization(String code) {
        var immunizationCode = immunizationSnomedDao.getImmunizationSnomednUsingConceptId(code);
        return immunizationCode != null;
    }
}
