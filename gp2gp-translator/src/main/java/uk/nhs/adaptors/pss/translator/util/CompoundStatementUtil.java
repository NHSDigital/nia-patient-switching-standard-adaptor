package uk.nhs.adaptors.pss.translator.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;

public class CompoundStatementUtil {

    public static List<RCMRMT030101UK04CompoundStatement> extractCompoundsFromCompound(
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component02::hasCompoundStatement)
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .flatMap(CompoundStatementUtil::flattenCompounds)
            .toList();
    }

    public static Stream<RCMRMT030101UK04CompoundStatement> flattenCompounds(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return Stream.concat(
            Stream.of(compoundStatement),
            compoundStatement.getComponent()
                .stream()
                .filter(RCMRMT030101UK04Component02::hasCompoundStatement)
                .map(RCMRMT030101UK04Component02::getCompoundStatement)
        );
    }

    public static List<RCMRMT030101UK04Component02> extractResourcesFromCompound(
            RCMRMT030101UK04CompoundStatement compoundStatement,
            Function<RCMRMT030101UK04Component02, Boolean> checker
    ) {
        return compoundStatement
            .getComponent()
            .stream()
            .flatMap(CompoundStatementUtil::flatten)
            .filter(checker::apply)
            .toList();
    }

    public static List<?> extractResourcesFromCompound(
            RCMRMT030101UK04CompoundStatement compoundStatement,
            Function<RCMRMT030101UK04Component02, Boolean> checker,
            Function<RCMRMT030101UK04Component02, ?> extractor
    ) {
        return extractResourcesFromCompound(compoundStatement, checker)
            .stream()
            .map(extractor)
            .toList();
    }

    public static List<?> extractResourcesFromCompound(
            RCMRMT030101UK04CompoundStatement compoundStatement,
            Function<RCMRMT030101UK04Component02, Boolean> checker,
            Function<RCMRMT030101UK04Component02, ?> extractor,
            Function<RCMRMT030101UK04CompoundStatement, Boolean> compoundStatementChecker
    ) {

        if (compoundStatementChecker.apply(compoundStatement)) {
            return compoundStatement
                .getComponent()
                .stream()
                .flatMap(component02 -> flatten(component02, compoundStatementChecker))
                .filter(checker::apply)
                .map(extractor)
                .toList();
        }

        return List.of();
    }

    private static Stream<RCMRMT030101UK04Component02> flatten(RCMRMT030101UK04Component02 component02) {
        return Stream.concat(
            Stream.of(component02),
            component02.hasCompoundStatement()
                    ? component02
                    .getCompoundStatement()
                    .getComponent()
                    .stream()
                    .flatMap(CompoundStatementUtil::flatten)
                    : Stream.empty()
        );
    }

    private static Stream<RCMRMT030101UK04Component02> flatten(
            RCMRMT030101UK04Component02 component02,
            Function<RCMRMT030101UK04CompoundStatement, Boolean> compoundStatementChecker
    ) {
        return Stream.concat(
            Stream.of(component02),
                component02.hasCompoundStatement()
                && compoundStatementChecker.apply(component02.getCompoundStatement())
                ?   component02
                    .getCompoundStatement()
                    .getComponent()
                    .stream()
                    .flatMap(component -> CompoundStatementUtil.flatten(component, compoundStatementChecker))
                : Stream.empty()
        );
    }
}
