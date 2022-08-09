package uk.nhs.adaptors.pss.translator.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "supported-filetypes")
public class SupportedFileTypes {

    private Set<String> accepted;

}
