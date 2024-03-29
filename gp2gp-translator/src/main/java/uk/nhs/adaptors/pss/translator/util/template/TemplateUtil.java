package uk.nhs.adaptors.pss.translator.util.template;

import java.io.IOException;
import java.io.StringWriter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateUtil {

    private static final String TEMPLATES_DIRECTORY = "templates";

    public static Mustache loadTemplate(String templateName) {
        MustacheFactory mustacheFactory = new XmlMustacheFactory(TEMPLATES_DIRECTORY);
        return mustacheFactory.compile(templateName);
    }

    public static String fillTemplate(Mustache template, Object content) {
        StringWriter writer = new StringWriter();
        String data = StringUtils.EMPTY;

        try {
            template.execute(writer, content).flush();
            data += writer.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        return data;
    }
}
