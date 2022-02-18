package uk.nhs.adaptors.common.util.fhir;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.common.exception.FhirValidationException;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FhirParser {

    private final FhirContext context;

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
        context.newJsonParser();
        context.setParserErrorHandler(new StrictErrorHandler());
        return context.newJsonParser();
    }
}
