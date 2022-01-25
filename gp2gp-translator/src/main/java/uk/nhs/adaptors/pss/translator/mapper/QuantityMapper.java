package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.v3.PQR;
import org.hl7.v3.Value;

public class QuantityMapper {
    private static final String UNIT_SYSTEM = "http://unitsofmeasure.org";
    private static final String TYPE_IVL_PQ = "IVL_PQ";
    

    public Quantity mapQuantity(Value value) {
        Quantity quantity = new Quantity();
        
        if (TYPE_IVL_PQ.equals(value.getType())) {
            if (value.getHigh() != null) {
                if (value.getHigh().isInclusive()){
                    quantity.setComparator(Quantity.QuantityComparator.LESS_OR_EQUAL);
                }
                else {
                    quantity.setComparator(Quantity.QuantityComparator.LESS_THAN);
                }
                
                quantity.setValue(Long.parseLong(value.getHigh().getValue()));
                setUnit(quantity, value.getHigh().getUnit(), value.getHigh().getTranslation());
            }
            else if (value.getLow() != null) {
                if (value.getLow().isInclusive()){
                    quantity.setComparator(Quantity.QuantityComparator.GREATER_OR_EQUAL);
                }
                else {
                    quantity.setComparator(Quantity.QuantityComparator.GREATER_THAN);
                }
                
                quantity.setValue(Long.parseLong(value.getLow().getValue()));
                setUnit(quantity, value.getLow().getUnit(), value.getLow().getTranslation());
            }
        }
        else {
            quantity.setValue(Long.parseLong(value.getValue()));
            setUnit(quantity, value.getUnit(), value.getTranslation());
        }

        return quantity;
    }
    
    private void setUnit(Quantity quantity, String unit, List<PQR> translation) {
        if (StringUtils.isNotBlank(unit)) {
            if (translation != null
                && translation.size() > 0) {
                quantity.setUnit(translation.get(0).getOriginalText());
            }
            else {
                quantity.setUnit(unit);
                quantity.setSystem(UNIT_SYSTEM);
                quantity.setCode(unit);
            }
        }
    }
}