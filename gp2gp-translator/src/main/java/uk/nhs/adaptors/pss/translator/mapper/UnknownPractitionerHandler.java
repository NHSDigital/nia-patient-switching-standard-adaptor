package uk.nhs.adaptors.pss.translator.mapper;

import static java.util.Arrays.asList;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UnknownPractitionerHandler {
    private static final String UNKNOWN_USER = "Unknown User";
    private static final String RECORDER_SYSTEM = "https://fhir.nhs.uk/STU3/CodeSystem/GPConnect-ParticipantType-1";
    private static final String RECORDER_CODE = "REC";
    private static final String RECORDER_DISPLAY = "recorder";

    private final IdGeneratorService idGeneratorService;

    public void updateUnknownPractitionersRefs(Bundle bundle) {
        boolean used = false;
        Practitioner unknown = createUnknownPractitioner();
        for (BundleEntryComponent entry : bundle.getEntry()) {
            if (handleMissingPractitioner(entry.getResource(), unknown)) {
                used = true;
            }
        }

        if (used) {
            bundle.addEntry(new BundleEntryComponent().setResource(unknown));
        }
    }

    private boolean handleMissingPractitioner(Resource resource, Practitioner unknown) {
        if (ResourceType.Observation == resource.getResourceType()) {
            Observation observation = (Observation) resource;
            if (!observation.hasPerformer()) {
                observation.addPerformer(new Reference(unknown));
                return true;
            }
        } else if (ResourceType.ProcedureRequest == resource.getResourceType()) {
            ProcedureRequest procedureRequest = (ProcedureRequest) resource;
            if (!procedureRequest.hasPerformer()) {
                procedureRequest.setPerformer(new Reference(unknown));
                return true;
            }
        } else if (ResourceType.Encounter == resource.getResourceType()) {
            Encounter encounter = (Encounter) resource;
            if (!hasRecorder(encounter)) {
                encounter.addParticipant(new EncounterParticipantComponent()
                    .setIndividual(new Reference(unknown))
                    .setType(asList((new CodeableConcept(new Coding(RECORDER_SYSTEM, RECORDER_CODE, RECORDER_DISPLAY))))));
                return true;
            }
        } else if (ResourceType.Condition == resource.getResourceType()) {
            Condition condition = (Condition) resource;
            if (!condition.hasAsserter()) {
                condition.setAsserter(new Reference(unknown));
                return true;
            }
        }

        return false;
    }

    private static boolean hasRecorder(Encounter encounter) {
        for (EncounterParticipantComponent participantComponent : encounter.getParticipant()) {
            if (RECORDER_CODE.equals(participantComponent.getTypeFirstRep().getCodingFirstRep().getCode())) {
                return true;
            }
        }
        return false;
    }

    private Practitioner createUnknownPractitioner() {
        Practitioner unknown = new Practitioner();
        unknown.setId(idGeneratorService.generateUuid());
        return unknown.addName(new HumanName().setText(UNKNOWN_USER));
    }
}
