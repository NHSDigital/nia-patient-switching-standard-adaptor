package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListEntryComponent;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.CD;
import org.hl7.v3.CsNullFlavor;
import org.hl7.v3.II;
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
import org.hl7.v3.RCMRMT030101UK04Participant2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceReferenceUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EncounterMapper {
    private static final List<String> INVALID_CODES = List.of("196401000000100", "196391000000103");
    private static final String ENCOUNTER_META_PROFILE = "Encounter-1";
    private static final String PRACTITIONER_REFERENCE_PREFIX = "Practitioner/";
    private static final String LOCATION_REFERENCE = "Location/%s";
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
    private static final String IDENTIFIER_EXTERNAL = "2.16.840.1.113883.2.1.4.5.3";
    private static final String RELATED_PROBLEM_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-RelatedProblemHeader-1";
    private static final String RELATED_PROBLEM_TARGET_URL = "target";

    private final CodeableConceptMapper codeableConceptMapper;
    private final ConsultationListMapper consultationListMapper;
    private final ResourceReferenceUtil resourceReferenceUtil;

    public Map<String, List<? extends DomainResource>> mapEncounters(
            RCMRMT030101UK04EhrExtract ehrExtract,
            Patient patient,
            String practiseCode,
            List<Location> entryLocations
    ) {
        List<Encounter> encounters = new ArrayList<>();
        List<ListResource> consultations = new ArrayList<>();
        List<ListResource> topics = new ArrayList<>();
        List<ListResource> categories = new ArrayList<>();

        Map<String, List<? extends DomainResource>> map = new HashMap<>();

        List<RCMRMT030101UK04EhrComposition> ehrCompositionList = getEncounterEhrCompositions(ehrExtract);

        ehrCompositionList.forEach(ehrComposition -> {
            var encounter = mapToEncounter(ehrComposition, patient, practiseCode, entryLocations);
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

        List<Reference> entryReferences = new ArrayList<>();
        resourceReferenceUtil.extractChildReferencesFromEhrComposition(ehrComposition, entryReferences);
        entryReferences.forEach(reference -> addEntry(topic, reference));

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

            List<Extension> relatedProblems = getRelatedProblemsFromEncounter(topicCompoundStatement, ehrComposition);
            relatedProblems.forEach(topic::addExtension);

            topics.add(topic);
        });
    }

    private List<Extension> getRelatedProblemsFromEncounter(RCMRMT030101UK04CompoundStatement topicCompoundStatement,
        RCMRMT030101UK04EhrComposition ehrComposition) {

        var components = topicCompoundStatement.getComponent().stream()
            .map(RCMRMT030101UK04Component02::getCompoundStatement)
            .filter(Objects::nonNull)
            .flatMap(categoryCompoundStatement -> categoryCompoundStatement.getComponent().stream())
            .toList();

        List<String> observationStatementIds = components.stream()
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .filter(Objects::nonNull)
            .map(observationStatement -> observationStatement.getId().getRoot())
            .toList();

        List<String> requestStatementIds = components.stream()
            .map(RCMRMT030101UK04Component02::getRequestStatement)
            .filter(Objects::nonNull)
            .flatMap(requestStatement -> requestStatement.getId().stream())
            .map(II::getRoot)
            .filter(root -> !root.equals(IDENTIFIER_EXTERNAL))
            .toList();

        HashSet<String> statementIds = new HashSet<>();
        statementIds.addAll(observationStatementIds);
        statementIds.addAll(requestStatementIds);

        var linkSets = getLinkSets(ehrComposition);
        List<Extension> extensions = new ArrayList<>();

        for (var linkSet : linkSets) {
            var conditionNamed = linkSet.getConditionNamed();

            if (conditionNamed != null
                && statementIds.contains(conditionNamed.getNamedStatementRef().getId().getRoot())) {

                var extension = new Extension(RELATED_PROBLEM_URL);

                extension.addExtension(new Extension(RELATED_PROBLEM_TARGET_URL,
                        new Reference(new IdType(ResourceType.Condition.name(), linkSet.getId().getRoot()))));

                extensions.add(extension);
            }
        }

        return extensions;
    }

    private List<RCMRMT030101UK04LinkSet> getLinkSets(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getLinkSet)
            .filter(Objects::nonNull)
            .toList();
    }

    private void generateCategoryLists(RCMRMT030101UK04CompoundStatement topicCompoundStatement, ListResource topic,
        List<ListResource> categories) {
        var categoryCompoundStatements = getCategoryCompoundStatements(topicCompoundStatement);
        categoryCompoundStatements.forEach(categoryCompoundStatement -> {
            var category = consultationListMapper.mapToCategory(topic, categoryCompoundStatement);

            List<Reference> entryReferences = new ArrayList<>();
            resourceReferenceUtil.extractChildReferencesFromCompoundStatement(categoryCompoundStatement, entryReferences);
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
            .filter(RCMRMT030101UK04Component::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(RCMRMT030101UK04Component3::hasEhrComposition)
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

    private Encounter mapToEncounter(
            RCMRMT030101UK04EhrComposition ehrComposition,
            Patient patient,
            String practiseCode,
            List<Location> entryLocations
    ) {
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

        setEncounterLocation(encounter, ehrComposition, entryLocations);

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
        } else if (effectiveTime.getCenter() != null
            && effectiveTime.getCenter().hasNullFlavor()
            && CsNullFlavor.UNK.value().equals(effectiveTime.getCenter().getNullFlavor().value())) {
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

    private void setEncounterLocation(Encounter encounter, RCMRMT030101UK04EhrComposition ehrComposition, List<Location> entryLocations) {
        if (ehrComposition.getLocation() != null) {

            var locationName = ehrComposition.getLocation().getLocatedEntity().getLocatedPlace().getName().toLowerCase();

            for (var entryLocation : entryLocations) {
                if (entryLocation.getName().toLowerCase().equals(locationName)) {
                    var id = entryLocation.getId();
                    var location = new EncounterLocationComponent();
                    location.setLocation(new Reference(LOCATION_REFERENCE.formatted(id)));
                    encounter.setLocation(List.of(location));
                }
            }
        }
    }

    private void addEntry(ListResource list, Reference reference) {
        list.addEntry(new ListEntryComponent(reference));
    }
}
