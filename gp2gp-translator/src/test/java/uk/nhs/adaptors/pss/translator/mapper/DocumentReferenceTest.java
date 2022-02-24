package uk.nhs.adaptors.pss.translator.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import java.util.List;

import org.hl7.fhir.dstu3.model.DocumentReference;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import lombok.SneakyThrows;

@ExtendWith(MockitoExtension.class)
public class DocumentReferenceTest {
    private static final String XML_RESOURCES_BASE = "xml/DocumentReference/";
    private static final String NARRATIVE_STATEMENT_ROOT_ID = "5E496953-065B-41F2-9577-BE8F2FBD0757";

    @InjectMocks
    private DocumentReferenceMapper documentReferenceMapper;

    @Test
    public void mapNarrativeStatementToDocumentReferenceWithValidData() {
        var ehrExtract = unmarshallEhrExtract("narrative_statement_has_referred_to_external_document.xml");
        List<DocumentReference> documentReferences = documentReferenceMapper.mapToDocumentReference(ehrExtract);
        var documentReference = documentReferences.get(0);

        assertFullValidData(documentReference);
    }

    private void assertFullValidData(DocumentReference documentReference) {
        assertThat(documentReference.getId()).isEqualTo(NARRATIVE_STATEMENT_ROOT_ID);
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallEhrExtract(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}
