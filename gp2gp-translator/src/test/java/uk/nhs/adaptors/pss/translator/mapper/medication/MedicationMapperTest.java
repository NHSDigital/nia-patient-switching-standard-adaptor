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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;

@ExtendWith(MockitoExtension.class)
public class MedicationMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/Consumable/";
    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @InjectMocks
    private MedicationMapper medicationMapper;

    @BeforeEach
    public void setup() {
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(new CodeableConcept());
    }

    @Test
    public void When_MappingConsumable_Expect_CorrectFieldsMapped() {
        var consumable = unmarshallConsumable("consumable1.xml");
        var medication = medicationMapper.createMedication(consumable);

        verify(codeableConceptMapper, times(1)).mapToCodeableConcept(any());

        assertThat(medication.getId()).isNotBlank();
        assertThat(medication.getMeta()).isNotNull();
        assertThat(medication.getCode()).isNotNull();
    }

    @SneakyThrows
    private RCMRMT030101UK04Consumable unmarshallConsumable(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE+ fileName), RCMRMT030101UK04Consumable.class);
    }
}
