package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.service.FhirIdGeneratorService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TopicListMapper {
    private static final String TOPIC_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-List-1";
    private static final String TOPIC_CODE_SYSTEM = "http://snomed.info/sct";
    private static final String TOPIC_ORDERED_BY_SYSTEM = "http://hl7.org/fhir/list-order";
    private static final String TOPIC_ORDERED_BY_CODE = "system";
    private static final String TOPIC_ORDERED_BY_DISPLAY = "Sorted by System";
    private static final String TOPIC_CODE_CODE = "25851000000105";
    private static final String TOPIC_CODE_DISPLAY = "Topic (EHR)";

    private final FhirIdGeneratorService idGenerator;
    private final CodeableConceptMapper codeableConceptMapper;

    public ListResource mapToTopic(ListResource consultation, RCMRMT030101UK04CompoundStatement compoundStatement) {
        ListResource topic = new ListResource();

        topic
            .setStatus(ListResource.ListStatus.CURRENT)
            .setMode(ListResource.ListMode.SNAPSHOT)
            .setCode(getCoding(TOPIC_CODE_SYSTEM, TOPIC_CODE_CODE, TOPIC_CODE_DISPLAY))
            .setEncounter(consultation.getEncounter())
            .setSubject(consultation.getSubject())
            .setDateElement(getTopicDate(compoundStatement, consultation))
            .setOrderedBy(getCoding(TOPIC_ORDERED_BY_SYSTEM, TOPIC_ORDERED_BY_CODE, TOPIC_ORDERED_BY_DISPLAY))
            .setMeta(getEncounterMeta())
            .setId(getTopicId(compoundStatement));

        setTopicTitle(topic, compoundStatement);

        return topic;
    }

    private DateTimeType getTopicDate(RCMRMT030101UK04CompoundStatement compoundStatement, ListResource consultation) {
        if (compoundStatement != null && compoundStatement.getAvailabilityTime().getValue() != null) {
            return DateFormatUtil.parseToDateTimeType(compoundStatement.getAvailabilityTime().getValue());
        }

        return consultation.getDateElement();
    }

    private void setTopicTitle(ListResource topic, RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (compoundStatement != null) {
            var codeableConcept = codeableConceptMapper.mapToCodeableConcept(compoundStatement.getCode());
            if (codeableConcept.hasText()) {
                topic.setTitle(codeableConcept.getText());
            } else if (codeableConcept.getCoding().get(0).hasDisplay()) {
                topic.setTitle(codeableConcept.getCoding().get(0).getDisplay());
            }
        }
    }

    private Meta getEncounterMeta() {
        return new Meta().addProfile(TOPIC_META_PROFILE);
    }

    private String getTopicId(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null ? compoundStatement.getId().get(0).getRoot() : idGenerator.generateUuid();
    }

    private static CodeableConcept getCoding(String system, String code, String display) {
        Coding coding = new Coding(system, code, display);
        return new CodeableConcept(coding);
    }
}
