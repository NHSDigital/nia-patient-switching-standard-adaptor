package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListEntryComponent;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.CD;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EncounterMapper {
    private static final List<String> INVALID_CODES = List.of("196401000000100", "196391000000103");
    private static final String ENCOUNTER_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Encounter-1";
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
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    private final CodeableConceptMapper codeableConceptMapper;
    private final ConsultationListMapper consultationListMapper;
    private final TopicListMapper topicListMapper;

    public Map<String, List<Object>> mapAllEncounters(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient) {
        List<RCMRMT030101UK04EhrComposition> ehrCompositionList = getEncounterEhrCompositions(ehrExtract);
        List<Encounter> encounterList = new ArrayList<>();
        List<ListResource> consultationList = new ArrayList<>();
        List<ListResource> topicList = new ArrayList<>();
        List<ListResource> categoryList = new ArrayList<>();

        Map<String, List<Object>> map = new HashMap<>();

        ehrCompositionList.forEach(ehrComposition -> {
            var encounter = mapToEncounter(ehrComposition, patient);
            var consultation = consultationListMapper.mapToConsultation(ehrExtract, encounter);

            var topicCompoundStatementList = getTopicCompoundStatements(ehrComposition);
            if (CollectionUtils.isEmpty(topicCompoundStatementList)) {
                // generate a 'flat' consultation
                var topic = topicListMapper.mapToTopic(consultation, null);

                consultation.addEntry(new ListResource.ListEntryComponent(new Reference(topic)));
                topicList.add(topic);
            } else {
                // generate a 'structured' topic for each child CompoundStatement
                topicCompoundStatementList.forEach(compoundStatement -> {
                    var topic = topicListMapper.mapToTopic(consultation, compoundStatement);

                    topicList.add(topic);
                    consultation.addEntry(new ListResource.ListEntryComponent(new Reference(topic)));
                });
            }

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

    private List<RCMRMT030101UK04CompoundStatement> getTopicCompoundStatements(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition
            .getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .filter(compoundStatement -> compoundStatement != null && compoundStatement.getClassCode().get(0).equals(TOPIC_CLASS_CODE))
            .collect(Collectors.toList());
    }

    private Encounter mapToEncounter(RCMRMT030101UK04EhrComposition ehrComposition, Patient patient) {
        var id = ehrComposition.getId().getRoot();

        var encounter = new Encounter();
        encounter
            .setParticipant(getEncounterParticipant(ehrComposition.getAuthor(), ehrComposition.getParticipant2()))
            .setStatus(EncounterStatus.FINISHED)
            .setSubject(new Reference(patient))
            .setType(getEncounterType(ehrComposition.getCode()))
            .setIdentifier(getEncounterIdentifier(id))
            .setMeta(getEncounterMeta())
            .setId(id);

        var period = getPeriod(ehrComposition.getEffectiveTime(), ehrComposition.getAvailabilityTime());
        if (period != null) {
            encounter.setPeriod(period);
        }

        var location = getLocation(ehrComposition);
        if (location != null) {
            encounter.setLocation(location);
        }

        return encounter;
    }

    private List<CodeableConcept> getEncounterType(CD code) {
        return List.of(codeableConceptMapper.mapToCodeableConcept(code));
    }

    private Meta getEncounterMeta() {
        return new Meta().addProfile(ENCOUNTER_META_PROFILE);
    }

    private List<Identifier> getEncounterIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
        return List.of(identifier);
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

    private List<EncounterParticipantComponent> getEncounterParticipant(RCMRMT030101UK04Author author,
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
}
