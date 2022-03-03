package uk.nhs.adaptors.pss.translator.mapper;

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
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListEntryComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
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
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String PROCEDURE_REQUEST_REFERENCE = "ProcedureRequest/%s";
    private static final String REFERRAL_REQUEST_REFERENCE = "ReferralRequest/%s";
    private static final String CONDITION_REFERENCE = "Condition/%s";
    private static final String OBSERVATION_REFERENCE = "Observation/%s";
    private static final String DOCUMENT_REFERENCE_REFERENCE = "DocumentReference/%s";
    private static final String IMMUNIZATION_REFERENCE = "Immunization/%s";
    private static final String BATTERY_VALUE = "BATTERY";

    private final CodeableConceptMapper codeableConceptMapper;
    private final ConsultationListMapper consultationListMapper;

    public Map<String, List<? extends DomainResource>> mapEncounters(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient) {
        List<Encounter> encounters = new ArrayList<>();
        List<ListResource> consultations = new ArrayList<>();
        List<ListResource> topics = new ArrayList<>();
        List<ListResource> categories = new ArrayList<>();

        Map<String, List<? extends DomainResource>> map = new HashMap<>();

        List<RCMRMT030101UK04EhrComposition> ehrCompositionList = getEncounterEhrCompositions(ehrExtract);

        ehrCompositionList.forEach(ehrComposition -> {
            var encounter = mapToEncounter(ehrComposition, patient);
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

            linkSetList.forEach(linkSet -> linkSetTopic.addEntry(new ListEntryComponent(new Reference(CONDITION_REFERENCE
                .formatted(linkSet.getId().getRoot())))));
            consultation.addEntry(new ListEntryComponent(new Reference(linkSetTopic)));
            topics.add(linkSetTopic);
        }
    }

    private void generateCategoryLists(RCMRMT030101UK04CompoundStatement topicCompoundStatement, ListResource topic,
        List<ListResource> categories) {
        var categoryCompoundStatements = getCategoryCompoundStatements(topicCompoundStatement);
        categoryCompoundStatements.forEach(categoryCompoundStatement -> {
            var category = consultationListMapper.mapToCategory(topic, categoryCompoundStatement);

            addMappableResourcesFromCompoundStatement(categoryCompoundStatement, category, new ArrayList<>());
            topic.addEntry(new ListEntryComponent(new Reference(category)));
            categories.add(category);
        });
    }

    private void addMappableResourcesToTopicEntry(RCMRMT030101UK04EhrComposition ehrComposition, ListResource topic) {

        List<String> entryReferences = new ArrayList<>();

        ehrComposition.getComponent().forEach(component -> {

            addPlanStatementEntry(component.getPlanStatement(), entryReferences);
            addRequestStatementEntry(component.getRequestStatement(), entryReferences);
            addLinkSetEntry(component.getLinkSet(), entryReferences);
            addObservationStatementEntry(component.getObservationStatement(), entryReferences, null);
            addNarrativeStatementEntry(component.getNarrativeStatement(), entryReferences);
            entryReferences.forEach(reference -> addEntry(topic, reference));

            /**
             * TODO: Additional References
             * - Extend Immunization filter by incorporating Snomed CD Database (NIAD-1947)
             * - Add references to cases of Pathology Reports (NIAD-1967)
             * - Add references to cases of Templates (NIAD-2023)
             * - Add references to cases of AllergyIntolerance (NIAD-1969)
             * - (Ensure these are also called as appropriate in addMappableResourcesFromCompoundStatement)
             */

            // in this ticket, add medication references and ensure functionality in normal structured and topic lists.

            addMappableResourcesFromCompoundStatement(component.getCompoundStatement(), topic, entryReferences);
        });
        var x = 1;
    }

    private void addMappableResourcesFromCompoundStatement(RCMRMT030101UK04CompoundStatement compoundStatement, ListResource list,
        List<String> entryReferences) {

        if (compoundStatement != null) {
            compoundStatement.getComponent().forEach(component -> {

                addObservationStatementEntry(component.getObservationStatement(), entryReferences, compoundStatement);

                if (noPriorBloodPressureMapping(compoundStatement, entryReferences)) {
                    addNarrativeStatementEntry(component.getNarrativeStatement(), entryReferences);
                }

                addPlanStatementEntry(component.getPlanStatement(), entryReferences);
                addRequestStatementEntry(component.getRequestStatement(), entryReferences);
                addLinkSetEntry(component.getLinkSet(), entryReferences);

                addMappableResourcesFromCompoundStatement(component.getCompoundStatement(), list, entryReferences);
                entryReferences.forEach(reference -> addEntry(list, reference));
            });
        }
        var x = 1;
    }

    private boolean noPriorBloodPressureMapping(RCMRMT030101UK04CompoundStatement compoundStatement, List<String> entryReferences) {
        return compoundStatement == null || !entryReferences.contains(OBSERVATION_REFERENCE
            .formatted(compoundStatement.getId().get(0).getRoot()));
    }

    private void addObservationStatementEntry(RCMRMT030101UK04ObservationStatement observationStatement, List<String> entryReferences,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (observationStatement != null && noPriorBloodPressureMapping(compoundStatement, entryReferences)) {
            if (ResourceFilterUtil.hasBloodPressure(compoundStatement)) {
                addBloodPressureEntry(compoundStatement, entryReferences);
            } else if (ResourceFilterUtil.hasImmunizationCode(observationStatement)) {
                addImmunizationEntry(observationStatement, entryReferences);
            } else {
                addUncategorisedObservationEntry(observationStatement, entryReferences);
            }
        }
    }

    private void addBloodPressureEntry(RCMRMT030101UK04CompoundStatement compoundStatement, List<String> entryReferences) {
        entryReferences.add(OBSERVATION_REFERENCE.formatted(compoundStatement.getId().get(0).getRoot()));
    }

    private void addImmunizationEntry(RCMRMT030101UK04ObservationStatement observationStatement, List<String> entryReferences) {
        entryReferences.add(IMMUNIZATION_REFERENCE.formatted(observationStatement.getId().getRoot()));
    }

    private void addUncategorisedObservationEntry(RCMRMT030101UK04ObservationStatement observationStatement, List<String> entryReferences) {
        entryReferences.add(OBSERVATION_REFERENCE.formatted(observationStatement.getId().getRoot()));
    }

    private void addPlanStatementEntry(RCMRMT030101UK04PlanStatement planStatement, List<String> entryReferences) {
        if (planStatement != null) {
            entryReferences.add(PROCEDURE_REQUEST_REFERENCE.formatted(planStatement.getId().getRoot()));
        }
    }

    private void addRequestStatementEntry(RCMRMT030101UK04RequestStatement requestStatement, List<String> entryReferences) {
        if (requestStatement != null) {
            entryReferences.add(REFERRAL_REQUEST_REFERENCE.formatted(requestStatement.getId().get(0).getRoot()));
        }
    }

    private void addNarrativeStatementEntry(RCMRMT030101UK04NarrativeStatement narrativeStatement, List<String> entryReferences) {
        if (narrativeStatement != null) {
            if (ResourceFilterUtil.hasReferredToExternalDocument(narrativeStatement)) {
                entryReferences.add(DOCUMENT_REFERENCE_REFERENCE.formatted(
                    narrativeStatement.getReference().get(0).getReferredToExternalDocument().getId().getRoot()));
            } else {
                entryReferences.add(OBSERVATION_REFERENCE.formatted(narrativeStatement.getId().getRoot()));
            }
        }
    }

    private void addLinkSetEntry(RCMRMT030101UK04LinkSet linkSet, List<String> entryReferences) {
        if (linkSet != null) {
            entryReferences.add(CONDITION_REFERENCE.formatted(linkSet.getId().getRoot()));
        }
    }

    private void addEntry(ListResource list, String reference) {
        list.addEntry(new ListEntryComponent(new Reference(reference)));
    }

    private List<RCMRMT030101UK04LinkSet> getLinkSets(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getLinkSet)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
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

    private Encounter mapToEncounter(RCMRMT030101UK04EhrComposition ehrComposition, Patient patient) {
        var id = ehrComposition.getId().getRoot();

        var encounter = new Encounter();
        encounter
            .setParticipant(getParticipants(ehrComposition.getAuthor(), ehrComposition.getParticipant2()))
            .setStatus(EncounterStatus.FINISHED)
            .setSubject(new Reference(patient))
            .setType(getType(ehrComposition.getCode()))
            .setPeriod(getPeriod(ehrComposition))
            .setIdentifier(getIdentifier(id))
            .setMeta(generateMeta(ENCOUNTER_META_PROFILE))
            .setId(id);

        setEncounterLocation(encounter, ehrComposition);

        return encounter;
    }

    private List<CodeableConcept> getType(CD code) {
        return List.of(codeableConceptMapper.mapToCodeableConcept(code));
    }

    private List<Identifier> getIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
        return List.of(identifier);
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
}
