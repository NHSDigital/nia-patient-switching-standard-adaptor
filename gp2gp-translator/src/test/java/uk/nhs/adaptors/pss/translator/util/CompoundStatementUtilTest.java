package uk.nhs.adaptors.pss.translator.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
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

    @Test
    public void When_CompoundStatementContainsNestedCompoundStatements_Expect_AllCompoundStatementsExtracted() {
        var compoundStatement = unmarshallCompoundStatement("NestMedicationsCompoundStatement.xml");
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
    public void When_CompoundStatementContainsNestedMedicationStatements_Expect_AllMedicationStatementsExtracted() {
        var compoundStatement = unmarshallCompoundStatement("NestMedicationsCompoundStatement.xml");
        var mappedValues = CompoundStatementUtil
            .extractResourcesFromCompound(compoundStatement,
                RCMRMT030101UK04Component02::hasMedicationStatement,
                RCMRMT030101UK04Component02::getMedicationStatement);

        assertThat(mappedValues.size()).isEqualTo(3);
        mappedValues.forEach(
            value -> assertTrue(value instanceof RCMRMT030101UK04MedicationStatement)
        );

    }

    @SneakyThrows
    private RCMRMT030101UK04CompoundStatement unmarshallCompoundStatement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_COMPOUND_STATEMENTS + fileName),
            RCMRMT030101UK04CompoundStatement.class);
    }
}
