package uk.nhs.adaptors.common.util;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import uk.nhs.adaptors.common.exception.FhirValidationException;

@Service
public class FhirParser {
    public <T extends IBaseResource> T parseResource(String body, Class<T> fhirClass) {
        IParser jsonParser = prepareParser();
        try {
            return jsonParser.parseResource(fhirClass, body);
        } catch (Exception ex) {
            throw new FhirValidationException(ex.getMessage());
        }
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
