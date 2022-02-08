package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;

public class ObservationCommentMapper {

    public Observation mapToObservation(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04NarrativeStatement narrativeStatement) {

        var id = narrativeStatement.getId().getRoot();
        // meta
        // identifier
        // status
        // code
        // subject
        // context
        // effective
        // issued
        // performer
        // comment

        return createObservation(id);
    }

    private Observation createObservation(String id) {
        var observation = new Observation();

        observation.setId(id);

        return observation;
    }
}
