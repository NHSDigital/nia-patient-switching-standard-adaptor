package uk.nhs.adaptors.pss.translator.mapper;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;

@ExtendWith(MockitoExtension.class)
public class UnknownPractitionerHandlerTest {
    private static final String UNKNOWN_PRACTITIONER_NAME = "Unknown User";
    private static final String UNKNOWN_PRACTITIONER_ID = randomUUID().toString();
    private static final String EXISTING_PRACTITIONER_ID = randomUUID().toString();
    private static final Practitioner EXISTING_PRACTITIONER = (Practitioner) new Practitioner().setId(EXISTING_PRACTITIONER_ID);
    private static final Observation OBSERVATION_WITH_PERFORMER = new Observation().addPerformer(new Reference(EXISTING_PRACTITIONER));
    private static final ProcedureRequest PROCEDURE_WITH_REQUESTER =
        new ProcedureRequest().setRequester(new ProcedureRequest.ProcedureRequestRequesterComponent(new Reference(EXISTING_PRACTITIONER)));
    private static final Encounter ENCOUNTER_WITHOUT_RECORDER = new Encounter()
        .addParticipant(new EncounterParticipantComponent()
            .setIndividual(new Reference(EXISTING_PRACTITIONER)));

    @Mock
    private IdGeneratorService idGeneratorService;

    @InjectMocks
    private UnknownPractitionerHandler unknownPractitionerHandler;

    @Test
    public void updateUnknownPractitionersRefsDontAddUnknown() {
        Bundle bundle = bundle(OBSERVATION_WITH_PERFORMER, PROCEDURE_WITH_REQUESTER);

        unknownPractitionerHandler.updateUnknownPractitionersRefs(bundle);

        assertThat(bundle.getEntry().size()).isEqualTo(2);
    }

    @Test
    public void updateUnknownPractitionersRefsAddUnknownOnce() {
        when(idGeneratorService.generateUuid()).thenReturn(UNKNOWN_PRACTITIONER_ID);
        Bundle bundle = bundle(new Observation(), new ProcedureRequest(), ENCOUNTER_WITHOUT_RECORDER);

        unknownPractitionerHandler.updateUnknownPractitionersRefs(bundle);

        verifyUnknownPractitionerAdded(bundle);
    }

    private void verifyUnknownPractitionerAdded(Bundle bundle) {
        final int expectedCount = 4;
        int entriesCount = bundle.getEntry().size();
        assertThat(entriesCount).isEqualTo(expectedCount);
        BundleEntryComponent entry = bundle.getEntry().get(entriesCount - 1);
        assertThat(entry.getResource()).isInstanceOf(Practitioner.class);
        Practitioner unknown = (Practitioner) entry.getResource();
        assertThat(unknown.getNameFirstRep().getText()).isEqualTo(UNKNOWN_PRACTITIONER_NAME);
        assertThat(unknown.getId()).isEqualTo(UNKNOWN_PRACTITIONER_ID);
    }

    private Bundle bundle(Resource... resources) {
        Bundle bundle = new Bundle();

        stream(resources)
            .forEach(resource -> bundle.addEntry(new BundleEntryComponent().setResource(resource)));

        return bundle;
    }
}
