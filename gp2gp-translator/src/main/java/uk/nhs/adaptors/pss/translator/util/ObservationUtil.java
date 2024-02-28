package uk.nhs.adaptors.pss.translator.util;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.v3.CV;
import org.hl7.v3.IVLPQ;
import org.hl7.v3.IVLTS;
import org.hl7.v3.PQ;
import org.hl7.v3.RCMRMT030101UKAuthor;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKInterpretationRange;
import org.hl7.v3.RCMRMT030101UKReferenceRange;
import org.hl7.v3.TS;

import org.springframework.stereotype.Component;
import uk.nhs.adaptors.common.util.CodeableConceptUtils;
import uk.nhs.adaptors.pss.translator.mapper.QuantityMapper;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObservationUtil {

    private static final String VALUE_QUANTITY_EXTENSION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-ValueApproximation-1";
    private static final String CODING_SYSTEM = "http://hl7.org/fhir/v2/0078";

    private static final QuantityMapper QUANTITY_MAPPER = new QuantityMapper();

    public static Quantity getValueQuantity(Object value, CV uncertaintyCode) {
        if (isValidValueQuantity(value)) {
            Quantity valueQuantity;
            if (value instanceof PQ pqValue) {
                valueQuantity = QUANTITY_MAPPER.mapValueQuantity(pqValue);
            } else {
                valueQuantity = QUANTITY_MAPPER.mapValueQuantity((IVLPQ) value);
            }

            if (uncertaintyCode != null) {
                valueQuantity.getExtension().add(new Extension()
                    .setUrl(VALUE_QUANTITY_EXTENSION)
                    .setValue(new BooleanType().setValue(true)));
            }

            return valueQuantity;
        }

        return null;
    }

    public static CodeableConcept getInterpretation(CV interpretationCode) {
        if (interpretationCode != null && !interpretationCode.hasNullFlavor()) {
            var code = interpretationCode.getCode();
            String text = null;
            if (StringUtils.isNotEmpty(interpretationCode.getOriginalText())) {
                text = interpretationCode.getOriginalText();
            } else if (StringUtils.isNotEmpty(interpretationCode.getDisplayName())) {
                text = interpretationCode.getDisplayName();
            }
            return CodeableConceptUtils.createCodeableConcept(
                getInterpretationCodeAbbreviation(code),
                CODING_SYSTEM,
                getInterpretationDisplay(code),
                text
            );
        }
        return null;
    }

    public static List<Observation.ObservationReferenceRangeComponent> getReferenceRange(
        List<RCMRMT030101UKReferenceRange> referenceRangeList) {

        var outputReferenceRanges = new ArrayList<Observation.ObservationReferenceRangeComponent>();

        for (RCMRMT030101UKReferenceRange referenceRange : referenceRangeList) {
            var referenceRangeComponent = new Observation.ObservationReferenceRangeComponent();
            referenceRangeComponent.setText(referenceRange.getReferenceInterpretationRange().getText());

            var quantity = QUANTITY_MAPPER.mapReferenceRangeQuantity(
                                                    referenceRange.getReferenceInterpretationRange().getValue());

            var referenceInterpretationRange = referenceRange.getReferenceInterpretationRange();
            if (referenceInterpretationRangeHasValue(referenceInterpretationRange) && referenceInterpretationRange.getValue() != null) {
                if (referenceInterpretationRange.getValue().getLow() != null) {
                    referenceRangeComponent.setLow(getSimpleQuantityFromQuantity(quantity,
                        referenceRange.getReferenceInterpretationRange().getValue().getLow().getValue()));
                }

                if (referenceInterpretationRange.getValue().getHigh() != null) {
                    referenceRangeComponent.setHigh(getSimpleQuantityFromQuantity(quantity,
                        referenceRange.getReferenceInterpretationRange().getValue().getHigh().getValue()));
                }
            }

            outputReferenceRanges.add(referenceRangeComponent);
        }

        return outputReferenceRanges;
    }

    public static InstantType getIssued(RCMRMT030101UKEhrComposition matchingEhrComposition) {

        if (authorHasValidTimeValue(matchingEhrComposition.getAuthor())) {
            return DateFormatUtil.parseToInstantType(matchingEhrComposition.getAuthor().getTime().getValue());
        }

        return null;
    }

    public static Object getEffective(IVLTS effectiveTime, TS availabilityTime) {
        if (effectiveTime != null) {
            if (effectiveTimeHasCenter(effectiveTime)) {
                return DateFormatUtil.parseToDateTimeType(effectiveTime.getCenter().getValue());
            }

            var effectivePeriod = new Period();

            if (effectiveTimeHasLow(effectiveTime)) {
                effectivePeriod.setStartElement(DateFormatUtil.parseToDateTimeType(effectiveTime.getLow().getValue()));
            }

            if (effectiveTimeHasHigh(effectiveTime)) {
                effectivePeriod.setEndElement(DateFormatUtil.parseToDateTimeType(effectiveTime.getHigh().getValue()));
            }

            if (availabilityTimeHasValue(availabilityTime) && effectivePeriod.getStart() == null && effectivePeriod.getEnd() != null) {
                effectivePeriod.setStartElement(DateFormatUtil.parseToDateTimeType(availabilityTime.getValue()));
            }

            if (effectivePeriod.hasStart() || effectivePeriod.hasEnd()) {
                return effectivePeriod;
            }
        }

        if (availabilityTimeHasValue(availabilityTime)) {
            return DateFormatUtil.parseToDateTimeType(availabilityTime.getValue());
        }
        return null;
    }

    private static boolean isValidValueQuantity(Object value) {
        return value instanceof PQ || value instanceof IVLPQ;
    }

    private static String getInterpretationCodeAbbreviation(String interpretationCode) {
        return switch (interpretationCode) {
            case ("HI") -> "H";
            case ("LO") -> "L";
            case ("OR") -> "A";
            default -> StringUtils.EMPTY;
        };
    }

    private static String getInterpretationDisplay(String interpretationCode) {
        return switch (interpretationCode) {
            case ("HI") -> "High";
            case ("LO") -> "Low";
            case ("OR") -> "Abnormal";
            default -> StringUtils.EMPTY;
        };
    }

    private static boolean referenceInterpretationRangeHasValue(RCMRMT030101UKInterpretationRange referenceInterpretationRange) {
        return referenceInterpretationRange != null && referenceInterpretationRange.getValue() != null;
    }

    private static SimpleQuantity getSimpleQuantityFromQuantity(Quantity quantity, String newValueString) {
        var simpleQuantity = new SimpleQuantity();
        buildValue(simpleQuantity, newValueString);
        return (SimpleQuantity) simpleQuantity
            .setUnit(quantity.getUnit())
            .setCode(quantity.getCode())
            .setSystem(quantity.getSystem())
            .setComparator(quantity.getComparator());
    }

    private static void buildValue(SimpleQuantity simpleQuantity, String newValueString) {
        if (newValueString.contains(".")) {
            simpleQuantity.setValue(Double.parseDouble(newValueString));
        } else {
            simpleQuantity.setValue(Long.parseLong(newValueString));
        }
    }

    private static boolean authorHasValidTimeValue(RCMRMT030101UKAuthor author) {
        return author != null && author.getTime() != null
            && author.getTime().getValue() != null
            && author.getTime().getNullFlavor() == null;
    }

    private static boolean timeHasNoNullFlavor(TS time) {
        return time.getNullFlavor() == null;
    }

    private static boolean effectiveTimeHasCenter(IVLTS effectiveTime) {
        return effectiveTime.getCenter() != null && effectiveTime.getCenter().getValue() != null
            && timeHasNoNullFlavor(effectiveTime.getCenter());
    }

    private static boolean effectiveTimeHasLow(IVLTS effectiveTime) {
        return effectiveTime.getLow() != null && effectiveTime.getLow().getValue() != null && timeHasNoNullFlavor(effectiveTime.getLow());
    }

    private static boolean effectiveTimeHasHigh(IVLTS effectiveTime) {
        return effectiveTime.getHigh() != null && effectiveTime.getHigh().getValue() != null
            && timeHasNoNullFlavor(effectiveTime.getHigh());
    }

    private static boolean availabilityTimeHasValue(TS availabilityTime) {
        return availabilityTime != null && availabilityTime.getValue() != null && timeHasNoNullFlavor(availabilityTime);
    }
}
