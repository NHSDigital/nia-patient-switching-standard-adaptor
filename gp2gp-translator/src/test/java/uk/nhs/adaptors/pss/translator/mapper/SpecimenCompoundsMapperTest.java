package uk.nhs.adaptors.pss.translator.mapper;

import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class SpecimenCompoundsMapperTest {

    private static final String XML_RESOURCES_BASE = "xml/SpecimenComponents";

    @Mock
    private DateTimeMapper dateTimeMapper;

    @InjectMocks
    private SpecimenMapper specimenMapper;


    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }

}
