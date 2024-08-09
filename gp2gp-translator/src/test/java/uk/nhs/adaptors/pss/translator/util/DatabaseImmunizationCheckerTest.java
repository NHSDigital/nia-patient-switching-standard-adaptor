package uk.nhs.adaptors.pss.translator.util;

import jakarta.xml.bind.JAXBException;
import org.hl7.v3.RCMRMT030101UKComponent3;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKObservationStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.connector.dao.ImmunizationSnomedCTDao;
import uk.nhs.adaptors.connector.model.ImmunizationSnomedCT;
import uk.nhs.adaptors.pss.translator.FileFactory;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;
import static uk.nhs.adaptors.pss.translator.TestUtility.getEhrFolderComponents;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

@ExtendWith(MockitoExtension.class)
class DatabaseImmunizationCheckerTest {
    @Mock
    private ImmunizationSnomedCTDao immunizationSnomedCTDao;

    @InjectMocks
    private DatabaseImmunizationChecker databaseImmunizationChecker;

    @Captor
    private ArgumentCaptor<String> snomedCtIdCaptor;

    private static final String TEST_FILES_DIRECTORY = "Immunization";

    @Test
    void When_IsObservationStatementImmunization_With_ImmunizationCode_Expect_True() throws JAXBException {
        final String expectedCode = "3955997015";
        final RCMRMT030101UKObservationStatement observationStatement = getObservationStatementFromExtract(
            "full_valid_immunization_with_no_translation.xml");
        final ImmunizationSnomedCT immunizationSnomedCT = ImmunizationSnomedCT.builder()
            .conceptId(expectedCode)
            .build();

        when(immunizationSnomedCTDao.getImmunizationSnomedUsingConceptOrDescriptionId(
            snomedCtIdCaptor.capture()
        )).thenReturn(immunizationSnomedCT);

        final boolean result = databaseImmunizationChecker.isImmunization(observationStatement);

        assertAll(
            () -> assertThat(result).isTrue(),
            () -> assertThat(snomedCtIdCaptor.getValue()).isEqualTo(expectedCode)
        );
    }

    @Test
    void When_IsObservationStatementImmunization_With_ImmunizationCodeAndNonImmunizationTranslation_Expect_True() throws JAXBException {
        final String snomedCode = "142934010";
        final String readsV2Code = "65E..00";
        final RCMRMT030101UKObservationStatement observationStatement = getObservationStatementFromExtract(
            "full_valid_immunization_with_translation.xml"
        );

        final ImmunizationSnomedCT immunizationSnomedCT = ImmunizationSnomedCT.builder()
            .conceptId(snomedCode)
            .build();

        when(immunizationSnomedCTDao.getImmunizationSnomedUsingConceptOrDescriptionId(
            snomedCtIdCaptor.capture()
        )).thenReturn(null, immunizationSnomedCT);

        final boolean result = databaseImmunizationChecker.isImmunization(observationStatement);

        assertAll(
            () -> assertThat(result).isTrue(),
            () -> assertThat(snomedCtIdCaptor.getAllValues().getFirst()).isEqualTo(readsV2Code),
            () -> assertThat(snomedCtIdCaptor.getAllValues().get(1)).isEqualTo(snomedCode)
        );
    }

    private RCMRMT030101UKObservationStatement getObservationStatementFromExtract(String filename) throws JAXBException {
        final RCMRMT030101UKEhrExtract ehrExtract = getEhrExtractFromFile(filename);
        final List<RCMRMT030101UKComponent3> components = getEhrFolderComponents(ehrExtract, 0);

        return components.getFirst()
            .getEhrComposition()
            .getComponent()
            .getFirst()
            .getObservationStatement();
    }

    private RCMRMT030101UKEhrExtract getEhrExtractFromFile(String filename) throws JAXBException {
        final File file = FileFactory.getXmlFileFor(TEST_FILES_DIRECTORY, filename);
        return unmarshallFile(file, RCMRMT030101UKEhrExtract.class);
    }
}