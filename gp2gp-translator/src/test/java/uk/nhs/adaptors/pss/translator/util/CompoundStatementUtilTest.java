package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class CompoundStatementUtilTest {

    private static final String XML_RESOURCES_COMPOUND_STATEMENTS = "xml/CompoundStatement/";

    private static final List<String> EXPECTED_DISPLAYED_NAMES = List.of(
        "1.0",
        "1.1",
        "1.1.1",
        "2.0",
        "2.1",
        "2.1.1",
        "2.1.2",
        "2.1.2.1",
        "2.2"
    );
    private static final int EXPECTED_MEDICATION_STATEMENT_COUNT = 3;
    private static final int EXPECTED_MEDICATION_STATEMENT_MIXED_COUNT = 1;
    private static final int EXPECTED_OBSERVATION_STATEMENT_COUNT = 4;
    private static final int EXPECTED_OBSERVATION_STATEMENT_MIXED_COUNT = 1;
    private static final int EXPECTED_LINKSET_COUNT = 5;
    private static final int EXPECTED_LINKSET_MIXED_COUNT = 2;

    @Test
    public void When_CompoundStatementContainsNestedCompoundStatements_Expect_AllCompoundStatementsExtracted() {
        var compoundStatement = unmarshallCompoundStatement("NestedMedicationsCompoundStatement.xml");
        var mappedValues = CompoundStatementUtil
            .extractResourcesFromCompound(compoundStatement,
                RCMRMT030101UK04Component02::hasCompoundStatement);

        assertThat(mappedValues.size()).isEqualTo(EXPECTED_DISPLAYED_NAMES.size());

        var displayNames = mappedValues
            .stream()
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .map(RCMRMT030101UK04CompoundStatement::getCode)
            .map(CD::getDisplayName)
            .toList();
        assertTrue(EXPECTED_DISPLAYED_NAMES.containsAll(displayNames));
    }

    @Test
    public void When_CompoundStatementContainsMedicationStatements_Expect_AllMedicationStatementsExtracted() {
        var compoundStatement = unmarshallCompoundStatement("NestedMedicationsCompoundStatement.xml");
        var mappedValues = CompoundStatementUtil
            .extractResourcesFromCompound(compoundStatement,
                RCMRMT030101UK04Component02::hasMedicationStatement,
                RCMRMT030101UK04Component02::getMedicationStatement);

        assertThat(mappedValues.size()).isEqualTo(EXPECTED_MEDICATION_STATEMENT_COUNT);
        mappedValues.forEach(
            value -> assertTrue(value instanceof RCMRMT030101UK04MedicationStatement)
        );
    }

    @Test
    public void When_CompoundStatementContainsObservationStatements_Expect_AllMedicationStatementsExtracted() {
        var compoundStatement = unmarshallCompoundStatement("NestedObservationCompoundStatement.xml");
        var mappedValues = CompoundStatementUtil
            .extractResourcesFromCompound(compoundStatement,
                RCMRMT030101UK04Component02::hasObservationStatement,
                RCMRMT030101UK04Component02::getObservationStatement);

        assertThat(mappedValues.size()).isEqualTo(EXPECTED_OBSERVATION_STATEMENT_COUNT);
        mappedValues.forEach(
            value -> assertTrue(value instanceof RCMRMT030101UK04ObservationStatement)
        );
    }

    @Test
    public void When_CompoundStatementContainsLinkSets_Expect_AllLinkSetsExtracted() {
        var compoundStatement = unmarshallCompoundStatement("NestedLinkSetsStatement.xml");
        var mappedValues = CompoundStatementUtil
            .extractResourcesFromCompound(compoundStatement,
                RCMRMT030101UK04Component02::hasLinkSet,
                RCMRMT030101UK04Component02::getLinkSet);

        assertThat(mappedValues.size()).isEqualTo(EXPECTED_LINKSET_COUNT);
        mappedValues.forEach(
            value -> assertTrue(value instanceof RCMRMT030101UK04LinkSet)
        );
    }

    @Test
    public void When_CompoundStatementContainsMixedResource_Expect_AllResourceExtracted() {
        var compoundStatement = unmarshallCompoundStatement("NestedResourcesStatement.xml");
        var mappedValuesLinkSet = CompoundStatementUtil
            .extractResourcesFromCompound(compoundStatement,
                RCMRMT030101UK04Component02::hasLinkSet,
                RCMRMT030101UK04Component02::getLinkSet);
        var mappedValuesObservationStatement = CompoundStatementUtil
            .extractResourcesFromCompound(compoundStatement,
                RCMRMT030101UK04Component02::hasObservationStatement,
                RCMRMT030101UK04Component02::getObservationStatement);
        var mappedValuesMedicationStatement = CompoundStatementUtil
            .extractResourcesFromCompound(compoundStatement,
                RCMRMT030101UK04Component02::hasMedicationStatement,
                RCMRMT030101UK04Component02::getMedicationStatement);

        assertThat(mappedValuesLinkSet.size()).isEqualTo(EXPECTED_LINKSET_MIXED_COUNT);
        assertThat(mappedValuesObservationStatement.size()).isEqualTo(EXPECTED_OBSERVATION_STATEMENT_MIXED_COUNT);
        assertThat(mappedValuesMedicationStatement.size()).isEqualTo(EXPECTED_MEDICATION_STATEMENT_MIXED_COUNT);

        mappedValuesLinkSet.forEach(
            linkSet -> assertTrue(linkSet instanceof RCMRMT030101UK04LinkSet)
        );
        mappedValuesObservationStatement.forEach(
            observation -> assertTrue(observation instanceof RCMRMT030101UK04ObservationStatement)
        );
        mappedValuesMedicationStatement.forEach(
            medicationStatement -> assertTrue(medicationStatement instanceof RCMRMT030101UK04MedicationStatement)
        );
    }

    @SneakyThrows
    private RCMRMT030101UK04CompoundStatement unmarshallCompoundStatement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_COMPOUND_STATEMENTS + fileName),
            RCMRMT030101UK04CompoundStatement.class);
    }
}
