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
            .filter(EhrResourceExtractorUtil::filterForValidImmunizationObservationStatements)
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

    public static RCMRMT030101UK04EhrComposition extractEhrCompositionForNarrativeStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        II resourceId) {
        return ehrExtract.getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> filterForMatchingEhrCompositionNarrativeStatement(ehrComposition, resourceId))
            .findFirst()
            .get();
    }

    public static List<RCMRMT030101UK04EhrComposition> extractValidDocumentReferenceEhrCompositions(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(EhrResourceExtractorUtil::filterForValidNarrativeStatement)
            .toList();
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

    private static boolean filterForValidImmunizationObservationStatements(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .anyMatch(EhrResourceExtractorUtil::validImmunizationSnomedCode);
    }

    private static boolean filterForValidNarrativeStatement(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .anyMatch(EhrResourceExtractorUtil::validDocumentReference);
    }

    private static boolean validDocumentReference(RCMRMT030101UK04Component4 component) {
        return !component.getNarrativeStatement().getReference().isEmpty();
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
