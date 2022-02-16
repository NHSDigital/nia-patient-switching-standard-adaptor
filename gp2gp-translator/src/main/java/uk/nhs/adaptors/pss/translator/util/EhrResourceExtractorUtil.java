package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;

import java.util.List;

public class EhrResourceExtractorUtil {

    private static final String IMMUNIZATION_SNOMED_CODE = "2.16.840.1.113883.2.1.3.2.3.15";

    public static RCMRMT030101UK04EhrComposition extractEhrCompositionForPlanStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        II resourceId) {
        return ehrExtract.getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> filterForMatchingEhrCompositionPlanStatement(ehrComposition, resourceId))
            .findFirst()
            .get();
    }

    public static List<RCMRMT030101UK04EhrComposition> extractValidImmunizationEhrCompositions(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> filterForValidObservationStatements(ehrComposition))
            .toList();
    }

    public static RCMRMT030101UK04EhrComposition extractEhrCompositionForObservationStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        II resourceId) {
        return ehrExtract.getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> filterForMatchingEhrCompositionObservationStatement(ehrComposition, resourceId))
            .findFirst()
            .get();
    }

    private static boolean filterForMatchingEhrCompositionPlanStatement(RCMRMT030101UK04EhrComposition ehrComposition, II resourceId) {
        return ehrComposition.getComponent()
            .stream()
            .anyMatch(component -> validPlanStatement(component, resourceId));
    }

    private static boolean validPlanStatement(RCMRMT030101UK04Component4 component, II resourceId) {
        return component.getPlanStatement() != null && component.getPlanStatement().getId() == resourceId;
    }

    private static boolean filterForMatchingEhrCompositionObservationStatement(RCMRMT030101UK04EhrComposition ehrComposition,
        II resourceId) {
        return ehrComposition.getComponent()
            .stream()
            .anyMatch(component -> validObservationStatement(component, resourceId));
    }

    private static boolean validObservationStatement(RCMRMT030101UK04Component4 component, II resourceId) {
        return component.getObservationStatement() != null && component.getObservationStatement().getId() == resourceId;
    }

    private static boolean filterForValidObservationStatements(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .anyMatch(component -> validImmunizationSnomedCode(component));
    }

    private static boolean validImmunizationSnomedCode(RCMRMT030101UK04Component4 component) {
        return component.getObservationStatement() != null
            && IMMUNIZATION_SNOMED_CODE.equals(component.getObservationStatement().getCode().getCodeSystem());
    }

    private static boolean hasEhrComposition(RCMRMT030101UK04Component3 component) {
        return component.getEhrComposition() != null;
    }

    private static boolean hasEhrFolder(RCMRMT030101UK04Component component) {
        return component.getEhrFolder() != null;
    }
}
