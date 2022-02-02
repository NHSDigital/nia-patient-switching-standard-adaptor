package uk.nhs.adaptors.pss.gpc.config;

import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import uk.nhs.adaptors.common.config.CommonConfiguration;
import uk.nhs.adaptors.common.util.fhir.FhirParser;
import uk.nhs.adaptors.connector.config.DbConnectorConfiguration;
import uk.nhs.adaptors.pss.gpc.config.serialization.ParametersDeserializer;

@Configuration
@Import({DbConnectorConfiguration.class, CommonConfiguration.class})
public class ApplicationConfiguration {
    @Bean
    public ObjectMapper objectMapper(FhirParser fhirParser) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Parameters.class, new ParametersDeserializer(fhirParser));
        return new ObjectMapper().registerModule(module);
    }

    @Bean
    public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(objectMapper);

        return jacksonConverter;
    }
}
