package uk.nhs.adaptors.pss.translator.mapper;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationReferenceRangeComponent;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.CV;
import org.hl7.v3.II;
import org.hl7.v3.IVLTS;
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

@AllArgsConstructor
public class ObservationMapper {
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";
    private static final String VALUE_QUANTITY_EXTENSION = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-ValueApproximation-1";
    private static final String ST_VALUE = "ST";
    private static final String CV_VALUE = "CV";
    private static final String SUBJECT_COMMENT = "Subject: %s ";

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
        // TODO: performer
        // TODO: valueQuantity
        // TODO: valueQuantity.extension
        // TODO: valueString ("Negative")
        // TODO: valueString ("Female")
        // TODO: interpretation
        //////// TODO: comment 1
        //////// TODO: comment 2
        // TODO: referenceRange

        var id = observationStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var code = getCode(observationStatement.getCode());
        var effective = getEffective(observationStatement.getEffectiveTime(), observationStatement.getAvailabilityTime());
        var issued = getIssued(ehrExtract, observationStatement.getId());
        //var performer = ...
        var valueQuantity = getValueQuantity(observationStatement.getUncertaintyCode());
        var valueString = getValueString(observationStatement.getValue());
        var comment = getComment(observationStatement.getPertinentInformation(), observationStatement.getSubject());
        var referenceRange = getReferenceRange(observationStatement.getReferenceRange());
            
        /**
         * TODO: Known future implementations to this mapper
         * - subject: references a global patient resource for the transaction (NIAD-2024)
         * - context: references an encounter resource if it has been generated from the ehrComposition (NIAD-2025)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        return null;
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
                return effectiveTime.getCenter().getValue();
            }

            var effectivePeriod = new Period();

            if (effectiveTimeHasLow(effectiveTime)) {
                effectivePeriod.setStart(DateFormatUtil.parse(effectiveTime.getLow().getValue()).getValue());
            }

            if (effectiveTimeHasHigh(effectiveTime)) {
                effectivePeriod.setEnd(DateFormatUtil.parse(effectiveTime.getLow().getValue()).getValue());
            }

            if (availabilityTimeHasValue(effectiveTime)) {
                if (effectivePeriod.getStart() == null) {
                    if (effectivePeriod.getEnd() == null) {
                        return availabilityTime.getValue();
                    }

                    effectivePeriod.setStart(DateFormatUtil.parse(availabilityTime.getValue()).getValue());

                    return effectivePeriod;
                }
            }
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
    
    private Instant getIssued(RCMRMT030101UK04EhrExtract ehrExtract, II observationStatementId) {
        var ehrComposition = EhrResourceExtractorUtil.extractEhrCompositionForPlanStatement(ehrExtract, observationStatementId);
        
        if (authorHasValidTimeValue(ehrComposition.getAuthor())) {
            return DateFormatUtil.parseToInstantType(ehrComposition.getAuthor().getTime().getValue()).getValue().toInstant();
        }

        if (availabilityTimeHasValue(ehrExtract.getAvailabilityTime())) {
            return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue()).getValue().toInstant();
        }
        
        return null;
    }
    
    private boolean authorHasValidTimeValue(RCMRMT030101UK04Author author) {
        return author != null && author.getTime() != null && author.getTime().getValue() != null && author.getTime().getNullFlavor() == null;
    }

    private Reference getPerformer(List<RCMRMT030101UK04Participant> participantList, RCMRMT030101UK04EhrExtract ehrExtract,
        II observationStatementId) {

    }

    private Quantity getValueQuantity(Value value, CV uncertaintyCode) {
        var valueQuantity = quantityMapper.mapQuantity(value);
        
        if (uncertaintyCode != null) {
            //TODO: .valueBoolean???????
            valueQuantity.getExtension().add(new Extension().setUrl(VALUE_QUANTITY_EXTENSION));
        }
        
        return valueQuantity;
    }

    private String getValueString(Value value) {
        if (ST_VALUE.equals(value.getType())) {
            return value.getValue();
        }

//        if(CV_VALUE.equals(value.getType())) {
//                if (StringUtils.isNotEmpty(value..getOriginalText())) {
//                    return PRIORITY_PREFIX + priorityCode.getOriginalText();
//                } else if (StringUtils.isNotEmpty(priorityCode.getDisplayName())) {
//                    return PRIORITY_PREFIX + priorityCode.getDisplayName();
//                }
//        }
        return null;
    }

    private String getComment(List<RCMRMT030101UK04PertinentInformation02> pertinentInformation, RCMRMT030101UK04Subject subject) {
        StringBuilder stringBuilder = new StringBuilder();
        
        if (subjectHasOriginalText(subject)) {
            stringBuilder.append(String.format(SUBJECT_COMMENT, subject.getPersonalRelationship().getCode().getOriginalText()));
        } else if (subjectHasDisplayName(subject)) {
            stringBuilder.append(String.format(SUBJECT_COMMENT, subject.getPersonalRelationship().getCode().getDisplayName()));
        }
        
        pertinentInformation.stream()
            .filter(this::pertinentInformationHasOriginalText)
            .map(stringBuilder::append);
        
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
            
           // if (range.getReferenceInterpretationRange().getValue().get)
        }
        // .text
        // .low
        // .high
        
        return null;
    }

    private Observation createObservation() {
        var observation = new Observation();
        
        observation.getMeta().getProfile().add(new UriType(META_PROFILE));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        return null;
    }
}
