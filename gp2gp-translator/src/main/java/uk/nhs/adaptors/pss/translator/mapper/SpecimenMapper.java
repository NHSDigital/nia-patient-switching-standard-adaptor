package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;

import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Specimen;
import org.springframework.stereotype.Service;

@Service
public class SpecimenMapper {

    private static final String SPECIMEN_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Specimen-1";


    public List<Specimen> mapSpecimen() {

    }

    private Specimen createSpecimen() {

    }

    private Meta createMeta() {
        return new Meta().addProfile(SPECIMEN_META_PROFILE);
    }



}
