package uk.nhs.adaptors.pss.translator.mapper;

import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class ObservationCommentMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/ObservationComment/";

    private final ObservationCommentMapper observationCommentMapper = new ObservationCommentMapper();

    @Test
    public void mapLocationWithValidData() {
        var ehrExtract = unmarshallCodeElement("test.xml");
        var narrativeStatement = getNarrativeStatement(ehrExtract);

        Observation mappedObservation = observationCommentMapper.mapToObservation(ehrExtract, narrativeStatement);

        var x = 1;
    }

    private RCMRMT030101UK04NarrativeStatement getNarrativeStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0)
            .getEhrFolder().getComponent().get(0)
            .getEhrComposition().getComponent().get(0)
            .getNarrativeStatement();
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
