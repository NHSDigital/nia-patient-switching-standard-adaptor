package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.TriFunction;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;

public abstract class AbstractMapper<T> {
    public Stream<T> mapEhrExtractToFhirResource(RCMRMT030101UK04EhrExtract ehrExtract,
        TriFunction<RCMRMT030101UK04EhrExtract, RCMRMT030101UK04EhrComposition, RCMRMT030101UK04Component4,
            Stream<T>> extractResourceFunction) {
        return ehrExtract.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .flatMap(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .flatMap(component4 -> extractResourceFunction.apply(ehrExtract, ehrComposition, component4)));
    }

    public abstract List<T> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        List<Encounter> encounters, String practiseCode);
}
