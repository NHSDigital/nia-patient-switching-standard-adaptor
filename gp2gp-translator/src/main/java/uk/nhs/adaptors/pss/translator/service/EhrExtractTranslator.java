package uk.nhs.adaptors.pss.translator.service;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallString;

import javax.xml.bind.JAXBException;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.generator.BundleGenerator;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractTranslator {
    private BundleGenerator bundleGenerator;

    public Bundle translateEhrToFhirBundle(String message) {
        var unmarshalledMessage = unmarshalMessage(message);
        Bundle bundle = bundleGenerator.generateBundle();
        //later add other resources
        return bundle;
    }

    private RCMRIN030000UK06Message unmarshalMessage(String message) {
        try {
            return unmarshallString(message, RCMRIN030000UK06Message.class);
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
}