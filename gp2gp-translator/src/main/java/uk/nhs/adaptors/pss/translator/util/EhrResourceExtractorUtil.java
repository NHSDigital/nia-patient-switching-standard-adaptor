package uk.nhs.adaptors.pss.translator.util;

import java.util.List;
import java.util.Objects;

import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;

public class EhrResourceExtractorUtil {

    public static List<RCMRMT030101UK04EhrComposition> extractEhrCompositionsFromEhrExtract(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .toList();
    }

    public static RCMRMT030101UK04EhrComposition extractEhrCompositionForPlanStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        II resourceId) {
        return extractEhrCompositionsFromEhrExtract(ehrExtract)
            .stream()
            .filter(ehrComposition -> filterForMatchingEhrCompositionPlanStatement(ehrComposition, resourceId))
            .findFirst()
            .get();
    }

    public static RCMRMT030101UK04EhrComposition extractEhrCompositionForObservationStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        II resourceId) {
        return extractEhrCompositionsFromEhrExtract(ehrExtract)
            .stream()
            .filter(ehrComposition -> filterForMatchingEhrCompositionObservationStatement(ehrComposition, resourceId))
            .findFirst()
            .get();
    }

    public static RCMRMT030101UK04EhrComposition extractEhrCompositionForCompoundStatement(RCMRMT030101UK04EhrExtract ehrExtract,
        II resourceId) {
        return extractEhrCompositionsFromEhrExtract(ehrExtract)
            .stream()
            .filter(ehrComposition -> filterForMatchingEhrCompositionCompoundStatement(ehrComposition, resourceId))
            .findFirst()
            .get();
    }

    public static List<RCMRMT030101UK04EhrComposition> getCompositionsContainingCompoundStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().stream()
            .flatMap(component -> component.getEhrFolder().getComponent().stream())
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .map(RCMRMT030101UK04Component4::getCompoundStatement)
                .anyMatch(Objects::nonNull))
            .toList();
    }

    public static List<RCMRMT030101UK04ObservationStatement> getObservationStatementsFromCompoundStatement(
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent().stream()
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .filter(Objects::nonNull)
            .toList();
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
