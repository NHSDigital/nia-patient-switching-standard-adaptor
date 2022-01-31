package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Quantity.QuantityComparator;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.hl7.v3.Value;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class ReferralRequestMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/RequestStatement/";

    private final ReferralRequestMapper referralRequestMapper = new ReferralRequestMapper();

    @SneakyThrows
    private RCMRMT030101UK04RequestStatement unmarshallValueElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04RequestStatement.class);
    }
    
    @Test
    public void test(){
        var value = unmarshallValueElement("test.xml");

        ReferralRequest referralRequest = referralRequestMapper.mapToReferralRequest(value);
    }
}
