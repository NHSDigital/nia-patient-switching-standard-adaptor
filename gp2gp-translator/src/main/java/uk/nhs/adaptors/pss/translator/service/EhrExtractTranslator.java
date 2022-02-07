package uk.nhs.adaptors.pss.translator.service;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.generator.BundleGenerator;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractTranslator {
    private BundleGenerator bundleGenerator;
//    private

    public Bundle translateEhrToFhirBundle(RCMRIN030000UK06Message message) {
        Bundle bundle = bundleGenerator.generateBundle();
//        Patient patient =
        //later add other resources
        return bundle;
    }
}