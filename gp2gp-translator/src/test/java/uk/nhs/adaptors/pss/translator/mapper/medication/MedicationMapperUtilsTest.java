package uk.nhs.adaptors.pss.translator.mapper.medication;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

public class MedicationMapperUtilsTest {

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

}
