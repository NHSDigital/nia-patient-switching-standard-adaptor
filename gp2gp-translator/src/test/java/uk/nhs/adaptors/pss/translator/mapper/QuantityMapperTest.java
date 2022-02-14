package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Quantity.QuantityComparator;
import org.hl7.v3.IVLPQ;
import org.hl7.v3.PQ;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class QuantityMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Quantity/";
    private static final String UNIT_SYSTEM = "http://unitsofmeasure.org";

    private final QuantityMapper quantityMapper = new QuantityMapper();

    @Test
    public void mapQuantityNoTypeStandardUnit() {
        var value = unmarshallValueElementForPQ("no_type_standard_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "Kg/m2", UNIT_SYSTEM, "Kg/m2", null);
    }

    @Test
    public void mapQuantityNoTypeArbitraryUnit() {
        var value = unmarshallValueElementForPQ("no_type_arbitrary_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "kua/L", null, null, null);
    }

    @Test
    public void mapQuantityPqStandardUnit() {
        var value = unmarshallValueElementForPQ("pq_standard_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "Kg/m2", UNIT_SYSTEM, "Kg/m2", null);
    }

    @Test
    public void mapQuantityPqArbitraryUnit() {
        var value = unmarshallValueElementForPQ("pq_arbitrary_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "kua/L", null, null, null);
    }

    @Test
    public void mapQuantityIvlPqHighStandardUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_high_standard_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "Kg/m2", UNIT_SYSTEM, "Kg/m2", QuantityComparator.LESS_THAN);
    }

    @Test
    public void mapQuantityIvlPqHighArbitraryUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_high_arbitrary_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "kua/L", null, null, QuantityComparator.LESS_THAN);
    }

    @Test
    public void mapQuantityIvlPqLowStandardUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_low_standard_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "Kg/m2", UNIT_SYSTEM, "Kg/m2", QuantityComparator.GREATER_THAN);
    }

    @Test
    public void mapQuantityIvlPqLowArbitraryUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_low_arbitrary_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "kua/L", null, null, QuantityComparator.GREATER_THAN);
    }

    @Test
    public void mapQuantityIvlPqHighInclusive() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_high_inclusive.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "Kg/m2", UNIT_SYSTEM, "Kg/m2", QuantityComparator.LESS_OR_EQUAL);
    }

    @Test
    public void mapQuantityIvlPqHighExclusive() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_high_exclusive.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "Kg/m2", UNIT_SYSTEM, "Kg/m2", QuantityComparator.LESS_THAN);
    }

    @Test
    public void mapQuantityIvlPqLowInclusive() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_low_inclusive.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "Kg/m2", UNIT_SYSTEM, "Kg/m2", QuantityComparator.GREATER_OR_EQUAL);
    }

    @Test
    public void mapQuantityIvlPqLowExclusive() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_low_exclusive.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "Kg/m2", UNIT_SYSTEM, "Kg/m2", QuantityComparator.GREATER_THAN);
    }

    @Test
    public void mapQuantityNoTypeNoUnit() {
        var observationStatement = unmarshallObservationStatement("no_type_no_unit.xml");
        var value = observationStatement.getValue();

        Quantity quantity = quantityMapper.mapQuantity((PQ) value);

        assertQuantity(quantity, "100", null, null, null, null);
    }

    @Test
    public void mapQuantityPqNoUnit() {
        var value = unmarshallValueElementForPQ("pq_no_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "1", UNIT_SYSTEM, "1", null);
    }

    @Test
    public void mapQuantityIvlPqNoUnit() {
        var value = unmarshallValueElementForIVLPQ("ivlpq_no_unit.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", null, null, null, QuantityComparator.LESS_THAN);
    }

    @Test
    public void mapQuantityUnitIsUnity() {
        var value = unmarshallValueElementForPQ("unit_is_unity.xml");

        Quantity quantity = quantityMapper.mapQuantity(value);

        assertQuantity(quantity, "100", "1", UNIT_SYSTEM, "1", null);
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
