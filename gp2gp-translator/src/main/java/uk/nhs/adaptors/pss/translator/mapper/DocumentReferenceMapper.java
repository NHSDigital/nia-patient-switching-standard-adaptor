package uk.nhs.adaptors.pss.translator.mapper;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.springframework.stereotype.Service;

import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;

import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;

@Service
public class DocumentReferenceMapper {
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DocumentReference-1";

    private CodeableConceptMapper codeableConceptMapper;

    public List<DocumentReference> mapToDocumentReference(RCMRMT030101UK04EhrExtract ehrExtract) {
        List<DocumentReference> mappedDocumentReferenceResources = new ArrayList<>();
        var ehrCompositionList = EhrResourceExtractorUtil.extractValidDocumentReferenceEhrCompositions(ehrExtract);

        ehrCompositionList.forEach(ehrComposition -> {
            var narrativeStatements = getNarrativeStatements(ehrComposition);

            narrativeStatements
                .forEach(narrativeStatement -> {
                    var mappedDocumentReferences = mapDocumentReference(narrativeStatement);
                    mappedDocumentReferenceResources.add(mappedDocumentReferences);
                });
        });

        return mappedDocumentReferenceResources;
    }

    private DocumentReference mapDocumentReference(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        DocumentReference documentReference = new DocumentReference();

        var id = narrativeStatement.getId().getRoot();
        var identifier = getIdentifier(id);

        documentReference.addIdentifier(identifier);
        documentReference.getMeta().addProfile(META_PROFILE);
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
        documentReference.setId(id);
        documentReference.setType(getType(narrativeStatement, documentReference));

        return documentReference;
    }

    private List<RCMRMT030101UK04NarrativeStatement> getNarrativeStatements(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getNarrativeStatement)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Identifier getIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
    }

    private CodeableConcept getType(RCMRMT030101UK04NarrativeStatement narrativeStatement, DocumentReference documentReference) {
        var referenceToExternalDocument = narrativeStatement.getReference().get(0).getReferredToExternalDocument();

        if (referenceToExternalDocument != null && referenceToExternalDocument.getCode() != null) {
            if (referenceToExternalDocument.getCode().getOriginalText() == null
                && referenceToExternalDocument.getCode().getDisplayName() != null) {
                //documentReference.setText();
                return codeableConceptMapper.mapToCodeableConcept(referenceToExternalDocument.getCode());
            } else if (referenceToExternalDocument.getCode().getOriginalText() != null) {
                //documentReference.setText();
                return codeableConceptMapper.mapToCodeableConcept(referenceToExternalDocument.getCode());
            }
        }

        return null;
    }
}
