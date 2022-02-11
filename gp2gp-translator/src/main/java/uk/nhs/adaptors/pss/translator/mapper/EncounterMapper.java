package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Identifier;
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
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04Participant2;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.IdentifierUtil;

@Service
@AllArgsConstructor
public class EncounterMapper {
    private static final List<String> INVALID_CODES = List.of("196401000000100", "196391000000103");
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Encounter-1";
    private static final String PATIENT_REFERENCE_PREFIX = "Patient/";
    private static final String PRACTITIONER_REFERENCE_PREFIX = "Practitioner/";
    private static final String LOCATION_REFERENCE_PREFIX = "Location/";
    private static final String LOCATION_REFERENCE_SUFFIX = "-LOC";
    private static final String PERFORMER_SYSTEM = "http://hl7.org/fhir/v3/ParticipationType";
    private static final String PERFORMER_CODE = "PPRF";
    private static final String PERFORMER_DISPLAY = "primary performer";
    private static final String RECORDER_SYSTEM = "https://fhir.nhs.uk/STU3/CodeSystem/GPConnect-ParticipantType-1";
    private static final String RECORDER_CODE = "REC";
    private static final String RECORDER_DISPLAY = "recorder";

    private CodeableConceptMapper codeableConceptMapper;

    public List<Encounter> mapAllEncounters(RCMRMT030101UK04EhrExtract ehrExtract, String patientId) {
        List<Encounter> encounterList = new ArrayList<>();
        List<RCMRMT030101UK04EhrComposition> ehrCompositionList = getValidEhrCompositions(ehrExtract);

        ehrCompositionList.forEach(ehrComposition -> {
            encounterList.add(mapToEncounter(ehrComposition, patientId));
        });

        return encounterList;
    }

    private List<RCMRMT030101UK04EhrComposition> getValidEhrCompositions(RCMRMT030101UK04EhrExtract ehrExtract) {
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

    private Encounter mapToEncounter(RCMRMT030101UK04EhrComposition ehrComposition, String patientId) {
        var id = ehrComposition.getId().getRoot();
        var identifier = IdentifierUtil.getIdentifier(id);
        var type = codeableConceptMapper.mapToCodeableConcept(ehrComposition.getCode());
        var subject = new Reference(PATIENT_REFERENCE_PREFIX + patientId);
        var period = getPeriod(ehrComposition.getEffectiveTime(), ehrComposition.getAvailabilityTime());
        var participant = getParticipant(ehrComposition.getAuthor(), ehrComposition.getParticipant2());
        var location = getLocation(ehrComposition);

        return createEncounter(id, identifier, type, subject, period, participant, location);
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

        encounter.getMeta().getProfile().add(new UriType(META_PROFILE));
        encounter.getIdentifier().add(identifier);
        encounter.getType().add(type);
        encounter
            .setPeriod(period)
            .setParticipant(participant)
            .setStatus(EncounterStatus.FINISHED)
            .setSubject(subject)
            .setLocation(location)
            .setId(id);

        return encounter;
    }
}
