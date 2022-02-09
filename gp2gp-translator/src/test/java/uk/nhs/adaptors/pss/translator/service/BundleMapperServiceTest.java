package uk.nhs.adaptors.pss.translator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.RCMRIN030000UK06Message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;
import uk.nhs.adaptors.pss.translator.generator.BundleGenerator;

@ExtendWith(MockitoExtension.class)
public class BundleMapperServiceTest {

    private final String XML_RESOURCES_BASE = "xml/RCMRIN030000UK06/";

    @Mock
    private BundleGenerator bundleGenerator;

    @InjectMocks
    private BundleMapperService bundleMapperService;

    @Test
    public void testAnything() {
        when(bundleGenerator.generateBundle()).thenReturn(new Bundle());
        RCMRIN030000UK06Message xml = unmarshallCodeElement("structuredRecord.xml");
        Bundle bundle = bundleMapperService.mapToBundle(xml);
        assertThat(bundle.getEntry()).isEmpty();
    }

    @SneakyThrows
    private RCMRIN030000UK06Message unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRIN030000UK06Message.class);
    }
}
