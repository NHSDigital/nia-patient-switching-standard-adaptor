package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;

import static uk.nhs.adaptors.pss.translator.util.EncounterReferenceUtil.getEncounterReference;

import java.util.List;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.junit.jupiter.api.Test;

public class EncounterReferenceUtilTest {

    private static final String ENCOUNTER_MATCHING_ID = "ENCOUNTER_MATCHING_ID";
    private static final String NON_MATCHING_ID = "NON_MATCHING_ID";

    private static final List<Encounter> ENCOUNTERS = List.of(
        (Encounter) new Encounter().setId(ENCOUNTER_MATCHING_ID)
    );

    @Test
    public void testCreateReferenceWithMatchingEncounter() {
        var reference = getEncounterReference(compositionWithId(ENCOUNTER_MATCHING_ID), ENCOUNTERS, ENCOUNTER_MATCHING_ID);

        assertThat(reference).isNotNull();
        assertThat(reference.getResource()).isNotNull();
        assertThat(reference.getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_MATCHING_ID);
    }

    @Test
    public void testCreateReferenceWithNonMatchingEncounter() {
        var reference = getEncounterReference(compositionWithId(NON_MATCHING_ID), ENCOUNTERS, NON_MATCHING_ID);

        assertThat(reference).isNull();
    }

    private List<RCMRMT030101UK04EhrComposition> compositionWithId(String id) {
        var composition = new RCMRMT030101UK04EhrComposition();
        var compositionId = new II();
        compositionId.setRoot(id);
        composition.setId(compositionId);
        composition.setComponent(List.of(new RCMRMT030101UK04Component4()));
        return List.of(composition);
    }

}
