package uk.nhs.adaptors.pss.translator.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.v3.deprecated.RCMRMT030101UKComponent02;
import org.hl7.v3.deprecated.RCMRMT030101UKCompoundStatement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompoundStatementUtil {

    public static List<RCMRMT030101UKCompoundStatement> extractCompoundsFromCompound(
        RCMRMT030101UKCompoundStatement compoundStatement) {

        return compoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UKComponent02::hasCompoundStatement)
            .map(RCMRMT030101UKComponent02::getCompoundStatement)
            .flatMap(CompoundStatementUtil::flattenCompounds)
            .toList();
    }

    public static Stream<RCMRMT030101UKCompoundStatement> flattenCompounds(RCMRMT030101UKCompoundStatement compoundStatement) {
        return Stream.concat(
            Stream.of(compoundStatement),
            compoundStatement.getComponent()
                .stream()
                .filter(RCMRMT030101UKComponent02::hasCompoundStatement)
                .map(RCMRMT030101UKComponent02::getCompoundStatement)
        );
    }

    public static List<RCMRMT030101UKComponent02> extractResourcesFromCompound(
            RCMRMT030101UKCompoundStatement compoundStatement,
            Function<RCMRMT030101UKComponent02, Boolean> checker
    ) {
        return compoundStatement
            .getComponent()
            .stream()
            .flatMap(CompoundStatementUtil::flatten)
            .filter(checker::apply)
            .toList();
    }

    public static List<?> extractResourcesFromCompound(
            RCMRMT030101UKCompoundStatement compoundStatement,
            Function<RCMRMT030101UKComponent02, Boolean> checker,
            Function<RCMRMT030101UKComponent02, ?> extractor
    ) {
        return extractResourcesFromCompound(compoundStatement, checker)
            .stream()
            .map(extractor)
            .toList();
    }

    public static List<?> extractResourcesFromCompound(
            RCMRMT030101UKCompoundStatement compoundStatement,
            Function<RCMRMT030101UKComponent02, Boolean> checker,
            Function<RCMRMT030101UKComponent02, ?> extractor,
            Function<RCMRMT030101UKCompoundStatement, Boolean> compoundStatementChecker
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

    private static Stream<RCMRMT030101UKComponent02> flatten(RCMRMT030101UKComponent02 component02) {
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

    private static Stream<RCMRMT030101UKComponent02> flatten(
            RCMRMT030101UKComponent02 component02,
            Function<RCMRMT030101UKCompoundStatement, Boolean> compoundStatementChecker
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
