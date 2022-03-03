package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Condition.ConditionClinicalStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.Condition.ConditionClinicalStatus.INACTIVE;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.EncounterReferenceUtil.getEncounterReference;
import static uk.nhs.adaptors.pss.translator.util.ExtensionUtil.buildReferenceExtension;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.CD;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04Component6;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.hl7.v3.RCMRMT030101UK04StatementRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConditionMapper {
    private static final String META_PROFILE = "ProblemHeader-Condition-1";
    private static final String RELATED_CLINICAL_CONTENT_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-RelatedClinicalContent-1";
    private static final String ACTUAL_PROBLEM_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-ActualProblem-1";
    private static final String PROBLEM_SIGNIFICANCE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-ProblemSignificance-1";
    private static final String MAJOR_CODE = "386134007";
    private static final String CLINICAL_STATUS_ACTIVE_CODE = "394774009";
    private static final String CLINICAL_STATUS_INACTIVE_CODE = "394775005";
    private static final String DEFAULT_CLINICAL_STATUS = "Defaulted status to active : Unknown status at source";
    private static final String DEFAULT_ANNOTATION = "Unspecified Significance: Defaulted to Minor";
    private static final String MAJOR_CODE_NAME = "major";
    private static final String MINOR_CODE_NAME = "minor";

    private final CodeableConceptMapper codeableConceptMapper;
    private final DateTimeMapper dateTimeMapper;

    public List<Condition> mapConditions(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        var compositionsContainingLinkSets = getCompositionsContainingLinkSets(ehrExtract);
        return compositionsContainingLinkSets.stream()
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream())
            .map(RCMRMT030101UK04Component4::getLinkSet)
            .filter(Objects::nonNull)
            .map(linkSet -> getCondition(
                ehrExtract,
                patient,
                encounters,
                compositionsContainingLinkSets,
                linkSet,
                practiseCode))
            .toList();
    }

    private Condition getCondition(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        List<RCMRMT030101UK04EhrComposition> compositionsContainingLinkSets, RCMRMT030101UK04LinkSet linkSet, String practiseCode) {
        RCMRMT030101UK04EhrComposition currentComposition = getCurrentEhrComposition(compositionsContainingLinkSets, linkSet);
        String id = linkSet.getId().getRoot();
        Condition condition = (Condition) new Condition()
            .addIdentifier(buildIdentifier(id, practiseCode))
            .addCategory(generateCategory())
            .setId(id)
            .setMeta(generateMeta(META_PROFILE));

        buildClinicalStatus(linkSet.getCode()).ifPresentOrElse(
            condition::setClinicalStatus,
            () -> {
                condition.setClinicalStatus(ACTIVE);
                condition.addNote(new Annotation(new StringType(DEFAULT_CLINICAL_STATUS)));
            });

        condition.setSubject(new Reference(patient));

        condition.addExtension(buildProblemSignificance(linkSet.getCode()));
        generateAnnotationToMinor(linkSet.getCode()).ifPresent(condition::addNote);

        buildContext(compositionsContainingLinkSets, encounters, linkSet).ifPresent(condition::setContext);

        buildOnsetDateTimeType(linkSet).ifPresent(condition::setOnset);
        buildAbatementDateTimeType(linkSet.getEffectiveTime()).ifPresent(condition::setAbatement);

        buildAssertedDateTimeType(currentComposition).ifPresentOrElse(
            condition::setAssertedDateElement,
            () -> condition.setAssertedDateElement(parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue())));

        currentComposition.getParticipant2()
            .stream()
            .findFirst()
            .ifPresent(participant2 -> condition.setAsserter(
                new Reference(new IdType(ResourceType.Practitioner.name(), participant2.getAgentRef().getId().getRoot())))
            );

        return condition;
    }

    public void addReferences(Bundle bundle, List<Condition> conditions, RCMRMT030101UK04EhrExtract ehrExtract) {
        getCompositionsContainingLinkSets(ehrExtract).stream()
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream())
            .map(RCMRMT030101UK04Component4::getLinkSet)
            .filter(Objects::nonNull)
            .forEach(linkSet ->
                conditions.stream()
                    .filter(condition1 -> linkSet.getId().getRoot().equals(condition1.getId()))
                    .findFirst().ifPresent(condition -> {
                        var namedStatementRef = linkSet.getConditionNamed().getNamedStatementRef();
                        buildActualProblem(bundle, namedStatementRef).ifPresent(condition::addExtension);

                        var referencedObservationStatement = getObservationStatementById(
                            ehrExtract,
                            namedStatementRef.getId().getRoot()
                        );

                        buildNotes(
                            referencedObservationStatement,
                            linkSet
                        ).forEach(condition::addNote);

                        referencedObservationStatement.ifPresent(
                            observationStatement -> condition.setCode(
                                codeableConceptMapper.mapToCodeableConcept(observationStatement.getCode()))
                        );

                        var statementRefs = linkSet.getComponent()
                            .stream()
                            .map(RCMRMT030101UK04Component6::getStatementRef)
                            .toList();
                        buildRelatedClinicalContent(bundle, statementRefs).forEach(condition::addExtension);
                    }));
    }

    private Optional<DateTimeType> buildOnsetDateTimeType(RCMRMT030101UK04LinkSet linkSet) {
        if (linkSet.getEffectiveTime() != null) {
            IVLTS effectiveTime = linkSet.getEffectiveTime();
            if (effectiveTime.hasLow() && effectiveTime.getLow().hasValue()) {
                return Optional.of(dateTimeMapper.mapDateTime(effectiveTime.getLow().getValue()));
            } else if (effectiveTime.hasCenter() && effectiveTime.getCenter().hasValue()) {
                return Optional.of(dateTimeMapper.mapDateTime(effectiveTime.getCenter().getValue()));
            }
        }
        if (linkSet.getAvailabilityTime() != null && linkSet.getAvailabilityTime().hasValue()) {
            return Optional.of(dateTimeMapper.mapDateTime(linkSet.getAvailabilityTime().getValue()));
        }
        return Optional.empty();
    }

    private Optional<DateTimeType> buildAbatementDateTimeType(IVLTS abatementDateTime) {
        if (abatementDateTime != null && abatementDateTime.hasHigh() && abatementDateTime.getHigh().getValue() != null) {
            return Optional.of(dateTimeMapper.mapDateTime(abatementDateTime.getHigh().getValue()));
        }
        return Optional.empty();
    }

    private Optional<DateTimeType> buildAssertedDateTimeType(RCMRMT030101UK04EhrComposition ehrComposition) {
        if (ehrComposition.getAuthor() != null && ehrComposition.getAuthor().getTime() != null) {
            return Optional.of(dateTimeMapper.mapDateTime(ehrComposition.getAuthor().getTime().getValue()));
        }
        return Optional.empty();
    }

    private Optional<Reference> buildContext(List<RCMRMT030101UK04EhrComposition> compositions, List<Encounter> encounters,
        RCMRMT030101UK04LinkSet linkSet) {
        return Optional.ofNullable(getEncounterReference(
            compositions,
            encounters,
            getCurrentEhrComposition(compositions, linkSet).getId().getRoot())
        );
    }

    private Optional<Extension> buildActualProblem(Bundle bundle, RCMRMT030101UK04StatementRef namedStatementRef) {
        if (namedStatementRef != null) {
            var resourceOpt = bundle.getEntry()
                .stream()
                .map(BundleEntryComponent::getResource)
                .filter(resource -> namedStatementRef.getId().getRoot().equals(resource.getId()))
                .findFirst();

            Reference reference = resourceOpt.map(Reference::new)
                .orElseGet(() -> new Reference(namedStatementRef.getId().getRoot()));

            return Optional.of(buildReferenceExtension(ACTUAL_PROBLEM_URL, reference));
        }
        return Optional.empty();
    }

    private boolean hasMajorCode(CD linkSetCode) {
        return hasCode(linkSetCode) && MAJOR_CODE.equals(linkSetCode.getQualifier().get(0).getName().getCode());
    }

    private Extension buildProblemSignificance(CD linkSetCode) {
        Extension extension = new Extension()
            .setUrl(PROBLEM_SIGNIFICANCE_URL);

        return hasMajorCode(linkSetCode)
            ? extension.setValue(new CodeType(MAJOR_CODE_NAME)) : extension.setValue(new CodeType(MINOR_CODE_NAME));
    }

    private Optional<Annotation> generateAnnotationToMinor(CD linkSetCode) {
        if (!hasMajorCode(linkSetCode)) {
            return Optional.of(new Annotation().setText(DEFAULT_ANNOTATION));
        }
        return Optional.empty();
    }

    private boolean hasCode(CD linkSetCode) {
        var crOpt = linkSetCode.getQualifier().stream().findFirst();
        return crOpt.isPresent() && crOpt.get().getName() != null && crOpt.get().getName().getCode() != null;
    }

    private Optional<Condition.ConditionClinicalStatus> buildClinicalStatus(CD linksetCode) {
        if (CLINICAL_STATUS_ACTIVE_CODE.equals(linksetCode.getCode())) {
            return Optional.of(ACTIVE);
        } else if (CLINICAL_STATUS_INACTIVE_CODE.equals(linksetCode.getCode())) {
            return Optional.of(INACTIVE);
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

    private List<Extension> buildRelatedClinicalContent(Bundle bundle, List<RCMRMT030101UK04StatementRef> relatedClinicalStatementRefs) {
        return relatedClinicalStatementRefs.stream()
            .flatMap(statementRef -> bundle.getEntry()
                .stream()
                .filter(entryComponent -> statementRef.getId().getRoot().equals(entryComponent.getResource().getId()))
            )
            .map(BundleEntryComponent::getResource)
            .map(resource -> buildReferenceExtension(RELATED_CLINICAL_CONTENT_URL, new Reference(resource)))
            .toList();
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

        if (linkSet.hasCode() && linkSet.getCode().getOriginalText() != null) {
            StringType text = new StringType(linkSet.getCode().getOriginalText());
            annotationList.add(new Annotation(text));
        }

        return annotationList;
    }

    private List<RCMRMT030101UK04EhrComposition> getCompositionsContainingLinkSets(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().stream()
            .flatMap(component -> component.getEhrFolder().getComponent().stream())
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .map(RCMRMT030101UK04Component4::getLinkSet)
                .anyMatch(Objects::nonNull))
            .toList();
    }

    private Optional<RCMRMT030101UK04ObservationStatement> getObservationStatementById(RCMRMT030101UK04EhrExtract ehrExtract, String id) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent().stream()
            .flatMap(e -> e.getEhrComposition().getComponent().stream())
            .map(RCMRMT030101UK04Component4::getObservationStatement)
            .filter(Objects::nonNull)
            .filter(observationStatement -> id.equals(observationStatement.getId().getRoot()))
            .findFirst();
    }

    private RCMRMT030101UK04EhrComposition getCurrentEhrComposition(List<RCMRMT030101UK04EhrComposition> ehrCompositions,
        RCMRMT030101UK04LinkSet linkSet) {
        return ehrCompositions
            .stream()
            .filter(e -> e.getComponent()
                .stream()
                .anyMatch(f -> linkSet.equals(f.getLinkSet()))
            ).findFirst().get();
    }
}
