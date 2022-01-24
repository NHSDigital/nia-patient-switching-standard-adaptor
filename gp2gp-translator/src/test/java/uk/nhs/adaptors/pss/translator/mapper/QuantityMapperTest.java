package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.v3.CD;
import org.hl7.v3.PQ;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class QuantityMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Quantity/";

    private final QuantityMapper quantityMapper = new QuantityMapper();

    @Test
    public void test() {
        var pq = unmarshallCodeElement("test.xml");

        Quantity quantity = quantityMapper.mapQuantity(pq);
    }

    @SneakyThrows
    private PQ unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), PQ.class);
    }
}
