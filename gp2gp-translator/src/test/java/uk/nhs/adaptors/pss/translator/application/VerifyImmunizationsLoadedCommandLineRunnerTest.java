package uk.nhs.adaptors.pss.translator.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VerifyImmunizationsLoadedCommandLineRunnerTest {

    @Mock
    private ImmunizationSnomedCTDao immunizationSnomedCTDao;

    @InjectMocks
    private VerifyImmunizationsLoadedCommandLineRunner verifyImmunizationsLoadedCommandLineRunner;

    @Test
    public void When_RunApplicationAndImmunizationsAreLoaded_Expect_ExceptionIsNotThrown() {
        when(immunizationSnomedCTDao.areImmunizationCodesLoaded()).thenReturn(true);

        assertDoesNotThrow(() -> verifyImmunizationsLoadedCommandLineRunner.run());
    }

    @Test
    public void When_RunApplicationAndImmunizationsNotLoaded_Expect_RuntimeExceptionThrown() {
        when(immunizationSnomedCTDao.areImmunizationCodesLoaded()).thenReturn(false);

        assertThrows(RuntimeException.class, () -> verifyImmunizationsLoadedCommandLineRunner.run());
    }
}
