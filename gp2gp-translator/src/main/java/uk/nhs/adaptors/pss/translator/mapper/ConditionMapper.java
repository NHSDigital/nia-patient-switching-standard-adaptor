package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04StatementRef;

public class ConditionMapper {

    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProblemHeader-Condition-1";
    private static final String RELATED_CLINICAL_CONTENT_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProblemHeader-Condition-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/{practiseCode}";
    private static final String ACTUAL_PROBLEM_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-ActualProblem-1";
    private static final String PROBLEM_SIGNIFICANCE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-ProblemSignificance-1";
    private static final String MAJOR_CODE = "386134007";
    private static final String CLINICAL_STATUS_ACTIVE_CODE = "394774009";
    private static final String CLINICAL_STATUS_INACTIVE_CODE = "394775005";
    private static final String DEFAULT_CLINICAL_STATUS = "Defaulted status to active : Unknown status at source";

    private CodeableConceptMapper codeableConceptMapper;

    public Condition mapToCondition(
        RCMRMT030101UK04LinkSet linkSet,
        Resource actualProblem,
        List<Resource> relatedClinicalContent,
        String patientId,
        String encounterId,
        String asserterId) {

        var condition = new Condition();
        var id = linkSet.getId().getRoot();

        condition.setId(id);
        condition.setMeta(generateConditionMeta());
        condition.addIdentifier(buildIdentifier(id));

        condition.addExtension(
            buildActualProblem(linkSet.getConditionNamed().getNamedStatementRef(), actualProblem)
        );
        condition.addExtension(
            buildProblemSignificance(linkSet.getCode())
        );
        buildRelatedClinicalContent(linkSet, relatedClinicalContent).forEach(condition::addExtension);

        buildClinicalStatus(linkSet.getCode()).ifPresentOrElse(
            condition::setClinicalStatus,
            () -> {
                condition.setClinicalStatus(Condition.ConditionClinicalStatus.ACTIVE);
                condition.addNote(new Annotation(new StringType(DEFAULT_CLINICAL_STATUS)));
        });

        condition.setCode(codeableConceptMapper.mapToCodeableConcept(linkSet.getCode()));
        condition.setSubject(new Reference(new IdType(ResourceType.Patient.name(), patientId)));
        condition.setContext(new Reference(new IdType(ResourceType.Encounter.name(), encounterId)));
        condition.setAsserter(new Reference(new IdType(ResourceType.Practitioner.name(), asserterId)));

        return condition;
    }

    private Meta generateConditionMeta() {
        Meta meta = new Meta();
        UriType profile = new UriType(META_PROFILE);
        meta.setProfile(List.of(profile));
        return meta;
    }

    private Identifier buildIdentifier(String rootId) {
        Identifier identifier = new Identifier();
        identifier.setSystem(IDENTIFIER_SYSTEM);
        identifier.setValue(rootId);

        return identifier;
    }

    private Extension buildActualProblem(RCMRMT030101UK04StatementRef namedStatementRef, Resource actualProblem) {
        Reference reference = buildResourceReference(actualProblem);
        return buildReferenceExtension(ACTUAL_PROBLEM_URL, reference);
    }

    private Extension buildProblemSignificance(CD linksetCode) {
        var code = Optional.ofNullable(linksetCode.getQualifier().get(0).getName().getCode());
        var extension = new Extension();
        extension.setUrl(PROBLEM_SIGNIFICANCE_URL);

        if (code.isPresent() && code.get().equals(MAJOR_CODE)) {
            extension.setValue(new CodeType("Major"));
        } else {
            extension.setValue(new CodeType("Minor"));
        }

        return extension;
    }

    private Optional<Condition.ConditionClinicalStatus> buildClinicalStatus(CD linksetCode) {
        if (linksetCode.getCode().equals(CLINICAL_STATUS_ACTIVE_CODE)) {
            return Optional.of(Condition.ConditionClinicalStatus.ACTIVE);
        } else if (linksetCode.getCode().equals(CLINICAL_STATUS_INACTIVE_CODE)) {
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

    private List<Extension> buildRelatedClinicalContent(RCMRMT030101UK04LinkSet linkSet, List<Resource> relatedClinicalResources) {
        return relatedClinicalResources.stream()
            .map(ConditionMapper::buildResourceReference)
            .map(reference -> buildReferenceExtension(RELATED_CLINICAL_CONTENT_URL, reference))
            .collect(Collectors.toList());
    }

    private static Reference buildResourceReference(Resource resource) {
        IdType idType = new IdType(resource.getResourceType().name(), resource.getId());
        return new Reference(idType);
    }

    private Extension buildReferenceExtension(String url, Reference reference) {
        Extension extension = new Extension();
        extension.setUrl(url);
        extension.setValue(reference);
        return extension;
    }
}
