package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.dstu3.model.Enumerations.DocumentReferenceStatus;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class DocumentReferenceTest {
    private static final String XML_RESOURCES_BASE = "xml/DocumentReference/";
    private static final String NARRATIVE_STATEMENT_ROOT_ID = "5E496953-065B-41F2-9577-BE8F2FBD0757";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DocumentReference-1";
    private static final String CODING_DISPLAY = "Original Text document";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/TESTPRACTISECODE";
    private static final String NARRATIVE_STATEMENT_TYPE = "Record Attachment";
    private static final String FILENAME = "31B75ED0-6E88-11EA-9384-E83935108FD5_patient-attachment.txt";
    private static final String URL = "file://localhost/31B75ED0-6E88-11EA-9384-E83935108FD5_patient-attachment.txt";
    private static final String CONTENT_TYPE = "text/plain";
    private static final String ENCOUNTER_ID = "62A39454-299F-432E-993E-5A6232B4E099";
    private static final String PATIENT_ID = "45329454-299F-432E-993E-5A6232B4E099";
    private static final Organization AUTHOR_ORG = new Organization().addIdentifier(new Identifier().setValue("TESTPRACTISECODE"));
    private static final String PLACEHOLDER = "GP2GP generated placeholder. Original document not available. See notes for details";
    private static final int THREE = 3;

    @InjectMocks
    private DocumentReferenceMapper documentReferenceMapper;

    @Mock
    private CodeableConceptMapper codeableConceptMapper;

    @BeforeEach
    public void setup() {
        setUpCodeableConceptMock();
    }

    @Test
    public void mapNarrativeStatementToDocumentReferenceWithValidData() {
        var ehrExtract = unmarshallEhrExtract("narrative_statement_has_referred_to_external_document.xml");
        List<DocumentReference> documentReferences = documentReferenceMapper.mapResources(ehrExtract, createPatient(),
            getEncounterList(), AUTHOR_ORG);
        var documentReference = documentReferences.get(0);

        assertFullValidData(documentReference);
    }

    @Test
    public void mapNarrativeStatementToDocumentReferenceWithOptionalData() {
        var ehrExtract = unmarshallEhrExtract("narrative_statement_has_referred_to_external_document_with_optional_data.xml");
        List<DocumentReference> documentReferences = documentReferenceMapper.mapResources(ehrExtract, createPatient(),
            getEncounterList(), AUTHOR_ORG);
        var documentReference = documentReferences.get(0);

        assertOptionalValidData(documentReference);
    }

    @Test
    public void mapMultpleNarrativeStatementToDocumentReference() {
        var ehrExtract = unmarshallEhrExtract("multiple_narrative_statements_has_referred_to_external_document.xml");
        List<DocumentReference> documentReferences = documentReferenceMapper.mapResources(ehrExtract, createPatient(),
            getEncounterList(), AUTHOR_ORG);

        assertThat(documentReferences.size()).isEqualTo(THREE);
    }

    @Test
    public void mapNarrativeStatementToDocumentReferenceWithAttachments() {
        var ehrExtract = unmarshallEhrExtract("narrative_statement_has_referred_to_external_document.xml");
        List<DocumentReference> documentReferences = documentReferenceMapper.mapResources(ehrExtract, createPatient(),
            getEncounterList(), AUTHOR_ORG);
        var documentReference = documentReferences.get(0);

        assertAttachmentData(documentReference);
    }

    @Test
    public void mapNarrativeStatementToDocumentReferenceWithAbsentAttachment() {
        var ehrExtract = unmarshallEhrExtract("narrative_statement_has_referred_to_external_document_with_absent_attachment.xml");
        List<DocumentReference> documentReferences = documentReferenceMapper.mapResources(ehrExtract, createPatient(),
            getEncounterList(), AUTHOR_ORG);
        var documentReference = documentReferences.get(0);

        assertDocumentReferenceWithAbsentAttachment(documentReference);
    }

    @Test
    public void mapNarrativeStatementToDocumentReferenceWithInvalidEncounterReference() {
        var ehrExtract = unmarshallEhrExtract("narrative_statement_with_invalid_encounter.xml");
        List<DocumentReference> documentReferences = documentReferenceMapper.mapResources(ehrExtract, createPatient(),
            getEncounterList(), AUTHOR_ORG);
        var documentReference = documentReferences.get(0);

        assertDocumentReferenceWithInvalidEncounter(documentReference);
    }

    @Test
    public void mapNestedNarrativeStatement() {
        var ehrExtract = unmarshallEhrExtract("nested_narrative_statements.xml");
        List<DocumentReference> documentReferences = documentReferenceMapper.mapResources(ehrExtract, createPatient(),
            getEncounterList(), AUTHOR_ORG);
        var documentReference = documentReferences.get(0);

        assertDocumentReferenceMappedFromNestedNarrativeStatement(documentReference);
    }

    private void assertDocumentReferenceMappedFromNestedNarrativeStatement(DocumentReference documentReference) {
        assertThat(documentReference.getId()).isEqualTo(NARRATIVE_STATEMENT_ROOT_ID);
        assertThat(documentReference.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(documentReference.getStatus()).isEqualTo(DocumentReferenceStatus.CURRENT);
        assertThatIdentifierIsValid(documentReference.getIdentifierFirstRep(), documentReference.getId());
        assertThat(documentReference.getType().getText()).isEqualTo(NARRATIVE_STATEMENT_TYPE);
        assertThat(documentReference.getAuthor().get(0).getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(documentReference.getDescription()).isEqualTo("Some example text");
        assertThat(documentReference.getIndexedElement().getValue()).isEqualTo("2010-01-14");
        assertThat(documentReference.getCreatedElement().asStringValue()).isEqualTo("2019-07-08T13:35:00+00:00");
        assertThat(documentReference.getSubject().getResource()).isNotNull();
        assertThat(documentReference.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
        assertThat(documentReference.getContext().getEncounter().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    private void assertFullValidData(DocumentReference documentReference) {
        assertThat(documentReference.getId()).isEqualTo(NARRATIVE_STATEMENT_ROOT_ID);
        assertThat(documentReference.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(documentReference.getStatus()).isEqualTo(DocumentReferenceStatus.CURRENT);
        assertThatIdentifierIsValid(documentReference.getIdentifierFirstRep(), documentReference.getId());
        assertThat(documentReference.getType().getText()).isEqualTo(NARRATIVE_STATEMENT_TYPE);
        assertThat(documentReference.getAuthor().get(0).getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(documentReference.getDescription()).isEqualTo("Some example text");
        assertThat(documentReference.getIndexedElement().getValue()).isEqualTo("2010-01-14");
        assertThat(documentReference.getCreatedElement().asStringValue()).isEqualTo("2019-07-08T13:35:00+00:00");
        assertThat(documentReference.getSubject().getResource()).isNotNull();
        assertThat(documentReference.getSubject().getResource().getIdElement().getValue()).isEqualTo(PATIENT_ID);
        assertThat(documentReference.getContext().getEncounter().getResource().getIdElement().getValue()).isEqualTo(ENCOUNTER_ID);
    }

    private void assertOptionalValidData(DocumentReference documentReference) {
        assertThat(documentReference.getId()).isEqualTo(NARRATIVE_STATEMENT_ROOT_ID);
        assertThat(documentReference.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(documentReference.getStatus()).isEqualTo(DocumentReferenceStatus.CURRENT);
        assertThatIdentifierIsValid(documentReference.getIdentifierFirstRep(), documentReference.getId());
        assertThat(documentReference.getType().getText()).isEqualTo(NARRATIVE_STATEMENT_TYPE);
        assertThat(documentReference.getAuthor().get(0).getReference()).isEqualTo("Practitioner/2D70F602-6BB1-47E0-B2EC-39912A59787D");
        assertThat(documentReference.getDescription()).isEqualTo("31B75ED0-6E88-11EA-9384-E83935108FD5_patient-attachment.txt");
        assertThat(documentReference.getCreatedElement().asStringValue()).isEqualTo("2019-07-08T13:35:00+00:00");
        assertAttachmentData(documentReference);
    }

    private void assertAttachmentData(DocumentReference documentReference) {
        assertThat(documentReference.getContent().get(0).getAttachment().getTitle()).isEqualTo(FILENAME);
        assertThat(documentReference.getContent().get(0).getAttachment().getUrl()).isEqualTo(URL);
        assertThat(documentReference.getContent().get(0).getAttachment().getContentType()).isEqualTo(CONTENT_TYPE);
    }

    private void assertDocumentReferenceWithAbsentAttachment(DocumentReference documentReference) {
        assertThat(documentReference.getId()).isEqualTo(NARRATIVE_STATEMENT_ROOT_ID);
        assertThat(documentReference.getContent().get(0).getAttachment().getTitle()).isEqualTo(PLACEHOLDER);
        assertThat(documentReference.getContent().get(0).getAttachment().getUrl()).isNull();
        assertThat(documentReference.getContent().get(0).getAttachment().getContentType()).isEqualTo(CONTENT_TYPE);
    }

    private void assertDocumentReferenceWithInvalidEncounter(DocumentReference documentReference) {
        assertThat(documentReference.getId()).isEqualTo(NARRATIVE_STATEMENT_ROOT_ID);
        assertThat(documentReference.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(documentReference.getStatus()).isEqualTo(DocumentReferenceStatus.CURRENT);
        assertThatIdentifierIsValid(documentReference.getIdentifierFirstRep(), documentReference.getId());
        assertThat(documentReference.getType().getText()).isEqualTo(NARRATIVE_STATEMENT_TYPE);
        assertThat(documentReference.getContext().getEncounter().getResource()).isNull();
    }

    private void assertThatIdentifierIsValid(Identifier identifier, String id) {
        assertThat(identifier.getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(identifier.getValue()).isEqualTo(id);
    }

    private static Patient createPatient() {
        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        return patient;
    }

    private void setUpCodeableConceptMock() {
        var codeableConcept = new CodeableConcept();
        var coding = new Coding();
        coding.setDisplay(CODING_DISPLAY);
        codeableConcept.addCoding(coding);
        when(codeableConceptMapper.mapToCodeableConcept(any())).thenReturn(codeableConcept);
    }

    private List<Encounter> getEncounterList() {
        var encounter = new Encounter();
        encounter.setId(ENCOUNTER_ID);
        return List.of(encounter);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
