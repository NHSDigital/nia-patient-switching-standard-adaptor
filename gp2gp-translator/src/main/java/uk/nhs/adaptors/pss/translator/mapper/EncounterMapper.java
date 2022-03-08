package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListEntryComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.CD;
import org.hl7.v3.CsNullFlavor;
import org.hl7.v3.RCMRMT030101UK04Author;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04Participant2;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.hl7.v3.RCMRMT030101UK04RequestStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EncounterMapper {
    private static final List<String> INVALID_CODES = List.of("196401000000100", "196391000000103");
    private static final String ENCOUNTER_META_PROFILE = "Encounter-1";
    private static final String PRACTITIONER_REFERENCE_PREFIX = "Practitioner/";
    private static final String LOCATION_REFERENCE = "Location/%s-LOC";
    private static final String PERFORMER_SYSTEM = "http://hl7.org/fhir/v3/ParticipationType";
    private static final String PERFORMER_CODE = "PPRF";
    private static final String PERFORMER_DISPLAY = "primary performer";
    private static final String RECORDER_SYSTEM = "https://fhir.nhs.uk/STU3/CodeSystem/GPConnect-ParticipantType-1";
    private static final String RECORDER_CODE = "REC";
    private static final String RECORDER_DISPLAY = "recorder";
    private static final String TOPIC_CLASS_CODE = "TOPIC";
    private static final String CATEGORY_CLASS_CODE = "CATEGORY";
    private static final String ENCOUNTER_KEY = "encounters";
    private static final String CONSULTATION_KEY = "consultations";
    private static final String TOPIC_KEY = "topics";
    private static final String CATEGORY_KEY = "categories";
    private static final String MEDICATION_STATEMENT_REFERENCE = "%s-MS";
    private static final String QUESTIONNAIRE_REFERENCE = "%s-QRSP";

    private final CodeableConceptMapper codeableConceptMapper;
    private final ConsultationListMapper consultationListMapper;

    public Map<String, List<? extends DomainResource>> mapEncounters(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        String practiseCode) {
        List<Encounter> encounters = new ArrayList<>();
        List<ListResource> consultations = new ArrayList<>();
        List<ListResource> topics = new ArrayList<>();
        List<ListResource> categories = new ArrayList<>();

        Map<String, List<? extends DomainResource>> map = new HashMap<>();

        List<RCMRMT030101UK04EhrComposition> ehrCompositionList = getEncounterEhrCompositions(ehrExtract);

        ehrCompositionList.forEach(ehrComposition -> {
            var encounter = mapToEncounter(ehrComposition, patient, practiseCode);
            var consultation = consultationListMapper.mapToConsultation(ehrExtract, encounter);

            var topicCompoundStatementList = getTopicCompoundStatements(ehrComposition);
            if (CollectionUtils.isEmpty(topicCompoundStatementList)) {
                generateFlatConsultation(consultation, topics, ehrComposition);
            } else {
                generateStructuredConsultation(topicCompoundStatementList, ehrComposition, consultation, topics, categories);
            }

            encounters.add(encounter);
            consultations.add(consultation);
        });

        map.put(ENCOUNTER_KEY, encounters);
        map.put(CONSULTATION_KEY, consultations);
        map.put(TOPIC_KEY, topics);
        map.put(CATEGORY_KEY, categories);

        return map;
    }

    private void generateFlatConsultation(ListResource consultation, List<ListResource> topics,
        RCMRMT030101UK04EhrComposition ehrComposition) {
        var topic = consultationListMapper.mapToTopic(consultation, null);

        addMappableResourcesToTopicEntry(ehrComposition, topic);
        consultation.addEntry(new ListEntryComponent(new Reference(topic)));
        topics.add(topic);
    }

    private void generateStructuredConsultation(List<RCMRMT030101UK04CompoundStatement> topicCompoundStatementList,
        RCMRMT030101UK04EhrComposition ehrComposition, ListResource consultation, List<ListResource> topics,
        List<ListResource> categories) {
        topicCompoundStatementList.forEach(topicCompoundStatement -> {
            var topic = consultationListMapper.mapToTopic(consultation, topicCompoundStatement);
            consultation.addEntry(new ListEntryComponent(new Reference(topic)));

            generateCategoryLists(topicCompoundStatement, topic, categories);
            generateLinkSetTopicLists(ehrComposition, consultation, topics);

            topics.add(topic);
        });
    }

    private void generateLinkSetTopicLists(RCMRMT030101UK04EhrComposition ehrComposition, ListResource consultation,
        List<ListResource> topics) {
        var linkSetList = getLinkSets(ehrComposition);
        if (!CollectionUtils.isEmpty(linkSetList)) {
            var linkSetTopic = consultationListMapper.mapToTopic(consultation, null);

            addLinkSetsAsTopicEntries(linkSetList, linkSetTopic);
            consultation.addEntry(new ListEntryComponent(new Reference(linkSetTopic)));
            topics.add(linkSetTopic);
        }
    }

    private List<RCMRMT030101UK04LinkSet> getLinkSets(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getLinkSet)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private void addLinkSetsAsTopicEntries(List<RCMRMT030101UK04LinkSet> linkSetList, ListResource linkSetTopic) {
        linkSetList.forEach(linkSet -> linkSetTopic.addEntry(new ListEntryComponent(
            new Reference(new IdType(ResourceType.Condition.name(), linkSet.getId().getRoot())))));
    }

    private void generateCategoryLists(RCMRMT030101UK04CompoundStatement topicCompoundStatement, ListResource topic,
        List<ListResource> categories) {
        var categoryCompoundStatements = getCategoryCompoundStatements(topicCompoundStatement);
        categoryCompoundStatements.forEach(categoryCompoundStatement -> {
            var category = consultationListMapper.mapToCategory(topic, categoryCompoundStatement);

            List<Reference> entryReferences = new ArrayList<>();
            addMappableResourcesFromCompoundStatement(categoryCompoundStatement, category, entryReferences);
            entryReferences.forEach(reference -> addEntry(category, reference));

            topic.addEntry(new ListEntryComponent(new Reference(category)));
            categories.add(category);
        });
    }

    private List<RCMRMT030101UK04CompoundStatement> getCategoryCompoundStatements(RCMRMT030101UK04CompoundStatement
        topicCompoundStatement) {
        return topicCompoundStatement.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .filter(this::hasValidCategoryCompoundStatement)
            .toList();
    }

    private boolean hasValidCategoryCompoundStatement(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null && CATEGORY_CLASS_CODE.equals(compoundStatement.getClassCode().get(0));
    }

    private List<RCMRMT030101UK04EhrComposition> getEncounterEhrCompositions(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract
            .getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(this::isEncounterEhrComposition)
            .toList();
    }

    private boolean isEncounterEhrComposition(RCMRMT030101UK04EhrComposition ehrComposition) {
        return !INVALID_CODES.contains(ehrComposition.getCode().getCode())
            && ehrComposition.getComponent().stream().noneMatch(this::hasSuppressedContent);
    }

    private boolean hasSuppressedContent(RCMRMT030101UK04Component4 component) {
        return component.getEhrEmpty() != null || component.getRegistrationStatement() != null;
    }

    private List<RCMRMT030101UK04CompoundStatement> getTopicCompoundStatements(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition
            .getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .filter(this::hasValidTopicCompoundStatement)
            .toList();
    }

    private boolean hasValidTopicCompoundStatement(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null && TOPIC_CLASS_CODE.equals(compoundStatement.getClassCode().get(0));
    }

    private Encounter mapToEncounter(RCMRMT030101UK04EhrComposition ehrComposition, Patient patient, String practiseCode) {
        var id = ehrComposition.getId().getRoot();

        var encounter = new Encounter();
        encounter
            .setParticipant(getParticipants(ehrComposition.getAuthor(), ehrComposition.getParticipant2()))
            .setStatus(EncounterStatus.FINISHED)
            .setSubject(new Reference(patient))
            .setType(getType(ehrComposition.getCode()))
            .setPeriod(getPeriod(ehrComposition))
            .addIdentifier(buildIdentifier(id, practiseCode))
            .setMeta(generateMeta(ENCOUNTER_META_PROFILE))
            .setId(id);

        setEncounterLocation(encounter, ehrComposition);

        return encounter;
    }

    private List<CodeableConcept> getType(CD code) {
        return List.of(codeableConceptMapper.mapToCodeableConcept(code));
    }

    private Period getPeriod(RCMRMT030101UK04EhrComposition ehrComposition) {
        Period period = new Period();
        var effectiveTime = ehrComposition.getEffectiveTime();
        var availabilityTime = ehrComposition.getAvailabilityTime();

        if (effectiveTime.hasCenter()) {
            return period.setStartElement(DateFormatUtil.parseToDateTimeType(effectiveTime.getCenter().getValue()));
        } else if (effectiveTime.hasLow() && effectiveTime.hasHigh()) {
            return period.setStartElement(DateFormatUtil.parseToDateTimeType(effectiveTime.getLow().getValue()))
                .setEndElement(DateFormatUtil.parseToDateTimeType(effectiveTime.getHigh().getValue()));
        } else if (effectiveTime.hasLow() && !effectiveTime.hasHigh()) {
            return period.setStartElement(DateFormatUtil.parseToDateTimeType(effectiveTime.getLow().getValue()));
        } else if (!effectiveTime.hasLow() && effectiveTime.hasHigh()) {
            return period.setEndElement(DateFormatUtil.parseToDateTimeType(effectiveTime.getHigh().getValue()));
        } else if (CsNullFlavor.UNK.value().equals(effectiveTime.getNullFlavor().value())) {
            return null;
        } else if (availabilityTime.hasValue()) {
            return period.setStartElement(DateFormatUtil.parseToDateTimeType(availabilityTime.getValue()));
        }

        return null;
    }

    private EncounterParticipantComponent getRecorder(RCMRMT030101UK04Author author) {
        var recorder = new EncounterParticipantComponent();
        var coding = new Coding(RECORDER_SYSTEM, RECORDER_CODE, RECORDER_DISPLAY);

        return recorder
            .addType(new CodeableConcept(coding))
            .setIndividual(new Reference(PRACTITIONER_REFERENCE_PREFIX + author.getAgentRef().getId().getRoot()));
    }

    private boolean isNonNullParticipant2(RCMRMT030101UK04Participant2 participant2) {
        return participant2.getNullFlavor() == null;
    }

    private EncounterParticipantComponent getPerformer(RCMRMT030101UK04Participant2 participant2) {
        var performer = new EncounterParticipantComponent();
        var coding = new Coding();

        coding
            .setSystem(PERFORMER_SYSTEM)
            .setCode(PERFORMER_CODE)
            .setDisplay(PERFORMER_DISPLAY);

        return performer
            .addType(new CodeableConcept(coding))
            .setIndividual(new Reference(PRACTITIONER_REFERENCE_PREFIX + participant2.getAgentRef().getId().getRoot()));
    }

    private List<EncounterParticipantComponent> getParticipants(RCMRMT030101UK04Author author,
        List<RCMRMT030101UK04Participant2> participant2List) {
        List<EncounterParticipantComponent> participants = new ArrayList<>();

        if (author.getNullFlavor() == null) {
            participants.add(getRecorder(author));
        }

        participant2List
            .stream()
            .filter(this::isNonNullParticipant2)
            .findFirst()
            .ifPresent(participant2 -> participants.add(getPerformer(participant2)));

        return participants;
    }

    private void setEncounterLocation(Encounter encounter, RCMRMT030101UK04EhrComposition ehrComposition) {
        if (ehrComposition.getLocation() != null) {
            var location = new EncounterLocationComponent();
            location.setLocation(new Reference(LOCATION_REFERENCE.formatted(ehrComposition.getId().getRoot())));

            encounter.setLocation(List.of(location));
        }
    }

    private void addMappableResourcesToTopicEntry(RCMRMT030101UK04EhrComposition ehrComposition, ListResource topic) {
        List<Reference> entryReferences = new ArrayList<>();

        ehrComposition.getComponent().forEach(component -> {
            addPlanStatementEntry(component.getPlanStatement(), entryReferences);
            addRequestStatementEntry(component.getRequestStatement(), entryReferences);
            addLinkSetEntry(component.getLinkSet(), entryReferences);
            addObservationStatementEntry(component.getObservationStatement(), entryReferences, null);
            addNarrativeStatementEntry(component.getNarrativeStatement(), entryReferences);
            addMedicationEntry(component.getMedicationStatement(), entryReferences);
            addMappableResourcesFromCompoundStatement(component.getCompoundStatement(), topic, entryReferences);
        });

        entryReferences.forEach(reference -> addEntry(topic, reference));
    }

    private void addMappableResourcesFromCompoundStatement(RCMRMT030101UK04CompoundStatement compoundStatement, ListResource list,
        List<Reference> entryReferences) {
        if (compoundStatement != null) {
            if (ResourceFilterUtil.isDiagnosticReport(compoundStatement)) {
                addDiagnosticReportEntry(compoundStatement, entryReferences);
            } else if (ResourceFilterUtil.isTemplate(compoundStatement)) {
                addTemplateEntry(compoundStatement, entryReferences);
            } else {
                compoundStatement.getComponent().forEach(component -> {
                    addObservationStatementEntry(component.getObservationStatement(), entryReferences, compoundStatement);
                    addPlanStatementEntry(component.getPlanStatement(), entryReferences);
                    addRequestStatementEntry(component.getRequestStatement(), entryReferences);
                    addLinkSetEntry(component.getLinkSet(), entryReferences);
                    addMedicationEntry(component.getMedicationStatement(), entryReferences);

                    if (noPriorBloodPressureMapping(compoundStatement, entryReferences)) {
                        addNarrativeStatementEntry(component.getNarrativeStatement(), entryReferences);
                    }

                    addMappableResourcesFromCompoundStatement(component.getCompoundStatement(), list, entryReferences);
                });
            }
        }
    }

    private boolean noPriorBloodPressureMapping(RCMRMT030101UK04CompoundStatement compoundStatement, List<Reference> entryReferences) {
        return compoundStatement == null || entryReferences.stream()
                .map(Reference::getReference)
                .noneMatch(reference -> reference.contains(compoundStatement.getId().get(0).getRoot()));
    }

    private void addTemplateEntry(RCMRMT030101UK04CompoundStatement compoundStatement, List<Reference> entryReferences) {
        entryReferences.add(new Reference(new IdType(ResourceType.QuestionnaireResponse.name(),
            QUESTIONNAIRE_REFERENCE.formatted(compoundStatement.getId().get(0).getRoot()))));
    }

    private void addObservationStatementEntry(RCMRMT030101UK04ObservationStatement observationStatement, List<Reference> entryReferences,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (observationStatement != null && noPriorBloodPressureMapping(compoundStatement, entryReferences)) {
            if (ResourceFilterUtil.isBloodPressure(compoundStatement)) {
                addBloodPressureEntry(compoundStatement, entryReferences);
            } else if (ResourceFilterUtil.isAllergyIntolerance(compoundStatement)) {
                addAllergyIntoleranceEntry(observationStatement, entryReferences);
            } else if (ResourceFilterUtil.isImmunization(observationStatement)) {
                addImmunizationEntry(observationStatement, entryReferences);
            } else {
                addUncategorisedObservationEntry(observationStatement, entryReferences);
            }
        }
    }

    private void addAllergyIntoleranceEntry(RCMRMT030101UK04ObservationStatement observationStatement, List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.AllergyIntolerance.name(), observationStatement.getId().getRoot()));
    }

    private void addDiagnosticReportEntry(RCMRMT030101UK04CompoundStatement compoundStatement, List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.DiagnosticReport.name(), compoundStatement.getId().get(0).getRoot()));
    }

    private void addMedicationEntry(RCMRMT030101UK04MedicationStatement medicationStatement, List<Reference> entryReferences) {
        if (medicationStatement != null) {
            medicationStatement.getComponent().forEach(component -> {
                if (component.hasEhrSupplyAuthorise()) {
                    var id = component.getEhrSupplyAuthorise().getId().getRoot();
                    entryReferences.add(createResourceReference(ResourceType.MedicationStatement.name(),
                        MEDICATION_STATEMENT_REFERENCE.formatted(id)));
                    entryReferences.add(createResourceReference(ResourceType.MedicationRequest.name(), id));
                } else if (component.hasEhrSupplyPrescribe()) {
                    entryReferences.add(createResourceReference(ResourceType.MedicationRequest.name(),
                        component.getEhrSupplyPrescribe().getId().getRoot()));
                }
            });
        }
    }

    private void addBloodPressureEntry(RCMRMT030101UK04CompoundStatement compoundStatement, List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.Observation.name(), compoundStatement.getId().get(0).getRoot()));
    }

    private void addImmunizationEntry(RCMRMT030101UK04ObservationStatement observationStatement, List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.Immunization.name(), observationStatement.getId().getRoot()));
    }

    private void addUncategorisedObservationEntry(RCMRMT030101UK04ObservationStatement observationStatement,
        List<Reference> entryReferences) {
        entryReferences.add(createResourceReference(ResourceType.Observation.name(), observationStatement.getId().getRoot()));
    }

    private void addPlanStatementEntry(RCMRMT030101UK04PlanStatement planStatement, List<Reference> entryReferences) {
        if (planStatement != null) {
            entryReferences.add(createResourceReference(ResourceType.ProcedureRequest.name(), planStatement.getId().getRoot()));
        }
    }

    private void addRequestStatementEntry(RCMRMT030101UK04RequestStatement requestStatement, List<Reference> entryReferences) {
        if (requestStatement != null) {
            entryReferences.add(createResourceReference(ResourceType.ReferralRequest.name(), requestStatement.getId().get(0).getRoot()));
        }
    }

    private void addNarrativeStatementEntry(RCMRMT030101UK04NarrativeStatement narrativeStatement, List<Reference> entryReferences) {
        if (narrativeStatement != null) {
            if (ResourceFilterUtil.hasReferredToExternalDocument(narrativeStatement)) {
                entryReferences.add(createResourceReference(ResourceType.DocumentReference.name(),
                    narrativeStatement.getReference().get(0).getReferredToExternalDocument().getId().getRoot()));
            } else {
                entryReferences.add(createResourceReference(ResourceType.Observation.name(), narrativeStatement.getId().getRoot()));
            }
        }
    }

    private void addLinkSetEntry(RCMRMT030101UK04LinkSet linkSet, List<Reference> entryReferences) {
        if (linkSet != null) {
            entryReferences.add(createResourceReference(ResourceType.Condition.name(), linkSet.getId().getRoot()));
        }
    }

    private Reference createResourceReference(String resourceName, String id) {
        return new Reference(new IdType(resourceName, id));
    }

    private void addEntry(ListResource list, Reference reference) {
        list.addEntry(new ListEntryComponent(reference));
    }
}
