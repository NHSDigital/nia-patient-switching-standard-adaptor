package uk.nhs.adaptors.pss.translator.util;

import java.util.stream.Stream;

import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;

public class CompoundStatementResourceExtractors {
    private static final String ALLERGY_SNOMED_CODE = "2.16.840.1.113883.2.1.6.2";

    public static Stream<RCMRMT030101UK04CompoundStatement> extractAllCompoundStatements(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getCompoundStatement()),
            component4.hasCompoundStatement()
                ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasCompoundStatement, RCMRMT030101UK04Component02::getCompoundStatement)
                .stream()
                .map(RCMRMT030101UK04CompoundStatement.class::cast)
                : Stream.empty()
        );
    }

    public static Stream<RCMRMT030101UK04LinkSet> extractAllLinkSets(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getLinkSet()),
            component4.hasCompoundStatement()
                ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasLinkSet, RCMRMT030101UK04Component02::getLinkSet)
                .stream()
                .map(RCMRMT030101UK04LinkSet.class::cast)
                : Stream.empty()
        );
    }

    public static Stream<RCMRMT030101UK04ObservationStatement> extractAllObservationStatements(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getObservationStatement()),
            component4.hasCompoundStatement() ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasObservationStatement, RCMRMT030101UK04Component02::getObservationStatement)
                .stream()
                .map(RCMRMT030101UK04ObservationStatement.class::cast)
                : Stream.empty()
        );
    }

    public static Stream<RCMRMT030101UK04ObservationStatement> extractAllObservationStatementsWithoutAllergies(
        RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getObservationStatement()),
            component4.hasCompoundStatement()
                ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasObservationStatement, RCMRMT030101UK04Component02::getObservationStatement,
                    CompoundStatementResourceExtractors::isNotAllergy)
                .stream()
                .map(RCMRMT030101UK04ObservationStatement.class::cast)
                : Stream.empty()
        );
    }

    public static Stream<RCMRMT030101UK04PlanStatement> extractAllPlanStatements(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getPlanStatement()),
            component4.hasCompoundStatement()
                ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasPlanStatement, RCMRMT030101UK04Component02::getPlanStatement)
                .stream()
                .map(RCMRMT030101UK04PlanStatement.class::cast)
                : Stream.empty()
        );
    }

    public static Stream<RCMRMT030101UK04RequestStatement> extractAllRequestStatements(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getRequestStatement()),
            component4.hasCompoundStatement()
                ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasRequestStatement, RCMRMT030101UK04Component02::getRequestStatement)
                .stream()
                .map(RCMRMT030101UK04RequestStatement.class::cast)
                : Stream.empty()
        );
    }

    public static Stream<RCMRMT030101UK04NarrativeStatement> extractAllNarrativeStatements(RCMRMT030101UK04Component4 component4) {
        return Stream.concat(
            Stream.of(component4.getNarrativeStatement()),
            component4.hasCompoundStatement() ? CompoundStatementUtil.extractResourcesFromCompound(component4.getCompoundStatement(),
                    RCMRMT030101UK04Component02::hasNarrativeStatement, RCMRMT030101UK04Component02::getNarrativeStatement)
                .stream()
                .map(RCMRMT030101UK04NarrativeStatement.class::cast)
                : Stream.empty()
        );
    }

    private static boolean isNotAllergy(RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (compoundStatement.hasCode() && compoundStatement.getCode().hasCodeSystem()) {
            return !ALLERGY_SNOMED_CODE.equals(compoundStatement.getCode().getCodeSystem());
        }
        return true;
    }
}
