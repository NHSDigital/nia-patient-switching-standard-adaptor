package uk.nhs.adaptors.pss.translator.service;

import java.util.Collection;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRIN030000UK06Message;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.generator.BundleGenerator;
import uk.nhs.adaptors.pss.translator.mapper.AgentDirectoryMapper;
import uk.nhs.adaptors.pss.translator.mapper.PatientMapper;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleMapperService {

    private final BundleGenerator generator;

    private final AgentDirectoryMapper agentDirectoryMapper;
    private final PatientMapper patientMapper;

    public Bundle mapToBundle(RCMRIN030000UK06Message xmlMessage) {
        LOGGER.info("Mapping the Bundle");
        Bundle bundle = generator.generateBundle();

        RCMRMT030101UK04EhrFolder ehrFolder = getEhrFolder(xmlMessage);
        //List<RCMRMT030101UK04Component4> statementComponents = mapComponents(ehrFolder);

        var agents = mapAgents(ehrFolder);


        //bundle.addEntry(mapPatient(xmlMessage))

        return bundle;
    }

    //    private List<Bundle.BundleEntryComponent> mapAgentDirectories(RCMRMT030101UK04EhrExtract ehrExtract) {
    //       var ehrFolder = ehrExtract.getComponent().get(0).getEhrFolder();
    //
    //    }

    private List<? extends DomainResource> mapAgents(RCMRMT030101UK04EhrFolder ehrFolder) {
        return agentDirectoryMapper.mapAgentDirectory(ehrFolder.getResponsibleParty().getAgentDirectory());
    }

    private Bundle.BundleEntryComponent mapPatient(RCMRMT030101UK04EhrExtract ehrExtract, Organization organization) {
        RCMRMT030101UK04Patient xmlPatient = ehrExtract.getRecordTarget().getPatient();
        Patient mappedPatient = patientMapper.mapToPatient(xmlPatient, organization);
        return new Bundle.BundleEntryComponent().setResource(mappedPatient);
    }

    private List<RCMRMT030101UK04Component4> mapComponents(RCMRMT030101UK04EhrFolder ehrFolder) {
        return ehrFolder.getComponent()
            .stream()
            .map(component3 -> component3.getEhrComposition().getComponent())
            .flatMap(Collection::stream)
            .toList();
    }

    private RCMRMT030101UK04EhrFolder getEhrFolder(RCMRIN030000UK06Message xmlMessage) {
        return xmlMessage.getControlActEvent().getSubject().getEhrExtract().getComponent().get(0).getEhrFolder();
    }

    private RCMRMT030101UK04EhrExtract getEhrExtract(RCMRIN030000UK06Message xmlMessage) {
        return xmlMessage.getControlActEvent().getSubject().getEhrExtract();
    }
}
