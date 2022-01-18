package uk.nhs.adaptors.pss.gpc.config.serialization;

import java.io.IOException;

import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import uk.nhs.adaptors.pss.gpc.exception.FhirValidationException;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;

@Component
public class ParametersDeserializer extends JsonDeserializer<Parameters> {
    private final FhirParser fhirParser;

    public ParametersDeserializer(FhirParser fhirParser) {
        super();
        this.fhirParser = fhirParser;
    }

    @Override
    public Parameters deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            return fhirParser.parseResource(p.readValueAsTree().toString(), Parameters.class);
        } catch (JsonParseException ex) {
            throw new FhirValidationException(ex.getMessage());
        }
    }
}
