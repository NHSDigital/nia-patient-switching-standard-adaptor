package uk.nhs.adaptors.pss.translator.mapper;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.EncounterReferenceUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Slf4j
@Service
@AllArgsConstructor
public class DocumentReferenceMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DocumentReference-1";
    private static final String ABSENT_ATTACHMENT = "Absent Attachment";
    private static final String PLACEHOLDER_VALUE = "GP2GP generated placeholder. Original document not available. See notes for details";
    private static final String INVALID_CONTENT_TYPE = "Content type was not a valid MIME type";

    private CodeableConceptMapper codeableConceptMapper;


    public List<DocumentReference> mapToDocumentReference(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        Organization organization, Long fileSize, List<Encounter> encounterList, String practiseCode) {
        List<DocumentReference> mappedDocumentReferenceResources = new ArrayList<>();
        var ehrCompositionList = EhrResourceExtractorUtil.extractValidDocumentReferenceEhrCompositions(ehrExtract);

        ehrCompositionList.forEach(ehrComposition -> {
            var narrativeStatements = getNarrativeStatements(ehrComposition);

            narrativeStatements
                .forEach(narrativeStatement -> {
                    var mappedDocumentReferences = mapDocumentReference(narrativeStatement, patient, ehrExtract,
                        organization, fileSize, encounterList, practiseCode);
                    mappedDocumentReferenceResources.add(mappedDocumentReferences);
                });
        });

        return mappedDocumentReferenceResources;
    }

    private DocumentReference mapDocumentReference(RCMRMT030101UK04NarrativeStatement narrativeStatement, Patient patient,
        RCMRMT030101UK04EhrExtract ehrExtract, Organization organization, long fileSize,
        List<Encounter> encounterList, String practiseCode) {
        DocumentReference documentReference = new DocumentReference();

        var ehrComposition = ehrExtract.getComponent().get(0).getEhrFolder().getComponent().get(0).getEhrComposition();

        var id = narrativeStatement.getReference().get(0).getReferredToExternalDocument().getId().getRoot();

        documentReference.addIdentifier(buildIdentifier(id, practiseCode));
        documentReference.getMeta().addProfile(META_PROFILE);
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
        documentReference.setId(id);
        documentReference.setType(getType(narrativeStatement));
        documentReference.setSubject(new Reference(patient));
        documentReference.setIndexedElement(getIndexed(ehrExtract));
        documentReference.setAuthor(getAuthor(narrativeStatement, ehrComposition));
        documentReference.setDescription(buildDescription(narrativeStatement));

        if (organization != null && organization.hasIdElement()) {
            documentReference.setCustodian(createOrganizationReference(organization));
        }

        if (narrativeStatement.getAvailabilityTime() != null && narrativeStatement.getAvailabilityTime().getValue() != null) {
            documentReference.setCreatedElement(
                DateFormatUtil.parseToDateTimeType(narrativeStatement.getAvailabilityTime().getValue()));
        }

        var encounterReference =
            EncounterReferenceUtil.getEncounterReference(List.of(ehrComposition), encounterList, ehrComposition.getId().getRoot());

        if (encounterReference != null) {
            DocumentReference.DocumentReferenceContextComponent documentReferenceContextComponent
                = new DocumentReference.DocumentReferenceContextComponent().setEncounter(encounterReference);

            documentReference.setContext(documentReferenceContextComponent);
        }

        setContentAttachments(documentReference, narrativeStatement, fileSize);

        return documentReference;
    }

    private List<RCMRMT030101UK04NarrativeStatement> getNarrativeStatements(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getNarrativeStatement)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private CodeableConcept getType(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        var referenceToExternalDocument = narrativeStatement.getReference().get(0).getReferredToExternalDocument();

        if (referenceToExternalDocument != null && referenceToExternalDocument.getCode() != null) {
            if (referenceToExternalDocument.getCode().getOriginalText() == null
                && referenceToExternalDocument.getCode().getDisplayName() != null) {
                return codeableConceptMapper.mapToCodeableConcept(referenceToExternalDocument.getCode())
                    .setText(referenceToExternalDocument.getCode().getDisplayName());
            } else if (referenceToExternalDocument.getCode().getOriginalText() != null) {
                return codeableConceptMapper.mapToCodeableConcept(referenceToExternalDocument.getCode())
                    .setText(referenceToExternalDocument.getCode().getOriginalText());
            }
        }

        return null;
    }

    private InstantType getIndexed(RCMRMT030101UK04EhrExtract ehrExtract) {
        if (ehrExtract.getAuthor() != null) {
            if (ehrExtract.getAuthor().getTime() != null && ehrExtract.getAuthor().getNullFlavor() == null) {
                return DateFormatUtil.parseToInstantType(ehrExtract.getAuthor().getTime().getValue());
            }

            return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
        }

        return null;
    }

    private List<Reference> getAuthor(RCMRMT030101UK04NarrativeStatement narrativeStatement,
        RCMRMT030101UK04EhrComposition ehrComposition) {
        return List.of(ParticipantReferenceUtil.getParticipantReference(narrativeStatement.getParticipant(), ehrComposition));
    }

    private Reference createOrganizationReference(Organization organization) {
        return new Reference(organization.getIdElement());
    }

    private String buildDescription(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        var referenceToExternalDocumentText = narrativeStatement.getReference().get(0).getReferredToExternalDocument().getText();
        if (isAbsentAttachment(narrativeStatement)) {
            return PLACEHOLDER_VALUE;
        }
        if (referenceToExternalDocumentText != null) {
            String filename = referenceToExternalDocumentText.getReference().getValue();
            if (filename != null) {
                return buildFileName(filename);
            }
        }

        return null;
    }

    private boolean isAbsentAttachment(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        return narrativeStatement.getReference().get(0)
            .getReferredToExternalDocument()
            .getCode()
            .getOriginalText()
            .equals(ABSENT_ATTACHMENT);
    }

    private void setContentAttachments(DocumentReference documentReference, RCMRMT030101UK04NarrativeStatement narrativeStatement,
        long fileSize) {
        var referenceToExternalDocument = narrativeStatement.getReference().get(0).getReferredToExternalDocument();
        var attachment = new Attachment();
        if (referenceToExternalDocument.getText() != null) {
            var mediaType = referenceToExternalDocument.getText().getMediaType();

            if (!isAbsentAttachment(narrativeStatement)) {
                attachment.setUrl(referenceToExternalDocument.getText().getReference().getValue());
                attachment.setSize((int) fileSize);
                attachment.setTitle(buildFileName(referenceToExternalDocument.getText().getReference().getValue()));
            } else {
                attachment.setTitle(PLACEHOLDER_VALUE);
            }

            if (isContentTypeValid(mediaType)) {
                attachment.setContentType(mediaType);
            } else {
                attachment.setContentType(PLACEHOLDER_VALUE);
                addContentTypeToNotes(documentReference);
                LOGGER.info("Content type was not a valid MIME type");
            }

            documentReference.addContent(new DocumentReference.DocumentReferenceContentComponent(attachment));
        }
    }

    private void addContentTypeToNotes(DocumentReference documentReference) {
        if (documentReference.getDescription().isEmpty()) {
            documentReference.setDescription(INVALID_CONTENT_TYPE);
        } else {
            String previousDesc = documentReference.getDescription();
            String newDesc = previousDesc + StringUtils.SPACE + INVALID_CONTENT_TYPE;
            documentReference.setDescription(newDesc);
        }
    }

    private String buildFileName(String text) {
        return text.replaceAll("file://localhost/", "Filename: ");
    }

    private boolean isContentTypeValid(String mediaType) {
        String validContentTypeFormat = ".*/.*";
        return Pattern.matches(validContentTypeFormat, mediaType);
    }
}
