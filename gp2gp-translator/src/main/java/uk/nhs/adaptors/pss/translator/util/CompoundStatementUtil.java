package uk.nhs.adaptors.pss.translator.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;

public class CompoundStatementUtil {

    public static List<RCMRMT030101UK04Component02> extractResourcesFromCompound(RCMRMT030101UK04CompoundStatement compoundStatement,
        Function<RCMRMT030101UK04Component02, Boolean> checker) {
        return compoundStatement
            .getComponent()
            .stream()
            .flatMap(CompoundStatementUtil::flatten)
            .filter(checker::apply)
            .collect(Collectors.toList());
    }

    public static List<?> extractResourcesFromCompound(RCMRMT030101UK04CompoundStatement compoundStatement,
        Function<RCMRMT030101UK04Component02, Boolean> checker, Function<RCMRMT030101UK04Component02, ?> extractor) {
        return extractResourcesFromCompound(compoundStatement, checker)
            .stream()
            .map(extractor)
            .toList();
    }

    private static Stream<RCMRMT030101UK04Component02> flatten(RCMRMT030101UK04Component02 component02) {
        return Stream.concat(
            Stream.of(component02),
            component02.hasCompoundStatement() ? component02.getCompoundStatement().getComponent().stream().flatMap(CompoundStatementUtil::flatten) : Stream.empty()
        );
    }
}
