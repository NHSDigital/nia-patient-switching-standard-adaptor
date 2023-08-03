package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Quantity.QuantityComparator;
import org.hl7.v3.IVLPQ;
import org.hl7.v3.PQ;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;
import org.junit.jupiter.api.TestInstance;
import uk.nhs.adaptors.pss.translator.util.MeasurementUnitsUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class QuantityMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Quantity/";
    private static final String UNIT_SYSTEM = "http://unitsofmeasure.org";

    private final QuantityMapper quantityMapper = new QuantityMapper();

    private static final MeasurementUnitsUtil MEASUREMENT_UNITS_UTIL = new MeasurementUnitsUtil();

    private Method getCreateMeasurementUnitsMethod() throws NoSuchMethodException {
        Method method = MeasurementUnitsUtil.class.getDeclaredMethod("createMeasurementUnits");
        method.setAccessible(true);
        return method;
    }

    @BeforeAll
    public void createMeasurementUnits() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        getCreateMeasurementUnitsMethod().invoke(MEASUREMENT_UNITS_UTIL);
    }

    @Test
    public void mapQuantityNoTypeStandardUnit() {
        var observationStatement = unmarshallObservationStatement("no_type_standard_unit.xml");
        var value = observationStatement.getValue();

        Quantity quantity = quantityMapper.mapValueQuantity((PQ) value);

        assertQuantity(quantity, "100", "kilogram per square meter", UNIT_SYSTEM, "Kg/m2", null);
    }

    @Test
    public void mapQuantityNoTypeArbitraryUnit() {
        var observationStatement = unmarshallObservationStatement("no_type_arbitrary_unit.xml");
        var value = observationStatement.getValue();

        Quantity quantity = quantityMapper.mapValueQuantity((PQ) value);

        assertQuantity(quantity, "100", "kua/L", null, null, null);
    }

    @Test
    public void mapQuantityPqStandardUnit() {
        var value = unmarshallValueElementForPQ("pq_standard_unit.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kilogram per square meter", UNIT_SYSTEM, "Kg/m2", null);
    }

    @Test
    public void mapQuantityPqArbitraryUnit() {
        var value = unmarshallValueElementForPQ("pq_arbitrary_unit.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kua/L", null, null, null);
    }

    @Test
    public void mapQuantityIvlPqHighStandardUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_high_standard_unit.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kilogram per square meter", UNIT_SYSTEM, "Kg/m2", QuantityComparator.LESS_THAN);
    }

    @Test
    public void mapQuantityIvlPqHighArbitraryUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_high_arbitrary_unit.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kua/L", null, null, QuantityComparator.LESS_THAN);
    }

    @Test
    public void mapQuantityIvlPqLowStandardUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_low_standard_unit.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kilogram per square meter", UNIT_SYSTEM, "Kg/m2", QuantityComparator.GREATER_THAN);
    }

    @Test
    public void mapQuantityIvlPqLowArbitraryUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_low_arbitrary_unit.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kua/L", null, null, QuantityComparator.GREATER_THAN);
    }

    @Test
    public void mapQuantityIvlPqHighInclusive() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_high_inclusive.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kilogram per square meter", UNIT_SYSTEM, "Kg/m2", QuantityComparator.LESS_OR_EQUAL);
    }

    @Test
    public void mapQuantityIvlPqHighExclusive() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_high_exclusive.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kilogram per square meter", UNIT_SYSTEM, "Kg/m2", QuantityComparator.LESS_THAN);
    }

    @Test
    public void mapQuantityIvlPqLowInclusive() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_low_inclusive.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kilogram per square meter", UNIT_SYSTEM, "Kg/m2", QuantityComparator.GREATER_OR_EQUAL);
    }

    @Test
    public void mapQuantityIvlPqLowExclusive() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_low_exclusive.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "kilogram per square meter", UNIT_SYSTEM, "Kg/m2", QuantityComparator.GREATER_THAN);
    }

    @Test
    public void mapQuantityNoTypeNoUnit() {
        var observationStatement = unmarshallObservationStatement("no_type_no_unit.xml");
        var value = observationStatement.getValue();

        Quantity quantity = quantityMapper.mapValueQuantity((PQ) value);

        assertQuantity(quantity, "100", "1", UNIT_SYSTEM, "1", null);
    }

    @Test
    public void mapQuantityPqNoUnit() {
        var value = unmarshallValueElementForPQ("pq_no_unit.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "1", UNIT_SYSTEM, "1", null);
    }

    @Test
    public void mapQuantityIvlPqNoUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_no_unit.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", null, null, null, QuantityComparator.LESS_THAN);
    }

    @Test
    public void mapQuantityUnitIsUnity() {
        var value = unmarshallValueElementForPQ("unit_is_unity.xml");

        Quantity quantity = quantityMapper.mapValueQuantity(value);

        assertQuantity(quantity, "100", "1", UNIT_SYSTEM, "1", null);
    }
    
    @Test
    public void mapQuantityPqMultipleKeysDifferingOnlyByCase() {
        var uppercaseValue = unmarshallValueElementForPQ("pq_arbitrary_unit_uppercase_s.xml");
        var lowercaseValue = unmarshallValueElementForPQ("pq_arbitrary_unit_lowercase_s.xml");

        Quantity uppercaseQuantity = quantityMapper.mapValueQuantity(uppercaseValue);
        Quantity lowercaseQuantity = quantityMapper.mapValueQuantity(lowercaseValue);

        assertQuantity(uppercaseQuantity, "10", "Siemens", null, "S", null);
        assertQuantity(lowercaseQuantity, "10", "second", null, "s", null);
    }

    private void assertQuantity(
        Quantity quantity,
        String value,
        String unit,
        String system,
        String code,
        QuantityComparator comparator) {
        assertThat(quantity.getValue()).isEqualTo(value);
        assertThat(quantity.getUnit()).isEqualTo(unit);
        assertThat(quantity.getSystem()).isEqualTo(system);
        assertThat(quantity.getCode()).isEqualTo(code);
        assertThat(quantity.getComparator()).isEqualTo(comparator);
    }

    @SneakyThrows
    private PQ unmarshallValueElementForPQ(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), PQ.class);
    }
    @SneakyThrows
    private IVLPQ unmarshallValueElementForIVLPQ(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), IVLPQ.class);
    }

    @SneakyThrows
    private RCMRMT030101UK04ObservationStatement unmarshallObservationStatement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04ObservationStatement.class);
    }
}
