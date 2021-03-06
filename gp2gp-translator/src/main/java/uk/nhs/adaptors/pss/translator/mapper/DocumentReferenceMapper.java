package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Enumerations.DocumentReferenceStatus;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllNarrativeStatements;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;

@Slf4j
@Service
@AllArgsConstructor
public class DocumentReferenceMapper extends AbstractMapper<DocumentReference> {

    // TODO: Add file Size using the uncompressed/unencoded size of the document (NIAD-2030)

    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DocumentReference-1";
    private static final String ABSENT_ATTACHMENT = "AbsentAttachment";
    private static final String PLACEHOLDER_VALUE = "GP2GP generated placeholder. Original document not available. See notes for details";
    private static final String INVALID_CONTENT_TYPE = "Content type was not a valid MIME type";

    private CodeableConceptMapper codeableConceptMapper;

    public List<DocumentReference> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        List<Encounter> encounterList, Organization organization) {

        return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllNarrativeStatements(component)
                .filter(Objects::nonNull)
                .filter(ResourceFilterUtil::isDocumentReference)
                .map(narrativeStatement -> mapDocumentReference(narrativeStatement, composition, patient, extract, encounterList,
                    organization)))
            .toList();
    }

    private DocumentReference mapDocumentReference(RCMRMT030101UK04NarrativeStatement narrativeStatement,
        RCMRMT030101UK04EhrComposition ehrComposition, Patient patient, RCMRMT030101UK04EhrExtract ehrExtract,
        List<Encounter> encounterList, Organization organization) {

        DocumentReference documentReference = new DocumentReference();

        var id = narrativeStatement.getReference().get(0).getReferredToExternalDocument().getId().getRoot();

        documentReference.addIdentifier(buildIdentifier(id, organization.getIdentifierFirstRep().getValue()));
        documentReference.setId(id);
        documentReference.getMeta().addProfile(META_PROFILE);
        documentReference.setStatus(DocumentReferenceStatus.CURRENT);
        documentReference.setType(getType(narrativeStatement));
        documentReference.setSubject(new Reference(patient));
        documentReference.setIndexedElement(getIndexed(ehrExtract));
        documentReference.setDescription(buildDescription(narrativeStatement));
        documentReference.setCustodian(new Reference(organization));
        getAuthor(narrativeStatement, ehrComposition).ifPresent(documentReference::addAuthor);

        if (narrativeStatement.hasAvailabilityTime() && narrativeStatement.getAvailabilityTime().hasValue()) {
            documentReference.setCreatedElement(DateFormatUtil.parseToDateTimeType(narrativeStatement.getAvailabilityTime().getValue()));
        }

        var encounterReference = encounterList.stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .findFirst()
            .map(Reference::new);

        if (encounterReference.isPresent()) {
            DocumentReference.DocumentReferenceContextComponent documentReferenceContextComponent =
                new DocumentReference.DocumentReferenceContextComponent().setEncounter(encounterReference.get());

            documentReference.setContext(documentReferenceContextComponent);
        }

        setContentAttachments(documentReference, narrativeStatement);

        return documentReference;
    }

    private CodeableConcept getType(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        var referenceToExternalDocument = narrativeStatement.getReference().get(0).getReferredToExternalDocument();

        if (referenceToExternalDocument != null && referenceToExternalDocument.hasCode()) {
            if (!referenceToExternalDocument.getCode().hasOriginalText() && referenceToExternalDocument.getCode().hasDisplayName()) {
                return codeableConceptMapper.mapToCodeableConcept(referenceToExternalDocument.getCode())
                    .setText(referenceToExternalDocument.getCode().getDisplayName());
            } else if (referenceToExternalDocument.getCode().hasOriginalText()) {
                return codeableConceptMapper.mapToCodeableConcept(referenceToExternalDocument.getCode())
                    .setText(referenceToExternalDocument.getCode().getOriginalText());
            }
        }

        return null;
    }

    private InstantType getIndexed(RCMRMT030101UK04EhrExtract ehrExtract) {
        if (ehrExtract.hasAuthor()) {
            if (ehrExtract.getAuthor().hasTime() && ehrExtract.getAuthor().getTime().hasValue()) {
                return DateFormatUtil.parseToInstantType(ehrExtract.getAuthor().getTime().getValue());
            }

            return DateFormatUtil.parseToInstantType(ehrExtract.getAvailabilityTime().getValue());
        }

        return null;
    }

    private Optional<Reference> getAuthor(RCMRMT030101UK04NarrativeStatement narrativeStatement,
        RCMRMT030101UK04EhrComposition ehrComposition) {
        final Reference ref = getParticipantReference(narrativeStatement.getParticipant(), ehrComposition);
        if (ref != null) {
            return Optional.of(ref);
        }
        return Optional.empty();
    }

    private String buildDescription(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        String description = null;
        if (narrativeStatement.hasText()) {
            description = addLine(description, narrativeStatement.getText());
        }

        if (isAbsentAttachment(narrativeStatement)) {
            description = addLine(description, PLACEHOLDER_VALUE);
        } else {
            description = addLine(description,
                buildFileName(narrativeStatement.getReference().get(0)
                    .getReferredToExternalDocument().getText().getReference().getValue()));
        }

        return description;
    }

    public static String addLine(String text, String line) {
        return text == null ? line : text.concat(StringUtils.LF).concat(line);
    }

    private boolean isAbsentAttachment(RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        return narrativeStatement.getReference().get(0)
            .getReferredToExternalDocument().getText().getReference().getValue().contains(ABSENT_ATTACHMENT);
    }

    private void setContentAttachments(DocumentReference documentReference, RCMRMT030101UK04NarrativeStatement narrativeStatement) {
        var referenceToExternalDocument = narrativeStatement.getReference().get(0).getReferredToExternalDocument();
        var attachment = new Attachment();
        if (referenceToExternalDocument.hasText()) {
            var mediaType = referenceToExternalDocument.getText().getMediaType();

            if (!isAbsentAttachment(narrativeStatement)) {
                attachment.setUrl(referenceToExternalDocument.getText().getReference().getValue());
                attachment.setTitle(buildFileName(referenceToExternalDocument.getText().getReference().getValue()));
            } else {
                attachment.setTitle(PLACEHOLDER_VALUE);
            }

            if (isContentTypeValid(mediaType)) {
                attachment.setContentType(mediaType);
            } else {
                attachment.setContentType(PLACEHOLDER_VALUE);
                addContentTypeToNotes(documentReference);
                LOGGER.info("Content type: '{}' was not a valid MIME type", attachment.getContentType());
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

    // stubbed method for abstract class
    public List<DocumentReference> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        List<Encounter> encounterList, String practiseCode) {
        return null;
    }
}
