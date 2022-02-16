package uk.nhs.adaptors.pss.translator.mapper.medication;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import lombok.SneakyThrows;

public class MedicationMapperUtilsTest {

    private static final String XML_RESOURCES_BASE = "xml/EhrSupplyAuthorise/";
    private static final int EXPECTED_SIZE_NOTES = 3;

    public static final String ENCOUNTER_ID = "88eb391f-e7a0-47aa-a02b-215ef3c266df";
    public static final String PATIENT_ID = "b705a0da-3cb9-4331-aa1b-697aa7919883";
    public static final String MEDICATION_REQUEST_ID = "a9edfd5e-bd52-461f-8b96-9c5b181af5bf";

    @Test
    public void When_CreatingMedicationRequest_Expect_ContextAndSubjectMappedAppropriately() {
        Encounter encounter = (Encounter) new Encounter().setId(ENCOUNTER_ID);
        Patient patient = (Patient) new Patient().setId(PATIENT_ID);

        MedicationRequest medicationRequest = MedicationMapperUtils.createMedicationRequestSkeleton(
            patient,
            encounter,
            MEDICATION_REQUEST_ID
        );
        assertThat(medicationRequest.getId()).isEqualTo(MEDICATION_REQUEST_ID);
        assertThat(medicationRequest.getContext().getResource().getIdElement().getIdPart()).isEqualTo(ENCOUNTER_ID);
        assertThat(medicationRequest.getSubject().getResource().getIdElement().getIdPart()).isEqualTo(PATIENT_ID);
    }

    @Test
    public void When_CreatingPrescriptionTypeExtension_Expect_AcuteValues() {
        var supplyAuth = unmarshallSupplyAuthorise("buildPrescriptionAcute.xml");
        var extension = MedicationMapperUtils.buildPrescriptionTypeExtension(supplyAuth);
        assertThat(extension.isPresent()).isTrue();
        extension.ifPresent(extension1 -> {
            assertThat(extension1.getValue()).isInstanceOf(CodeableConcept.class);
            var codeableConcept = (CodeableConcept) extension1.getValue();
            assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Acute");
        });
    }

    @Test
    public void When_CreatingPrescriptionTypeExtension_Expect_RepeatValues() {
        var supplyAuth = unmarshallSupplyAuthorise("buildPrescriptionRepeat.xml");
        var extension = MedicationMapperUtils.buildPrescriptionTypeExtension(supplyAuth);
        assertThat(extension.isPresent()).isTrue();
        extension.ifPresent(extension1 -> {
            assertThat(extension1.getValue()).isInstanceOf(CodeableConcept.class);
            var codeableConcept = (CodeableConcept) extension1.getValue();
            assertThat(codeableConcept.getCodingFirstRep().getDisplay()).isEqualTo("Repeat");
        });
    }

    @Test
    public void When_CreatingNotes_Expect_AllPertinentInformationToBeMapped() {
        var supplyAuth = unmarshallSupplyAuthorise("buildNotesForAuthMultiple.xml");
        var notes = MedicationMapperUtils.buildNotes(supplyAuth.getPertinentInformation());

        assertThat(notes.size()).isEqualTo(EXPECTED_SIZE_NOTES);
    }

    @SneakyThrows
    private RCMRMT030101UK04Authorise unmarshallSupplyAuthorise(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04Authorise.class);
    }

}
