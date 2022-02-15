package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil.extractEhrCompositionForObservationStatement;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Observation.ObservationStatus;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.CV;
import org.hl7.v3.II;
import org.hl7.v3.IVLPQ;
import org.hl7.v3.IVLTS;
import org.hl7.v3.PQ;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04Author;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04InterpretationRange;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.hl7.v3.RCMRMT030101UK04ReferenceRange;
import org.hl7.v3.RCMRMT030101UK04Subject;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@AllArgsConstructor
public class ObservationMapper {
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String VALUE_QUANTITY_EXTENSION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-ValueApproximation-1";
    private static final String SUBJECT_COMMENT = "Subject: %s ";
    private static final String CODING_SYSTEM = "http://hl7.org/fhir/v2/0078";

    private CodeableConceptMapper codeableConceptMapper;

    private QuantityMapper quantityMapper;

    public List<Observation> mapObservations(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        /**
         * TODO: Known future implementations to this mapper
         * - subject: references a global patient resource for the transaction (NIAD-2024) <- DONE
         * - context: references an encounter resource if it has been generated from the ehrComposition (NIAD-2025) <- DONE
         * - performer: fallback to a default 'Unknown User' Practitioner if none are present in performer (NIAD-2026)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        var compositionsList = getCompositionsContainingObservationStatement(ehrExtract);

        return compositionsList
            .stream()
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream())
            .map(RCMRMT030101UK04Component4::getObservationStatement)
            .map(observationStatement -> {
                var id = observationStatement.getId().getRoot();

                Observation observation = new Observation()
                    .setStatus(ObservationStatus.FINAL)
                    .addIdentifier(getIdentifier(id))
                    .setCode(getCode(observationStatement.getCode()))
                    .setIssuedElement(getIssued(ehrExtract, observationStatement.getId()))
                    .addPerformer(getParticipantReference(
                        observationStatement.getParticipant(),
                        extractEhrCompositionForObservationStatement(ehrExtract, observationStatement.getId())))
                    .setInterpretation(getInterpretation(observationStatement.getInterpretationCode()))
                    .setComment(getComment(observationStatement.getPertinentInformation(), observationStatement.getSubject()))
                    .setReferenceRange(getReferenceRange(observationStatement.getReferenceRange()))
                    .setSubject(createPatientReference(patient))
                    .addPerformer(getParticipantReference(
                        observationStatement.getParticipant(),
                        extractEhrCompositionForObservationStatement(ehrExtract, observationStatement.getId())
                    ));

                observation.getMeta().getProfile().add(new UriType(META_PROFILE));

                addContext(observation, getEncounterReference(ehrExtract, encounters,
                    getEhrCompositionId(compositionsList, observationStatement).getRoot()));
                addValue(observation, getValueQuantity(observationStatement.getValue(), observationStatement.getUncertaintyCode()),
                    getValueString(observationStatement.getValue()));
                addEffective(observation,
                    getEffective(observationStatement.getEffectiveTime(), observationStatement.getAvailabilityTime()));

                return observation;
            }).toList();
    }

    private II getEhrCompositionId(List<RCMRMT030101UK04EhrComposition> ehrCompositions, RCMRMT030101UK04ObservationStatement observationStatement) {
        return ehrCompositions
            .stream()
            .filter(e -> e.getComponent()
                .stream()
                .anyMatch(f -> observationStatement.equals(f.getObservationStatement()))
            ).findFirst()
            .map(RCMRMT030101UK04EhrComposition::getId)
            .orElse(null);
    }

    private void addContext(Observation observation, Reference context) {
        if(context != null) {
            observation.setContext(context);
        }
    }

    private void addEffective(Observation observation, Object effective) {
        if (effective instanceof DateTimeType) {
            observation.setEffective((DateTimeType) effective);
        } else if (effective instanceof Period) {
            observation.setEffective((Period) effective);
        }
    }

    private void addValue(Observation observation, Quantity valueQuantity, String valueString) {
        if (valueQuantity != null) {
            observation.setValue(valueQuantity);
        } else if (StringUtils.isNotEmpty(valueString)) {
            observation.setValue(new StringType().setValue(valueString));
        }
    }

    private Identifier getIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
    }

    private CodeableConcept getCode(CD code) {
        return code != null ? codeableConceptMapper.mapToCodeableConcept(code) : null;
    }

    private Object getEffective(IVLTS effectiveTime, TS availabilityTime) {
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

    private boolean effectiveTimeHasCenter(IVLTS effectiveTime) {
        return effectiveTime.getCenter() != null && effectiveTime.getCenter().getValue() != null
            && !timeHasNullFlavor(effectiveTime.getCenter());
    }

    private boolean effectiveTimeHasLow(IVLTS effectiveTime) {
        return effectiveTime.getLow() != null && effectiveTime.getLow().getValue() != null && !timeHasNullFlavor(effectiveTime.getLow());
    }

    private boolean effectiveTimeHasHigh(IVLTS effectiveTime) {
        return effectiveTime.getHigh() != null && effectiveTime.getHigh().getValue() != null && !timeHasNullFlavor(effectiveTime.getHigh());
    }

    private boolean availabilityTimeHasValue(TS availabilityTime) {
        return availabilityTime != null && availabilityTime.getValue() != null && !timeHasNullFlavor(availabilityTime);
    }

    private boolean timeHasNullFlavor(TS time) {
        return time.getNullFlavor() != null;
    }

    private InstantType getIssued(RCMRMT030101UK04EhrExtract ehrExtract, II observationStatementId) {
        var ehrComposition = extractEhrCompositionForObservationStatement(ehrExtract, observationStatementId);

        if (authorHasValidTimeValue(ehrComposition.getAuthor())) {
            return DateFormatUtil.parseToInstantType(ehrComposition.getAuthor().getTime().getValue());
        }

        if (availabilityTimeHasValue(ehrExtract.getAvailabilityTime())) {
            return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
        }

        return null;
    }

    private boolean authorHasValidTimeValue(RCMRMT030101UK04Author author) {
        return author != null && author.getTime() != null
            && author.getTime().getValue() != null
            && author.getTime().getNullFlavor() == null;
    }

    private Quantity getValueQuantity(Object value, CV uncertaintyCode) {
        if (isValidValueQuantity(value)) {
            Quantity valueQuantity;
            if (value instanceof PQ) {
                valueQuantity = quantityMapper.mapQuantity((PQ) value);
            } else {
                valueQuantity = quantityMapper.mapQuantity((IVLPQ) value);
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

    private boolean isValidValueQuantity(Object value) {
        return value instanceof PQ || value instanceof IVLPQ;
    }

    private String getValueString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof CV cvValue) {
            return cvValue.getOriginalText() != null ? cvValue.getOriginalText() : cvValue.getDisplayName();
        }
        return null;
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
        return switch (interpretationCode) {
            case ("HI") -> "H";
            case ("LO") -> "L";
            case ("OR") -> "A";
            default -> StringUtils.EMPTY;
        };
    }

    private String getInterpretationDisplay(String interpretationCode) {
        return switch (interpretationCode) {
            case ("HI") -> "High";
            case ("LO") -> "Low";
            case ("OR") -> "Abnormal";
            default -> StringUtils.EMPTY;
        };
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

    private List<ObservationReferenceRangeComponent> getReferenceRange(List<RCMRMT030101UK04ReferenceRange> referenceRangeList) {
        var outputReferenceRanges = new ArrayList<ObservationReferenceRangeComponent>();

        for (RCMRMT030101UK04ReferenceRange referenceRange : referenceRangeList) {
            var referenceRangeComponent = new ObservationReferenceRangeComponent();
            referenceRangeComponent.setText(referenceRange.getReferenceInterpretationRange().getText());


            var quantity = quantityMapper.mapQuantity(referenceRange.getReferenceInterpretationRange().getValue());

            var referenceInterpretationRange = referenceRange.getReferenceInterpretationRange();
            if (referenceInterpretationRangeHasValue(referenceInterpretationRange)) {
                if (referenceInterpretationRange.getValue().getLow() != null) {
                    referenceRangeComponent.setLow(getSimpleQuantityFromQuantity(quantity,
                        Long.parseLong(referenceRange.getReferenceInterpretationRange().getValue().getLow().getValue())));
                }

                if (referenceInterpretationRange.getValue().getHigh() != null) {
                    referenceRangeComponent.setHigh(getSimpleQuantityFromQuantity(quantity,
                        Long.parseLong(referenceRange.getReferenceInterpretationRange().getValue().getHigh().getValue())));
                }
            }

            outputReferenceRanges.add(referenceRangeComponent);
        }

        return outputReferenceRanges;
    }

    private boolean referenceInterpretationRangeHasValue(RCMRMT030101UK04InterpretationRange referenceInterpretationRange) {
        return referenceInterpretationRange != null && referenceInterpretationRange.getValue() != null;
    }

    private SimpleQuantity getSimpleQuantityFromQuantity(Quantity quantity, long newValue) {
        return (SimpleQuantity) new SimpleQuantity()
            .setValue(newValue)
            .setUnit(quantity.getUnit())
            .setCode(quantity.getCode())
            .setSystem(quantity.getSystem())
            .setComparator(quantity.getComparator());
    }

    private Reference createPatientReference(Patient patient) {
        return new Reference(patient);
    }

    private Reference getEncounterReference(RCMRMT030101UK04EhrExtract ehrExtract, List<Encounter> encounterList, String ehrCompositionId) {
        return getCompositionsContainingObservationStatement(ehrExtract)
            .stream()
            .map(component3 -> encounterList
                .stream()
                .filter(encounter -> encounter.getId().equals(ehrCompositionId))
                .findFirst()
            ).flatMap(Optional::stream)
            .findFirst().map(Reference::new).orElse(null);
    }

    private List<RCMRMT030101UK04EhrComposition> getCompositionsContainingObservationStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().stream()
            .flatMap(component -> component.getEhrFolder().getComponent().stream())
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .map(RCMRMT030101UK04Component4::getObservationStatement)
                .anyMatch(Objects::nonNull))
            .toList();
    }
}
