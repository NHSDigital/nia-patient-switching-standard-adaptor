package uk.nhs.adaptors.pss.translator.mapper;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.CV;
import org.hl7.v3.EnFamily;
import org.hl7.v3.II;
import org.hl7.v3.IVLTS;
import org.hl7.v3.PQR;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04Author;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04Participant;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.hl7.v3.RCMRMT030101UK04ReferenceRange;
import org.hl7.v3.RCMRMT030101UK04Subject;
import org.hl7.v3.TS;
import org.hl7.v3.Value;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@AllArgsConstructor
public class ObservationMapper {
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String VALUE_QUANTITY_EXTENSION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect" +
        "-ValueApproximation-1";
    private static final String ST_VALUE = "ST";
    private static final String CV_VALUE = "CV";
    private static final String SUBJECT_COMMENT = "Subject: %s ";
    private static final String CODING_SYSTEM = "http://hl7.org/fhir/v2/0078";

    private CodeableConceptMapper codeableConceptMapper;

    private QuantityMapper quantityMapper;

    public Observation mapToObservation(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04ObservationStatement observationStatement) {
        //////// TODO: id
        //////// TODO: meta
        //////// TODO: identifier
        //////// TODO: status
        //////// TODO: code
        //////// TODO: subject
        //////// TODO: context
        //////// TODO: effective
        //////// TODO: issued
        //////// TODO: performer
        //////// TODO: valueQuantity
        //////// TODO: valueQuantity.extension
        //////// TODO: valueString ("Negative")
        //////// TODO: valueString ("Female")
        //////// TODO: interpretation
        //////// TODO: comment 1
        //////// TODO: comment 2
        //////// TODO: referenceRange

        var id = observationStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var code = getCode(observationStatement.getCode());
        var effective = getEffective(observationStatement.getEffectiveTime(), observationStatement.getAvailabilityTime());
        var issued = getIssued(ehrExtract, observationStatement.getId());
        var performer = ParticipantReferenceUtil.getParticipantReference(observationStatement.getParticipant(),
            EhrResourceExtractorUtil.extractEhrCompositionForObservationStatement(ehrExtract, observationStatement.getId()));
        var valueQuantity = getValueQuantity(observationStatement.getValue(), observationStatement.getUncertaintyCode());
        var valueString = getValueString(observationStatement.getValue());
        var interpretation = getInterpretation(observationStatement.getInterpretationCode());
        var comment = getComment(observationStatement.getPertinentInformation(), observationStatement.getSubject());
        var referenceRange = getReferenceRange(observationStatement.getReferenceRange());

        /**
         * TODO: Known future implementations to this mapper
         * - subject: references a global patient resource for the transaction (NIAD-2024)
         * - context: references an encounter resource if it has been generated from the ehrComposition (NIAD-2025)
         * - performer: fallback to a default 'Unknown User' Practitioner if none are present in performer (NIAD-2026)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        return createObservation(id, identifier, code, effective, issued, performer, valueQuantity, valueString, interpretation, comment,
            referenceRange);
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
        return identifier;
    }

    private CodeableConcept getCode(CD code) {
        if (code != null) {
            return codeableConceptMapper.mapToCodeableConcept(code);
        }
        return null;
    }

    private Object getEffective(IVLTS effectiveTime, TS availabilityTime) {
        if (effectiveTime != null) {
            if (effectiveTimeHasCenter(effectiveTime)) {
                return DateFormatUtil.parse(effectiveTime.getCenter().getValue());
            }

            var effectivePeriod = new Period();

            if (effectiveTimeHasLow(effectiveTime)) {
                effectivePeriod.setStart(DateFormatUtil.parse(effectiveTime.getLow().getValue()).getValue());
            }

            if (effectiveTimeHasHigh(effectiveTime)) {
                effectivePeriod.setEnd(DateFormatUtil.parse(effectiveTime.getHigh().getValue()).getValue());
            }

            if (availabilityTimeHasValue(availabilityTime)) {
                if (effectivePeriod.getStart() == null) {
                    if (effectivePeriod.getEnd() == null) {
                        return availabilityTime.getValue();
                    }
                }
            }
            
            return effectivePeriod;
        } else if (availabilityTimeHasValue(availabilityTime)) {
            return DateFormatUtil.parse(availabilityTime.getValue());
        }
        return null;
    }

    private boolean effectiveTimeHasCenter(IVLTS effectiveTime) {
        return effectiveTime.getCenter() != null && effectiveTime.getCenter().getValue() != null;
    }

    private boolean effectiveTimeHasLow(IVLTS effectiveTime) {
        return effectiveTime.getLow() != null && effectiveTime.getLow().getValue() != null;
    }

    private boolean effectiveTimeHasHigh(IVLTS effectiveTime) {
        return effectiveTime.getHigh() != null && effectiveTime.getHigh().getValue() != null;
    }

    private boolean availabilityTimeHasValue(TS availabilityTime) {
        return availabilityTime != null && availabilityTime.getValue() != null;
    }

    private InstantType getIssued(RCMRMT030101UK04EhrExtract ehrExtract, II observationStatementId) {
        var ehrComposition = EhrResourceExtractorUtil.extractEhrCompositionForObservationStatement(ehrExtract, observationStatementId);

        if (authorHasValidTimeValue(ehrComposition.getAuthor())) {
            return DateFormatUtil.parseToInstantType(ehrComposition.getAuthor().getTime().getValue());
        }

        if (availabilityTimeHasValue(ehrExtract.getAvailabilityTime())) {
            return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
        }

        return null;
    }

    private boolean authorHasValidTimeValue(RCMRMT030101UK04Author author) {
        return author != null && author.getTime() != null && author.getTime().getValue() != null && author.getTime().getNullFlavor() == null;
    }

    private Quantity getValueQuantity(Value value, CV uncertaintyCode) {
        if (value != null) {
            var valueQuantity = quantityMapper.mapQuantity(value);

            if (uncertaintyCode != null) {
                valueQuantity.getExtension().add(new Extension()
                    .setUrl(VALUE_QUANTITY_EXTENSION)
                    .setValue(new BooleanType().setValue(true)));
            }

            return valueQuantity;
        }

        return null;
    }

    private String getValueString(Value value) {
        if (value != null) {
            if (ST_VALUE.equals(value.getType())) {
                return value.getValue();
            }

            if (CV_VALUE.equals(value.getType())) {
                var translation = value.getTranslation();
                if (valueTranslationHasOriginalText(translation)) {
                    return translation.get(0).getOriginalText();
                } else if (valueTranslationHasDisplayName(translation)) {
                    return translation.get(0).getDisplayName();
                }
            }
        }
        return null;
    }

    private boolean valueTranslationHasOriginalText(List<PQR> translation) {
        return !translation.isEmpty() && translation.get(0).getOriginalText() != null;
    }

    private boolean valueTranslationHasDisplayName(List<PQR> translation) {
        return !translation.isEmpty() && translation.get(0).getDisplayName() != null;
    }

    private CodeableConcept getInterpretation(CV interpretationCode) {
        if (interpretationCode != null) {
            var interpretationCodeableConcept = new CodeableConcept();

            var code = interpretationCode.getCode();

            interpretationCodeableConcept.getCoding().add(new Coding()
                .setCode(getInterpretationCodeAbbreviation(code))
                .setDisplay(getInterpretationDisplay(code))
                .setSystem(CODING_SYSTEM));

            if (StringUtils.isNotEmpty(interpretationCode.getOriginalText())) {
                interpretationCodeableConcept.setText(interpretationCode.getOriginalText());
            } else if (StringUtils.isNotEmpty(interpretationCode.getDisplayName())) {
                interpretationCodeableConcept.setText(interpretationCode.getDisplayName());
            }

            return interpretationCodeableConcept;
        }

        return null;
    }

    private String getInterpretationCodeAbbreviation(String interpretationCode) {
        switch (interpretationCode) {
            case ("HI"):
                return "H";
            case ("LO"):
                return "L";
            case ("OR"):
                return "A";
        }
        return StringUtils.EMPTY;
    }

    private String getInterpretationDisplay(String interpretationCode) {
        switch (interpretationCode) {
            case ("HI"):
                return "High";
            case ("LO"):
                return "Low";
            case ("OR"):
                return "Abnormal";
        }
        return StringUtils.EMPTY;
    }

    private String getComment(List<RCMRMT030101UK04PertinentInformation02> pertinentInformation, RCMRMT030101UK04Subject subject) {
        StringBuilder stringBuilder = new StringBuilder();

        if (subjectHasOriginalText(subject)) {
            stringBuilder.append(String.format(SUBJECT_COMMENT, subject.getPersonalRelationship().getCode().getOriginalText()));
        } else if (subjectHasDisplayName(subject)) {
            stringBuilder.append(String.format(SUBJECT_COMMENT, subject.getPersonalRelationship().getCode().getDisplayName()));
        }

        stringBuilder.append(pertinentInformation.stream()
            .filter(this::pertinentInformationHasOriginalText)
            .map(RCMRMT030101UK04PertinentInformation02.class::cast)
            .map(RCMRMT030101UK04PertinentInformation02::getPertinentAnnotation)
            .map(RCMRMT030101UK04Annotation::getText)
            .collect(Collectors.joining(StringUtils.SPACE)));

        return stringBuilder.toString();
    }

    private boolean pertinentInformationHasOriginalText(RCMRMT030101UK04PertinentInformation02 pertinentInformation) {
        return pertinentInformation != null && pertinentInformation.getPertinentAnnotation() != null
            && pertinentInformation.getPertinentAnnotation().getText() != null;
    }

    private boolean subjectHasOriginalText(RCMRMT030101UK04Subject subject) {
        return subject != null && subject.getPersonalRelationship() != null
            && subject.getPersonalRelationship().getCode() != null && subject.getPersonalRelationship().getCode().getOriginalText() != null;
    }

    private boolean subjectHasDisplayName(RCMRMT030101UK04Subject subject) {
        return subject != null && subject.getPersonalRelationship() != null
            && subject.getPersonalRelationship().getCode() != null && subject.getPersonalRelationship().getCode().getDisplayName() != null;
    }

    private ObservationReferenceRangeComponent getReferenceRange(List<RCMRMT030101UK04ReferenceRange> referenceRangeList) {
        var referenceRange = referenceRangeList.stream().findFirst();

        if (referenceRange.isPresent()) {
            var range = referenceRange.get();

            var referenceRangeComponent = new ObservationReferenceRangeComponent();
            referenceRangeComponent.setText(range.getReferenceInterpretationRange().getText().toString());

            var quantity = quantityMapper.mapQuantity(range.getReferenceInterpretationRange().getValue());

            if (range.getReferenceInterpretationRange().getValue().getLow() != null) {
                referenceRangeComponent.setLow(getSimpleQuantityFromQuantity(quantity,
                    Long.parseLong(range.getReferenceInterpretationRange().getValue().getLow().getValue())));
            }

            if (range.getReferenceInterpretationRange().getValue().getHigh() != null) {
                referenceRangeComponent.setHigh(getSimpleQuantityFromQuantity(quantity,
                    Long.parseLong(range.getReferenceInterpretationRange().getValue().getHigh().getValue())));
            }

            return referenceRangeComponent;
        }

        return null;
    }

    private SimpleQuantity getSimpleQuantityFromQuantity(Quantity quantity, long newValue) {
        return (SimpleQuantity) new SimpleQuantity()
            .setValue(newValue)
            .setUnit(quantity.getUnit())
            .setCode(quantity.getCode())
            .setSystem(quantity.getSystem())
            .setComparator(quantity.getComparator());
    }

    private Observation createObservation(String id, Identifier identifier, CodeableConcept code, Object effective, InstantType issued,
        Reference performer, Quantity valueQuantity, String valueString, CodeableConcept interpretation, String comment,
        ObservationReferenceRangeComponent referenceRange) {
        var observation = new Observation();
        
        observation.setId(id);
        observation.getMeta().getProfile().add(new UriType(META_PROFILE));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.getIdentifier().add(identifier);
        observation.setCode(code);
        observation.setIssuedElement(issued);
        observation.getPerformer().add(performer);
        observation.setInterpretation(interpretation);
        observation.setComment(comment);
        observation.getReferenceRange().add(referenceRange);
        
        if (valueQuantity != null) {
            observation.setValue(valueQuantity);
        } else if (StringUtils.isNotEmpty(valueString)) {
            observation.setValue(new StringType().setValue(valueString));
        }
        
        if (effective instanceof DateTimeType) {
            observation.setEffective((DateTimeType) effective);
        } else if (effective instanceof Period) {
            observation.setEffective((Period) effective);
        }

        return observation;
    }
}
