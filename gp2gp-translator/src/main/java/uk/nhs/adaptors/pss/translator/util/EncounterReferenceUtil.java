package uk.nhs.adaptors.pss.translator.util;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;

public class EncounterReferenceUtil {

    public static Reference getEncounterReference(List<RCMRMT030101UK04EhrComposition> compositionsContaintingStatement,
        List<Encounter> encounterList, String ehrCompositionId) {
        return compositionsContaintingStatement
            .stream()
            .map(component3 -> encounterList
                .stream()
                .filter(encounter -> encounter.getId().equals(ehrCompositionId))
                .findFirst()
            ).flatMap(Optional::stream)
            .findFirst().map(Reference::new).orElse(null);
    }

}
