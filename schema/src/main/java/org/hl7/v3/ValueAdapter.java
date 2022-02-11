package org.hl7.v3;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.w3c.dom.Element;

public class ValueAdapter extends XmlAdapter<Object, Object> {
    @Override
    public Object unmarshal(Object v) {
        if (v instanceof PQ || v instanceof IVLPQ || v instanceof CV) {
            return v;
        }
        Element element = (Element) v;
        return element.getTextContent();
    }

    @Override
    public Object marshal(Object v) {
        return v;
    }
}
