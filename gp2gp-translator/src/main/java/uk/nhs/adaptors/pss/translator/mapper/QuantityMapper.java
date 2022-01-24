package uk.nhs.adaptors.pss.translator.mapper;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.v3.IVLPQ;
import org.hl7.v3.PQ;

public class QuantityMapper {
    private static final String UNIT_SYSTEM = "http://unitsofmeasure.org";

    public Quantity mapQuantity(PQ pq) {
        //TODO xsi:type="PQ" or no xsi:type <- simple quantity with no comparator
        Quantity quantity = new Quantity();
        
        quantity.setValue(Long.parseLong(pq.getValue()));
        
        if (StringUtils.isNotBlank(pq.getUnit())) {
            if (StringUtils.isNumeric(pq.getUnit())
                && pq.getTranslation() != null
                && pq.getTranslation().size() > 0) {
                quantity.setUnit(pq.getTranslation().get(0).getOriginalText());
            }
            else {
                quantity.setUnit(pq.getUnit());
                quantity.setSystem(UNIT_SYSTEM);
                quantity.setCode(pq.getUnit());
            }
        }

        return quantity;
    }

    public Quantity mapQuantity(IVLPQ ivlpq) {
        Quantity quantity = new Quantity();
        return null;
    }
}