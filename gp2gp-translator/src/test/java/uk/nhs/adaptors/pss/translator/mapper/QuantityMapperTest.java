package uk.nhs.adaptors.pss.translator.mapper;

import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.v3.Value;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class QuantityMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Quantity/";

    private final QuantityMapper quantityMapper = new QuantityMapper();

    @Test
    public void testPq() {
        var value = unmarshallValueElement("test_PQ.xml");

//        Quantity quantity = quantityMapper.mapQuantity(value);
    }

    @Test
    public void testIvlpq() {
        var value = unmarshallValueElement("test_IVL_PQ.xml");

//        Quantity quantity = quantityMapper.mapQuantity(value);
    }

    @SneakyThrows
    private Value unmarshallValueElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), Value.class);
    }
}
