package uk.nhs.adaptors.pss.gpc.service;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;

@Service
public class FhirParser {
    public <T extends IBaseResource> T parseResource(String body, Class<T> fhirClass) {
        IParser jsonParser = prepareParser();
        return jsonParser.parseResource(fhirClass, body);
    }

    public String encodeToJson(IBaseResource resource) {
        IParser jsonParser = prepareParser();
        return jsonParser
            .setPrettyPrint(true)
            .encodeResourceToString(resource);
    }

    private IParser prepareParser() {
        FhirContext ctx = FhirContext.forDstu3();
        ctx.newJsonParser();
        ctx.setParserErrorHandler(new StrictErrorHandler());
        return ctx.newJsonParser();
    }
}
