package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UnknownPractitionerHandler {
    private static final String UNKNOWN_USER = "Unknown User";

    private final IdGeneratorService idGeneratorService;

    public void updateUnknownPractitionersRefs(Bundle bundle) {
        boolean used = false;
        Practitioner unknown = createUnknownPractitioner();
        for (BundleEntryComponent entry : bundle.getEntry()) {
            if (entry.getResource().getResourceType().equals(ResourceType.Observation)) {
                if (handleObservation((Observation) entry.getResource(), unknown)) {
                    used = true;
                }
            } else if (entry.getResource().getResourceType().equals(ResourceType.ProcedureRequest)) {
                if (handleProcedureRequest((ProcedureRequest) entry.getResource(), unknown)) {
                    used = true;
                }
            }
        }

        if (used) {
            bundle.addEntry(new BundleEntryComponent().setResource(unknown));
        }
    }

    private boolean handleObservation(Observation observation, Practitioner unknown) {
        if (!observation.hasPerformer()) {
            observation.addPerformer(new Reference(unknown));
            return true;
        }
        return false;
    }

    private boolean handleProcedureRequest(ProcedureRequest procedureRequest, Practitioner unknown) {
        if (!procedureRequest.hasPerformer()) {
            procedureRequest.setPerformer(new Reference(unknown));
            return true;
        }
        return false;
    }

    private Practitioner createUnknownPractitioner() {
        Practitioner unknown = new Practitioner();
        unknown.setId(idGeneratorService.generateUuid());
        return unknown
            .addName(new HumanName().setText(UNKNOWN_USER));
    }
}
