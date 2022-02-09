package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;

public class EncounterMapper {

    public Encounter mapToEncounter(RCMRMT030101UK04EhrComposition ehrComposition) {

        return createEncounter();
    }

    private Encounter createEncounter() {
        var encounter = new Encounter();

        return encounter;
    }
}
