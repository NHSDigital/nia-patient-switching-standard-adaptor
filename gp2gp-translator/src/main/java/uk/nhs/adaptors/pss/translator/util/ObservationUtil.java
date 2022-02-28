package uk.nhs.adaptors.pss.translator.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
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
import org.hl7.v3.RCMRMT030101UK04Author;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04InterpretationRange;
import org.hl7.v3.RCMRMT030101UK04ReferenceRange;
import org.hl7.v3.TS;

import uk.nhs.adaptors.common.util.CodeableConceptUtils;
import uk.nhs.adaptors.pss.translator.mapper.QuantityMapper;

public class ObservationUtil {
    private static final String VALUE_QUANTITY_EXTENSION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-ValueApproximation-1";
    private static final String CODING_SYSTEM = "http://hl7.org/fhir/v2/0078";

    private static final QuantityMapper QUANTITY_MAPPER = new QuantityMapper();

    public static Quantity getValueQuantity(Object value, CV uncertaintyCode) {
        if (isValidValueQuantity(value)) {
            Quantity valueQuantity;
            if (value instanceof PQ) {
                valueQuantity = QUANTITY_MAPPER.mapQuantity((PQ) value);
            } else {
                valueQuantity = QUANTITY_MAPPER.mapQuantity((IVLPQ) value);
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
        if (interpretationCode != null) {
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
        List<RCMRMT030101UK04ReferenceRange> referenceRangeList) {
        var outputReferenceRanges = new ArrayList<Observation.ObservationReferenceRangeComponent>();

        for (RCMRMT030101UK04ReferenceRange referenceRange : referenceRangeList) {
            var referenceRangeComponent = new Observation.ObservationReferenceRangeComponent();
            referenceRangeComponent.setText(referenceRange.getReferenceInterpretationRange().getText());

            var quantity = QUANTITY_MAPPER.mapQuantity(referenceRange.getReferenceInterpretationRange().getValue());

            var referenceInterpretationRange = referenceRange.getReferenceInterpretationRange();
            if (referenceInterpretationRangeHasValue(referenceInterpretationRange)) {
                if (referenceInterpretationRange.getValue() != null) {
                    if (referenceInterpretationRange.getValue().getLow() != null) {
                        referenceRangeComponent.setLow(getSimpleQuantityFromQuantity(quantity,
                            Long.parseLong(referenceRange.getReferenceInterpretationRange().getValue().getLow().getValue())));
                    }

                    if (referenceInterpretationRange.getValue().getHigh() != null) {
                        referenceRangeComponent.setHigh(getSimpleQuantityFromQuantity(quantity,
                            Long.parseLong(referenceRange.getReferenceInterpretationRange().getValue().getHigh().getValue())));
                    }
                }
            }

            outputReferenceRanges.add(referenceRangeComponent);
        }

        return outputReferenceRanges;
    }

    public static InstantType getIssued(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition matchingEhrComposition) {
        if (authorHasValidTimeValue(matchingEhrComposition.getAuthor())) {
            return DateFormatUtil.parseToInstantType(matchingEhrComposition.getAuthor().getTime().getValue());
        }

        if (availabilityTimeHasValue(ehrExtract.getAvailabilityTime())) {
            return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
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

            if (availabilityTimeHasValue(availabilityTime)) {
                if (effectivePeriod.getStart() == null) {
                    if (effectivePeriod.getEnd() != null) {
                        effectivePeriod.setStartElement(DateFormatUtil.parseToDateTimeType(availabilityTime.getValue()));
                    }
                }
            }

            return effectivePeriod;
        } else if (availabilityTimeHasValue(availabilityTime)) {
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

    private static boolean referenceInterpretationRangeHasValue(RCMRMT030101UK04InterpretationRange referenceInterpretationRange) {
        return referenceInterpretationRange != null && referenceInterpretationRange.getValue() != null;
    }

    private static SimpleQuantity getSimpleQuantityFromQuantity(Quantity quantity, long newValue) {
        return (SimpleQuantity) new SimpleQuantity()
            .setValue(newValue)
            .setUnit(quantity.getUnit())
            .setCode(quantity.getCode())
            .setSystem(quantity.getSystem())
            .setComparator(quantity.getComparator());
    }

    private static boolean authorHasValidTimeValue(RCMRMT030101UK04Author author) {
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
