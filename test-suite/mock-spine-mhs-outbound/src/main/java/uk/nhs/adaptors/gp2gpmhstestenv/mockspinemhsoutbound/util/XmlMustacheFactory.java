package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.util;

import static org.apache.commons.text.StringEscapeUtils.escapeXml10;

import java.io.IOException;
import java.io.Writer;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheException;

public class XmlMustacheFactory extends DefaultMustacheFactory {
    public XmlMustacheFactory(String resourceRoot) {
        super(resourceRoot);
    }

    @Override
    public void encode(String value, Writer writer) {
        try {
            writer.write(escapeXml10(value));
        } catch (IOException e) {
            throw new MustacheException("Failed to encode value: " + value, e);
        }
    }
}
