package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildReferenceExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.hl7.v3.TS;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
public class ConditionMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProblemHeader-Condition-1";
    private static final String RELATED_CLINICAL_CONTENT_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-RelatedClinicalContent-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/{practiseCode}";
    private static final String PRACTISE_CODE = "{practiseCode}";
    private static final String ACTUAL_PROBLEM_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-ActualProblem-1";
    private static final String PROBLEM_SIGNIFICANCE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-ProblemSignificance-1";
    private static final String MAJOR_CODE = "386134007";
    private static final String CLINICAL_STATUS_ACTIVE_CODE = "394774009";
    private static final String CLINICAL_STATUS_INACTIVE_CODE = "394775005";
    private static final String DEFAULT_CLINICAL_STATUS = "Defaulted status to active : Unknown status at source";

    private CodeableConceptMapper codeableConceptMapper;
    private DateTimeMapper dateTimeMapper;

    public Condition mapToCondition(ConditionMapperParameters conditionMapperParameters) {

        Optional<RCMRMT030101UK04LinkSet> linkSetOpt = conditionMapperParameters.getEhrComposition().getComponent().stream()
            .map(RCMRMT030101UK04Component4::getLinkSet)
            .filter(Objects::nonNull)
            .findFirst();

        Optional<RCMRMT030101UK04ObservationStatement> observationStatementOpt = conditionMapperParameters.getEhrComposition().getComponent().stream()
            .map(RCMRMT030101UK04Component4::getObservationStatement)
            .filter(Objects::nonNull)
            .findFirst();

        if (linkSetOpt.isPresent()) {
            RCMRMT030101UK04LinkSet linkSet = linkSetOpt.get();
            Condition condition = new Condition();
            String id = linkSet.getId().getRoot();

            condition.setId(id);
            condition.setMeta(generateConditionMeta());
            condition.addIdentifier(buildIdentifier(id, conditionMapperParameters.getPractiseCode()));
            condition.addCategory(generateCategory());

            conditionMapperParameters.getActualProblem().ifPresent((resource) -> {
                condition.addExtension(
                    buildActualProblem(resource)
                );
            });
            condition.addExtension(
                buildProblemSignificance(linkSet.getCode())
            );
            buildRelatedClinicalContent(conditionMapperParameters.getRelatedClinicalContent()).forEach(condition::addExtension);

            buildClinicalStatus(linkSet.getCode()).ifPresentOrElse(
                condition::setClinicalStatus,
                () -> {
                    condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
                    condition.addNote(new Annotation(new StringType(DEFAULT_CLINICAL_STATUS)));
                });

            condition.setCode(codeableConceptMapper.mapToCodeableConcept(linkSet.getCode()));
            condition.setSubject(new Reference(conditionMapperParameters.getPatient()));
            conditionMapperParameters.getEncounter().ifPresent(
                value -> condition.setContext(new Reference(value))
            );

            buildOnsetDateTimeType(linkSet).ifPresent(condition::setOnset);
            buildAbatementDateTimeType(linkSet.getEffectiveTime()).ifPresent(condition::setAbatement);

            buildAssertedDateTimeType(conditionMapperParameters.getEhrComposition()).ifPresentOrElse(
                condition::setAssertedDate,
                () -> {
                    condition.setAssertedDate(conditionMapperParameters.getEhrExtractAvailabilityTime());
                });
            condition.setAsserter(new Reference(new IdType(ResourceType.Practitioner.name(), conditionMapperParameters.getAsserterId())));

            buildNotes(observationStatementOpt, linkSet).forEach(condition::addNote);

            return condition;
        }
        return null;
    }

    private Optional<DateTimeType> buildOnsetDateTimeType(RCMRMT030101UK04LinkSet linkSet) {
        IVLTS effectiveTime = linkSet.getEffectiveTime();
        TS availabilityTime = linkSet.getAvailabilityTime();

        if (effectiveTime.hasLow() && effectiveTime.getLow().getValue() != null) {
            return Optional.of(dateTimeMapper.mapDateTime(effectiveTime.getLow().getValue()));
        } else if (effectiveTime.hasCenter() && effectiveTime.getCenter().getValue() != null) {
            return Optional.of(dateTimeMapper.mapDateTime(effectiveTime.getCenter().getValue()));
        } else if (availabilityTime != null && availabilityTime.getValue() != null) {
            return Optional.of(dateTimeMapper.mapDateTime(availabilityTime.getValue()));
        }
        return Optional.empty();
    }

    private Optional<DateTimeType> buildAbatementDateTimeType(IVLTS abatementDateTime) {
        if (abatementDateTime.hasHigh() && abatementDateTime.getHigh().getValue() != null) {
            return Optional.of(dateTimeMapper.mapDateTime(abatementDateTime.getHigh().getValue()));
        }
        return Optional.empty();
    }

    private Optional<Date> buildAssertedDateTimeType(RCMRMT030101UK04EhrComposition ehrComposition) {
        if (ehrComposition.getAuthor() != null && ehrComposition.getAuthor().getTime() != null) {
            return Optional.of(
                dateTimeMapper.mapDateTime(ehrComposition.getAuthor().getTime().getValue()).getValue()
            );
        }
        return Optional.empty();
    }

    private Meta generateConditionMeta() {
        Meta meta = new Meta();
        UriType profile = new UriType(META_PROFILE);
        meta.setProfile(List.of(profile));
        return meta;
    }

    private Identifier buildIdentifier(String rootId, String practiseCode) {
        Identifier identifier = new Identifier();
        identifier.setSystem(IDENTIFIER_SYSTEM.replace(PRACTISE_CODE, practiseCode));
        identifier.setValue(rootId);

        return identifier;
    }

    private Extension buildActualProblem(Resource actualProblem) {
        return buildReferenceExtension(ACTUAL_PROBLEM_URL, new Reference(actualProblem));
    }

    private Extension buildProblemSignificance(CD linksetCode) {
        Optional<String> code = Optional.ofNullable(linksetCode.getQualifier().get(0).getName().getCode());
        Extension extension = new Extension();
        extension.setUrl(PROBLEM_SIGNIFICANCE_URL);

        if (code.isPresent() && code.get().equals(MAJOR_CODE)) {
            extension.setValue(new CodeType("Major"));
        } else {
            extension.setValue(new CodeType("Minor"));
        }

        return extension;
    }

    private Optional<Condition.ConditionClinicalStatus> buildClinicalStatus(CD linksetCode) {
        if (CLINICAL_STATUS_ACTIVE_CODE.equals(linksetCode.getCode())) {
            return Optional.of(Condition.ConditionClinicalStatus.ACTIVE);
        } else if (CLINICAL_STATUS_INACTIVE_CODE.equals(linksetCode.getCode())) {
            return Optional.of(Condition.ConditionClinicalStatus.INACTIVE);
        }
        return Optional.empty();
    }

    private CodeableConcept generateCategory() {
        Coding coding = new Coding();
        coding.setSystem("https://fhir.hl7.org.uk/STU3/CodeSystem/CareConnect-ConditionCategory-1");
        coding.setCode("problem-list-item");
        coding.setDisplay("Problem List Item");

        return new CodeableConcept()
            .addCoding(coding);
    }

    private List<Extension> buildRelatedClinicalContent(List<Resource> relatedClinicalResources) {
        return relatedClinicalResources.stream()
            .map(Reference::new)
            .map(reference -> buildReferenceExtension(RELATED_CLINICAL_CONTENT_URL, reference))
            .collect(Collectors.toList());
    }

    private List<Annotation> buildNotes(Optional<RCMRMT030101UK04ObservationStatement> observationStatement,
        RCMRMT030101UK04LinkSet linkSet) {
        List<Annotation> annotationList = new ArrayList<>();

        observationStatement.ifPresent(observationStatement1 -> observationStatement1.getPertinentInformation()
            .stream()
            .map(RCMRMT030101UK04PertinentInformation02::getPertinentAnnotation)
            .filter(Objects::nonNull)
            .map(RCMRMT030101UK04Annotation::getText)
            .filter(StringUtils::isNotBlank)
            .map(StringType::new)
            .map(Annotation::new)
            .forEach(annotationList::add));

        if (linkSet.getCode().getOriginalText() != null) {
            StringType text = new StringType(linkSet.getCode().getOriginalText());
            annotationList.add(new Annotation(text));
        }

        return annotationList;
    }

    @Builder
    @Getter
    public static class ConditionMapperParameters {
        private RCMRMT030101UK04EhrComposition ehrComposition;
        private Date ehrExtractAvailabilityTime;
        private Optional<Resource> actualProblem;
        private List<Resource> relatedClinicalContent;
        private Patient patient;
        private Optional<Encounter> encounter;
        private String asserterId;
        private String practiseCode;
    }
}
