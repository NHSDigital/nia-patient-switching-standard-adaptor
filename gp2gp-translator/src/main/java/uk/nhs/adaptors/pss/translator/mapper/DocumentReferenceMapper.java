package uk.nhs.adaptors.pss.translator.mapper;

import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04Reference;
import org.springframework.stereotype.Service;

import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;

@Service
public class DocumentReferenceMapper {
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DocumentReference-1";


    public DocumentReference mapToDocumentReference(RCMRMT030101UK04NarrativeStatement narrativeStatement,
        RCMRMT030101UK04EhrExtract ehrExtract) {

        DocumentReference documentReference = new DocumentReference();

        var id = narrativeStatement.getId().getRoot();
        var identifier = getIdentifier(id);

        documentReference.addIdentifier(identifier);
        documentReference.getMeta().addProfile(META_PROFILE);
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.valueOf("Current"));
        documentReference.setId(id);
        //documentReference.setType();

        return documentReference;
    }

    private Identifier getIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
    }

    private CodeableConcept getType(RCMRMT030101UK04NarrativeStatement narrativeStatement, RCMRMT030101UK04EhrExtract ehrExtract) {
        var type = narrativeStatement.getReference().stream()
            .map(RCMRMT030101UK04Reference::getType)
            .toList();

        return new CodeableConcept();
    }
}
