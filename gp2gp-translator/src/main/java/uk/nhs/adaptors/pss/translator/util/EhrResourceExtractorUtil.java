package uk.nhs.adaptors.pss.translator.util;

import java.util.List;

import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;

public class EhrResourceExtractorUtil {

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

    public static RCMRMT030101UK04EhrComposition extractEhrCompositionForCompoundStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        II resourceId) {
        return ehrExtract.getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> filterForMatchingEhrCompositionCompoundStatement(ehrComposition, resourceId))
            .findFirst()
            .get();
    }

    public static boolean hasEhrComposition(RCMRMT030101UK04Component3 component) {
        return component.getEhrComposition() != null;
    }

    public static boolean hasEhrFolder(RCMRMT030101UK04Component component) {
        return component.getEhrFolder() != null;
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

    private static boolean filterForMatchingEhrCompositionNarrativeStatement(RCMRMT030101UK04EhrComposition ehrComposition, II resourceId) {
        return ehrComposition.getComponent()
            .stream()
            .anyMatch(component -> validNarrativeStatement(component, resourceId));
    }

    private static boolean validNarrativeStatement(RCMRMT030101UK04Component4 component, II resourceId) {
        return component.getNarrativeStatement() != null && component.getNarrativeStatement().getId() == resourceId;
    }

    private static boolean filterForMatchingEhrCompositionCompoundStatement(RCMRMT030101UK04EhrComposition ehrComposition,
        II resourceId) {
        return ehrComposition.getComponent()
            .stream()
            .anyMatch(component -> validCompoundStatement(component, resourceId));
    }

    private static boolean validCompoundStatement(RCMRMT030101UK04Component4 component, II resourceId) {
        return component.getCompoundStatement() != null && !component.getCompoundStatement().getId().isEmpty()
            && component.getCompoundStatement().getId().get(0) == resourceId;
    }
}
