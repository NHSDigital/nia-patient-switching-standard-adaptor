package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListMode;
import org.hl7.fhir.dstu3.model.ListResource.ListStatus;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CsNullFlavor;
import org.hl7.v3.IVLTS;
import org.hl7.v3.IVXBTS;
import org.hl7.v3.RCMRMT030101UK04Author;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04Participant2;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;

@Service
@AllArgsConstructor
public class EncounterMapper {
    private static final List<String> INVALID_CODES = List.of("196401000000100", "196391000000103");
    private static final String ENCOUNTER_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Encounter-1";
    private static final String LIST_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-List-1";
    private static final String PRACTITIONER_REFERENCE_PREFIX = "Practitioner/";
    private static final String LOCATION_REFERENCE_PREFIX = "Location/";
    private static final String LOCATION_REFERENCE_SUFFIX = "-LOC";
    private static final String PERFORMER_SYSTEM = "http://hl7.org/fhir/v3/ParticipationType";
    private static final String PERFORMER_CODE = "PPRF";
    private static final String PERFORMER_DISPLAY = "primary performer";
    private static final String RECORDER_SYSTEM = "https://fhir.nhs.uk/STU3/CodeSystem/GPConnect-ParticipantType-1";
    private static final String RECORDER_CODE = "REC";
    private static final String RECORDER_DISPLAY = "recorder";
    private static final String TOPIC_CLASS_CODE = "TOPIC";
    private static final String ENCOUNTER_KEY = "encounters";
    private static final String CONSULTATION_KEY = "consultations";
    private static final String TOPIC_KEY = "topics";
    private static final String CATEGORY_KEY = "categories";
    private static final String CONSULTATION_ID_SUFFIX = "-CONS";
    private static final String LIST_CODE_SYSTEM = "http://snomed.info/sct";
    private static final String CONSULTATION_CODE_CODE = "325851000000107";
    private static final String CONSULTATION_CODE_DISPLAY = "Consultation";
    private static final String LIST_ORDERED_BY_SYSTEM = "http://hl7.org/fhir/list-order";
    private static final String LIST_ORDERED_BY_CODE = "system";
    private static final String LIST_ORDERED_BY_DISPLAY = "Sorted by System";
    private static final String TOPIC_CODE_CODE = "25851000000105";
    private static final String TOPIC_CODE_DISPLAY = "Topic (EHR)";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    private CodeableConceptMapper codeableConceptMapper;

    public Map<String, List<Object>> mapAllEncounters(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient) {
        List<RCMRMT030101UK04EhrComposition> ehrCompositionList = getEncounterEhrCompositions(ehrExtract);
        List<Encounter> encounterList = new ArrayList<>();
        List<ListResource> consultationList = new ArrayList<>();
        List<ListResource> topicList = new ArrayList<>();
        List<ListResource> categoryList = new ArrayList<>();

        Map<String, List<Object>> map = new HashMap<>();

        ehrCompositionList.forEach(ehrComposition -> {
            var encounter = mapToEncounter(ehrComposition, patient);
            var consultation = mapToConsultation(ehrExtract, encounter);

            var topicCompoundStatementList = getTopicCompoundStatements(ehrComposition);

            if (CollectionUtils.isEmpty(topicCompoundStatementList)) {
                // generate a 'flat' consultation
                var topic = mapToTopic(consultation, null);
                 topicList.add(topic);
            } else {
                // generate a 'structured' topic for each child CompoundStatement
                topicCompoundStatementList.forEach(compoundStatement -> {
                    var topic = mapToTopic(consultation, compoundStatement);
                    topicList.add(topic);
                });

            }

            // TODO reference each Topic in the parent consultation

            encounterList.add(encounter);
            consultationList.add(consultation);
        });

        map.put(ENCOUNTER_KEY, Collections.singletonList(encounterList));
        map.put(CONSULTATION_KEY, Collections.singletonList(consultationList));
        map.put(TOPIC_KEY, Collections.singletonList(topicList));

        return map;
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
            .filter(ehrComposition -> isEncounterEhrComposition(ehrComposition.getCode().getCode(), ehrComposition.getComponent()))
            .collect(Collectors.toList());
    }

    private boolean isEncounterEhrComposition(String code, List<RCMRMT030101UK04Component4> component) {
        return !INVALID_CODES.contains(code) && !component.stream().anyMatch(this::hasSuppressedContent);
    }

    private boolean hasSuppressedContent(RCMRMT030101UK04Component4 component) {
        return component.getEhrEmpty() != null || component.getRegistrationStatement() != null;
    }

    private Encounter mapToEncounter(RCMRMT030101UK04EhrComposition ehrComposition, Patient patient) {
        var id = ehrComposition.getId().getRoot();
        var identifier = getIdentifier(id);
        var type = codeableConceptMapper.mapToCodeableConcept(ehrComposition.getCode());
        var subject = new Reference(patient);
        var period = getPeriod(ehrComposition.getEffectiveTime(), ehrComposition.getAvailabilityTime());
        var participant = getParticipant(ehrComposition.getAuthor(), ehrComposition.getParticipant2());
        var location = getLocation(ehrComposition);

        return createEncounter(id, identifier, type, subject, period, participant, location);
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
        return identifier;
    }

    private Period getPeriod(IVLTS effectiveTime, TS availabilityTime) {
        Period period = new Period();
        var center = getTSStringValue(effectiveTime.getCenter());
        var low = getIVXBTSStringValue(effectiveTime.getLow());
        var high = getIVXBTSStringValue(effectiveTime.getHigh());
        var availabilityTimeValue = availabilityTime.getValue();

        if (validValue(center)) {
            return period.setStartElement(DateFormatUtil.parseToDateTimeType(center));
        } else if (validValue(low) && validValue(high)) {
            return period.setStartElement(DateFormatUtil.parseToDateTimeType(low)).setEndElement(DateFormatUtil.parseToDateTimeType(high));
        } else if (validValue(low) && !validValue(high)) {
            return period.setStartElement(DateFormatUtil.parseToDateTimeType(low));
        } else if (!validValue(low) && validValue(high) && !validValue(availabilityTimeValue)) {
            return period.setEndElement(DateFormatUtil.parseToDateTimeType(high));
        } else if (CsNullFlavor.UNK.value().equals(center)) {
            return null;
        } else if (validValue(availabilityTimeValue)) {
            return period.setStartElement(DateFormatUtil.parseToDateTimeType(availabilityTimeValue));
        }

        return null;
    }

    private String getTSStringValue(TS ts) {
        if (ts == null) {
            return null;
        } else if (ts.getValue() != null) {
            return ts.getValue();
        } else if (ts.getNullFlavor().equals(CsNullFlavor.UNK)) {
            return CsNullFlavor.UNK.value();
        }

        return null;
    }

    private String getIVXBTSStringValue(IVXBTS ivxbts) {
        if (ivxbts == null) {
            return null;
        } else if (ivxbts.getValue() != null) {
            return ivxbts.getValue();
        } else if (ivxbts.getNullFlavor().equals(CsNullFlavor.UNK)) {
            return CsNullFlavor.UNK.value();
        }

        return null;
    }

    private boolean validValue(String value) {
        return value != null && !CsNullFlavor.UNK.value().equals(value);
    }

    private EncounterParticipantComponent getRecorder(RCMRMT030101UK04Author author) {
        var recorder = new EncounterParticipantComponent();
        var coding = new Coding();

        coding
            .setSystem(RECORDER_SYSTEM)
            .setCode(RECORDER_CODE)
            .setDisplay(RECORDER_DISPLAY);

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

    private List<EncounterParticipantComponent> getParticipant(RCMRMT030101UK04Author author,
        List<RCMRMT030101UK04Participant2> participant2List) {
        List<EncounterParticipantComponent> participantList = new ArrayList<>();

        if (author.getNullFlavor() == null) {
            participantList.add(getRecorder(author));
        }

        // TODO: If author has a NullFlavor then create a recorder which references the Unknown Practitioner (NIAD-2026)

        var participant2 = participant2List.stream().filter(this::isNonNullParticipant2).findFirst();
        if (participant2.isPresent()) {
            participantList.add(getPerformer(participant2.get()));
        }

        return participantList;
    }

    private List<EncounterLocationComponent> getLocation(RCMRMT030101UK04EhrComposition ehrComposition) {
        if (ehrComposition.getLocation() != null) {
            var location = new EncounterLocationComponent();

            return List.of(location
                .setLocation(new Reference(
                    LOCATION_REFERENCE_PREFIX + ehrComposition.getId().getRoot() + LOCATION_REFERENCE_SUFFIX)));
        }

        return null;
    }

    private Encounter createEncounter(String id, Identifier identifier, CodeableConcept type, Reference subject, Period period,
        List<EncounterParticipantComponent> participant, List<EncounterLocationComponent> location) {
        var encounter = new Encounter();

        encounter.getMeta().getProfile().add(new UriType(ENCOUNTER_META_PROFILE));
        encounter.getIdentifier().add(identifier);
        encounter.getType().add(type);
        encounter
            .setParticipant(participant)
            .setStatus(EncounterStatus.FINISHED)
            .setSubject(subject)
            .setId(id);

        if (period != null) {
            encounter.setPeriod(period);
        }
        if (location != null) {
            encounter.setLocation(location);
        }

        return encounter;
    }

    private ListResource mapToConsultation(RCMRMT030101UK04EhrExtract ehrExtract, Encounter encounter) {
        var id = encounter.getId() + CONSULTATION_ID_SUFFIX;
        var title = getConsultationTitle(encounter.getType());
        var code = getCoding(LIST_CODE_SYSTEM, CONSULTATION_CODE_CODE, CONSULTATION_CODE_DISPLAY);
        var subject = encounter.getSubject();
        var date = getConsultationDate(encounter.getPeriod(), ehrExtract);
        var orderedBy = getCoding(LIST_ORDERED_BY_SYSTEM, LIST_ORDERED_BY_CODE, LIST_ORDERED_BY_DISPLAY);

        return createConsultationList(id, title, code, subject, date, orderedBy, encounter);
    }

    private String getConsultationTitle(List<CodeableConcept> codeableConceptList) {
        if (!CollectionUtils.isEmpty(codeableConceptList)) {
            var codeableConcept = codeableConceptList.get(0);
            if (codeableConcept.hasText()) {
                return codeableConcept.getText();
            } else if (codeableConcept.getCoding().get(0).hasDisplay()) {
                return codeableConcept.getCoding().get(0).getDisplay();
            }
        }

        return null;
    }

    private List<RCMRMT030101UK04CompoundStatement> getTopicCompoundStatements(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition
            .getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .filter(compoundStatement -> compoundStatement != null && compoundStatement.getClassCode().get(0).equals(TOPIC_CLASS_CODE))
            .collect(Collectors.toList());
    }

    private DateTimeType getConsultationDate(Period period, RCMRMT030101UK04EhrExtract ehrExtract) {
        if (period != null && period.hasStart()) {
            return period.getStartElement();
        } else {
            return DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
        }
    }

    private CodeableConcept getCoding(String system, String code, String display) {
        Coding coding = new Coding(system, code, display);
        return new CodeableConcept(coding);
    }

    private ListResource createConsultationList(String id, String title, CodeableConcept code, Reference subject, DateTimeType date,
        CodeableConcept orderedBy, Encounter encounter) {
        ListResource consultation = new ListResource();

        consultation.getMeta().getProfile().add(new UriType(LIST_META_PROFILE));
        consultation
            .setStatus(ListStatus.CURRENT)
            .setMode(ListMode.SNAPSHOT)
            .setCode(code)
            .setSubject(subject)
            .setDateElement(date)
            .setOrderedBy(orderedBy)
            .setEncounter(new Reference(encounter))
            .setId(id);

        if (title != null) {
            consultation.setTitle(title);
        }

        return consultation;
    }

    private ListResource mapToTopic(ListResource consultation, RCMRMT030101UK04CompoundStatement compoundStatement) {
        var id = getTopicId(compoundStatement);
        var title = getTopicTitle(compoundStatement);
        var code = getCoding(LIST_CODE_SYSTEM, TOPIC_CODE_CODE, TOPIC_CODE_DISPLAY);
        var encounter = consultation.getEncounter();
        var subject = consultation.getSubject();
        //        var date = getTopicDate()
        var orderedBy = getCoding(LIST_ORDERED_BY_SYSTEM, LIST_ORDERED_BY_CODE, LIST_ORDERED_BY_DISPLAY);

        return createTopicList(id, code, encounter, subject, orderedBy);
    }

    private String getTopicTitle(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return "HI";
    }

    private String getTopicId(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null ? compoundStatement.getId().get(0).getRoot() : "TEST"; // TODO: use fhirIdService
    }

    private ListResource createTopicList(String id, CodeableConcept code, Reference encounter, Reference subject,
        CodeableConcept orderedBy) {
        ListResource topic = new ListResource();

        topic.getMeta().getProfile().add(new UriType(LIST_META_PROFILE));
        topic
            .setStatus(ListStatus.CURRENT)
            .setMode(ListMode.SNAPSHOT)
            .setCode(code)
            .setEncounter(encounter)
            .setSubject(subject)
            .setOrderedBy(orderedBy)
            .setId(id);

        return topic;
    }
}
