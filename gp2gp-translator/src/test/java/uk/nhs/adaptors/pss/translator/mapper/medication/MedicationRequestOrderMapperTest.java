package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.MetaFactory.MetaType.META_WITHOUT_SECURITY;
import static uk.nhs.adaptors.pss.translator.MetaFactory.MetaType.META_WITH_SECURITY;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.ORDER;
import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus.COMPLETED;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.RCMRMT030101UKComponent2;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKMedicationStatement;
import org.hl7.v3.RCMRMT030101UKPrescribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.MetaFactory;
import uk.nhs.adaptors.pss.translator.matcher.OptionalCVCodeMatcher;
import uk.nhs.adaptors.pss.translator.service.ConfidentialityService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@ExtendWith(MockitoExtension.class)
public class MedicationRequestOrderMapperTest {

    private static final String META_PROFILE = "MedicationRequest-1";
    private static final String XML_RESOURCES_MEDICATION_STATEMENT = "xml/MedicationStatement/";
    private static final String PRACTISE_CODE = "TESTPRACTISECODE";
    private static final String TEST_ID = "TEST_ID";
    private static final String MEDICATION_ID = "MEDICATION_ID";
    private static final String TAKE_ONE_DAILY = "TAKE ONE DAILY";
    private static final String AVAILABILITY_TIME = "20060426";

    private static final int THREE = 3;
    private static final int SEVEN = 7;

    @Mock
    private MedicationMapper medicationMapper;
    @Mock
    private ConfidentialityService confidentialityService;
    @InjectMocks
    private MedicationRequestOrderMapper medicationRequestOrderMapper;

    @BeforeEach
    void beforeEach() {
        Mockito.lenient().when(confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            eq(META_PROFILE),
            any(Optional.class)
        )).thenReturn(MetaFactory.getMetaFor(META_WITHOUT_SECURITY, META_PROFILE));
    }

    @Test
    public void When_MappingPrescribeResourceWithAllOptionals_Expect_AllFieldsToBeMappedCorrectly() {
        var medicationStatement = unmarshallMedicationStatement("medicationStatementPrescribeAllOptionals.xml");
        var prescribe = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UKComponent2::getEhrSupplyPrescribe)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        assertThat(prescribe.isPresent()).isTrue();
        var medicationRequest = medicationRequestOrderMapper.mapToOrderMedicationRequest(new RCMRMT030101UKEhrExtract(),
            medicationStatement, prescribe.get(), PRACTISE_CODE);
        assertCommonValues(medicationRequest);
        medicationRequest
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1")
            .forEach(extension -> assertPrescriptionType(extension, "Repeat"));
        assertThat(medicationRequest.getBasedOnFirstRep().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
        assertThat(medicationRequest.getNote()).hasSize(THREE);
        assertThat(medicationRequest.getDosageInstructionFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
        assertThat(medicationRequest.getDispenseRequest().getQuantity().getValue().intValue()).isEqualTo(SEVEN);
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().getStartElement().getValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType(AVAILABILITY_TIME).getValue());

        assertMetaSecurityNotPresent(medicationRequest);
    }

    @Test
    public void When_MappingPrescribeResourceWithNoOptionals_Expect_AllFieldsToBeMappedCorrectly() {
        var medicationStatement = unmarshallMedicationStatement("medicationStatementPrescribeNoOptionals.xml");
        var prescribe = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UKComponent2::getEhrSupplyPrescribe)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        assertThat(prescribe.isPresent()).isTrue();
        var medicationRequest = medicationRequestOrderMapper.mapToOrderMedicationRequest(new RCMRMT030101UKEhrExtract(),
            medicationStatement, prescribe.get(), PRACTISE_CODE);
        assertCommonValues(medicationRequest);

        medicationRequest
            .getExtensionsByUrl("https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1")
            .forEach(extension -> assertPrescriptionType(extension, "Repeat"));
        assertThat(medicationRequest.getBasedOnFirstRep().getReferenceElement().getIdPart()).isEqualTo(TEST_ID);
        assertThat(medicationRequest.getNote()).hasSize(1);
        assertThat(medicationRequest.getDosageInstructionFirstRep().getText()).isEqualTo(TAKE_ONE_DAILY);
        assertThat(medicationRequest.getDispenseRequest().getQuantity().getValue()).isNull();
        assertThat(medicationRequest.getDispenseRequest().getValidityPeriod().getStartElement().getValue())
            .isEqualTo(DateFormatUtil.parseToDateTimeType(AVAILABILITY_TIME).getValue());

        assertMetaSecurityNotPresent(medicationRequest);
    }

    @Test
    public void When_MappingPrescribeResourceWithNopatConfidentialityCode_Expect_MetaSecurityToBeAdded() {
        final RCMRMT030101UKMedicationStatement medicationStatement = unmarshallMedicationStatement(
            "medicationStatementPrescribeNoOptionalsWithNopatConfidentialityCode.xml"
        );

        final Optional<RCMRMT030101UKPrescribe> prescribe = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UKComponent2::getEhrSupplyPrescribe)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        when(confidentialityService.createMetaAndAddSecurityIfConfidentialityCodesPresent(
            any(String.class),
            any(Optional.class)
        )).thenReturn(MetaFactory.getMetaFor(META_WITH_SECURITY, META_PROFILE));

        final MedicationRequest medicationRequest = medicationRequestOrderMapper.mapToOrderMedicationRequest(
            new RCMRMT030101UKEhrExtract(),
            medicationStatement,
            prescribe.orElseThrow(),
            PRACTISE_CODE
        );

        assertThat(medicationRequest.getMeta().getSecurity()).hasSize(1);

        assertMetaSecurityPresent(medicationRequest);
    }

    @Test
    public void When_MappingPrescribeResourceWithNoscrubConfidentialityCode_Expect_MetaSecurityNotToBeAdded() {
        final RCMRMT030101UKMedicationStatement medicationStatement =
            unmarshallMedicationStatement("medicationStatementPrescribeNoOptionalsWithNoscrubConfidentialityCode.xml");
        final Optional<RCMRMT030101UKPrescribe> prescribe = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UKComponent2::getEhrSupplyPrescribe)
            .findFirst();

        when(medicationMapper.extractMedicationReference(any()))
            .thenReturn(Optional.of(new Reference(new IdType(ResourceType.Medication.name(), MEDICATION_ID))));

        final MedicationRequest medicationRequest = medicationRequestOrderMapper.mapToOrderMedicationRequest(
            new RCMRMT030101UKEhrExtract(),
            medicationStatement,
            prescribe.orElseThrow(),
            PRACTISE_CODE
        );

        assertThat(medicationRequest.getMeta().getSecurity()).hasSize(0);
        verify(confidentialityService).createMetaAndAddSecurityIfConfidentialityCodesPresent(
            META_PROFILE,
            medicationStatement.getConfidentialityCode()
        );
    }

    private void assertMetaSecurityPresent(MedicationRequest request) {
        final Coding expectedNopatCoding = MetaFactory.getNopatCoding();
        final Coding actualSecurityCoding = request.getMeta().getSecurity().get(0);
        final int securitySize = request.getMeta().getSecurity().size();

        assertAll(
            () -> assertThat(securitySize).isEqualTo(1),
            () -> assertThat(expectedNopatCoding).isEqualTo(actualSecurityCoding)
        );

        verify(confidentialityService).createMetaAndAddSecurityIfConfidentialityCodesPresent(
            eq(META_PROFILE),
            argThat(new OptionalCVCodeMatcher("NOPAT"))
        );
    }

    private void assertMetaSecurityNotPresent(MedicationRequest request) {
        final Meta meta = request.getMeta();

        assertAll(
            () -> assertThat(meta.getSecurity()).hasSize(0),
            () -> assertThat(meta.getProfile().get(0).getValue()).isEqualTo(META_PROFILE)
        );

        verify(confidentialityService).createMetaAndAddSecurityIfConfidentialityCodesPresent(
            eq(META_PROFILE),
            eq(Optional.empty())
        );
    }

    public void assertCommonValues(MedicationRequest medicationRequest) {
        assertThat(medicationRequest.getIdentifier()).hasSize(1);
        assertThat(medicationRequest.getIntent()).isEqualTo(ORDER);
        assertThat(medicationRequest.getStatus()).isEqualTo(COMPLETED);
        assertThat(medicationRequest.getMedicationReference().getReferenceElement().getIdPart()).isEqualTo(MEDICATION_ID);
    }

    public void assertPrescriptionType(Extension extension, String expectedDisplayValue) {
        var codeableConcept = (CodeableConcept) extension.getValue();
        assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo(expectedDisplayValue);
    }

    @SneakyThrows
    private RCMRMT030101UKMedicationStatement unmarshallMedicationStatement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_MEDICATION_STATEMENT + fileName),
            RCMRMT030101UKMedicationStatement.class);
    }
}
