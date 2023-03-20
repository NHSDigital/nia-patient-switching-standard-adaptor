package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.springframework.util.ResourceUtils.getFile;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;
import static org.assertj.core.api.Assertions.assertThat;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.v3.RCMRMT030101UK04Consumable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

@ExtendWith(MockitoExtension.class)
public class MedicationMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/Consumable/";
    private static final String TEST_ID = "TEST_ID";

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @Mock
    private IdGeneratorService idGeneratorService;

    @Mock
    private MedicationMapperContext medicationMapperContext;

    @InjectMocks
    private MedicationMapper medicationMapper;

    @BeforeEach
    public void setup() {
        when(codeableConceptMapper.mapToCodeableConceptForMedication(any())).thenReturn(new CodeableConcept());
    }

    @Test
    public void When_MappingConsumable_Expect_CorrectFieldsMapped() {
        var consumable = unmarshallConsumable("consumable1.xml");
        var medication = medicationMapper.createMedication(consumable);

        verify(codeableConceptMapper, times(1)).mapToCodeableConceptForMedication(any());

        assertThat(medication.getMeta()).isNotNull();
        assertThat(medication.getCode()).isNotNull();
    }

    @Test
    public void When_MappingConsumableWithCodeNotFound_Expect_CorrectFieldsMappedWithDegraded() {
        var consumable = unmarshallConsumable("degraded_consumable.xml");
        var medication = medicationMapper.createMedication(consumable);

        verify(codeableConceptMapper, times(1)).mapToCodeableConceptForMedication(any());

        assertThat(medication.getMeta()).isNotNull();
        assertThat(medication.getCode()).isNotNull();
        assertThat(medication.getCode().getCoding()).isNotNull();
        assertThat(medication.getCode().getCoding().get(0)).isEqualTo(DegradedCodeableConcepts.DEGRADED_MEDICATION);
    }

    @SneakyThrows
    private RCMRMT030101UK04Consumable unmarshallConsumable(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04Consumable.class);
    }
}
