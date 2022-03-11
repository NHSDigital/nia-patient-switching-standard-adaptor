package uk.nhs.adaptors.pss.translator.mapper.medication;

import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationRequestMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/MedicationStatement/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final int SINGLE_INVOCATION = 1;
    private static final int DOUBLE_INVOCATION = 2;
    private static final int EXPECTED_RESOURCES_MAPPED = 6;

    private static final DateTimeType EXPECTED_DATE_TIME_TYPE = DateFormatUtil.parseToDateTimeType("20100115");

    private static final String PATIENT_ID = "d7d4ab01-c3a9-4120-9364-4a5b3fd614d0";

    @Mock
    private MedicationMapper medicationMapper;
    @Mock
    private MedicationRequestOrderMapper medicationRequestOrderMapper;
    @Mock
    private MedicationRequestPlanMapper medicationRequestPlanMapper;
    @Mock
    private MedicationStatementMapper medicationStatementMapper;
    @Mock
    private MedicationMapperContext medicationMapperContext;

    @InjectMocks
    private MedicationRequestMapper medicationRequestMapper;

    @Test
    public void When_MappingMedicationStatement_Expect_CorrectMappersToBeCalled() {
        var ehrExtract = unmarshallEhrExtract("ehrExtract1.xml");

        when(medicationRequestPlanMapper.mapToPlanMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationRequestOrderMapper.mapToOrderMedicationRequest(any(), any(), any(), any())).thenReturn(new MedicationRequest());
        when(medicationStatementMapper.mapToMedicationStatement(any(), any(), any(), any(), any())).thenReturn(new MedicationStatement());
        when(medicationMapper.createMedication(any())).thenReturn(new Medication());

        var resources = medicationRequestMapper.mapResources(ehrExtract, List.of(), (Patient) new Patient().setId(PATIENT_ID),
            PRACTISE_CODE);

        verify(medicationRequestPlanMapper, times(DOUBLE_INVOCATION)).mapToPlanMedicationRequest(any(), any(), any(), any());
        verify(medicationRequestOrderMapper, times(SINGLE_INVOCATION)).mapToOrderMedicationRequest(any(), any(), any(), any());
        verify(medicationStatementMapper, times(DOUBLE_INVOCATION)).mapToMedicationStatement(any(), any(), any(), any(), any());
        verify(medicationMapper, times(SINGLE_INVOCATION)).createMedication(any());

        assertThat(resources.size()).isEqualTo(EXPECTED_RESOURCES_MAPPED);
        resources
            .stream()
            .filter(resource -> ResourceType.MedicationRequest.equals(resource.getResourceType()))
            .map(MedicationRequest.class::cast)
            .forEach(medicationRequest -> {
                assertThat(medicationRequest.getSubject().getResource().getIdElement().getIdPart()).isEqualTo(PATIENT_ID);
                assertThat(medicationRequest.getAuthoredOnElement().getValue())
                    .isEqualTo(EXPECTED_DATE_TIME_TYPE.getValue());
            });

        resources
            .stream()
            .filter(resource -> ResourceType.MedicationStatement.equals(resource.getResourceType()))
            .map(MedicationStatement.class::cast)
            .forEach(medicationStatement -> {
                assertThat(medicationStatement.getSubject().getResource().getIdElement().getIdPart()).isEqualTo(PATIENT_ID);
                assertThat(medicationStatement.getDateAssertedElement().getValue())
                    .isEqualTo(EXPECTED_DATE_TIME_TYPE.getValue());
            });
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
