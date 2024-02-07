package uk.nhs.adaptors.pss.translator.mapper;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListMode;
import org.hl7.fhir.dstu3.model.ListResource.ListStatus;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UKCompoundStatement;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.nhs.adaptors.common.util.CodeableConceptUtils;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

import java.util.List;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsultationListMapper {
    private static final String LIST_META_PROFILE = "List-1";
    private static final String LIST_ORDERED_BY_SYSTEM = "http://hl7.org/fhir/list-order";
    private static final String LIST_ORDERED_BY_CODE = "system";
    private static final String LIST_ORDERED_BY_DISPLAY = "Sorted by System";
    private static final String CONSULTATION_ID_SUFFIX = "-CONS";
    private static final String LIST_CODE_SYSTEM = "http://snomed.info/sct";
    private static final String CONSULTATION_CODE_CODE = "325851000000107";
    private static final String CONSULTATION_CODE_DISPLAY = "Consultation";
    private static final String TOPIC_CODE_CODE = "25851000000105";
    private static final String TOPIC_CODE_DISPLAY = "Topic (EHR)";
    private static final String CATEGORY_CODE_CODE = "24781000000107";
    private static final String CATEGORY_CODE_DISPLAY = "Category (EHR)";

    private final IdGeneratorService idGenerator;
    private final CodeableConceptMapper codeableConceptMapper;

    public ListResource mapToConsultation(RCMRMT030101UKEhrExtract ehrExtract, Encounter encounter) {
        ListResource consultation = new ListResource();
        consultation
            .setStatus(ListStatus.CURRENT)
            .setMode(ListMode.SNAPSHOT)
            .setTitle(getConsultationTitle(encounter.getType()))
            .setCode(CodeableConceptUtils.createCodeableConcept(CONSULTATION_CODE_CODE, LIST_CODE_SYSTEM, CONSULTATION_CODE_DISPLAY, null))
            .setSubject(encounter.getSubject())
            .setDateElement(getConsultationDate(encounter.getPeriod(), ehrExtract))
            .setOrderedBy(CodeableConceptUtils.createCodeableConcept(LIST_ORDERED_BY_CODE, LIST_ORDERED_BY_SYSTEM,
                LIST_ORDERED_BY_DISPLAY, null))
            .setEncounter(new Reference(encounter))
            .setMeta(generateMeta(LIST_META_PROFILE))
            .setId(getConsultationId(encounter.getId()));

        return consultation;
    }

    private String getConsultationId(String id) {
        return id + CONSULTATION_ID_SUFFIX;
    }

    private String getConsultationTitle(List<CodeableConcept> codeableConceptList) {
        if (!CollectionUtils.isEmpty(codeableConceptList)) {
            var codeableConcept = codeableConceptList.get(0);
            if (codeableConcept.hasText()) {
                return codeableConcept.getText();
            } else if (codeableConcept.getCodingFirstRep().hasDisplay()) {
                return codeableConcept.getCodingFirstRep().getDisplay();
            }
        }

        return null;
    }

    private DateTimeType getConsultationDate(Period period, RCMRMT030101UKEhrExtract ehrExtract) {
        if (period != null && period.hasStart()) {
            return period.getStartElement();
        } else {
            return DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
        }
    }

    public ListResource mapToTopic(ListResource consultation, RCMRMT030101UKCompoundStatement compoundStatement) {
        ListResource topic = new ListResource();

        topic
            .setStatus(ListStatus.CURRENT)
            .setMode(ListMode.SNAPSHOT)
            .setTitle(getTitle(compoundStatement))
            .setCode(CodeableConceptUtils.createCodeableConcept(TOPIC_CODE_CODE, LIST_CODE_SYSTEM, TOPIC_CODE_DISPLAY, null))
            .setEncounter(consultation.getEncounter())
            .setSubject(consultation.getSubject())
            .setDateElement(getDate(compoundStatement, consultation))
            .setOrderedBy(CodeableConceptUtils.createCodeableConcept(LIST_ORDERED_BY_CODE, LIST_ORDERED_BY_SYSTEM,
                LIST_ORDERED_BY_DISPLAY, null))
            .setMeta(generateMeta(LIST_META_PROFILE))
            .setId(getTopicId(compoundStatement));

        return topic;
    }

    private String getTopicId(RCMRMT030101UKCompoundStatement compoundStatement) {
        return compoundStatement != null ? compoundStatement.getId().get(0).getRoot() : idGenerator.generateUuid();
    }

    public ListResource mapToCategory(ListResource topic, RCMRMT030101UKCompoundStatement compoundStatement) {
        ListResource category = new ListResource();

        var id = compoundStatement.getId() != null ? compoundStatement.getId().get(0).getRoot() : "";
        category
            .setStatus(ListStatus.CURRENT)
            .setMode(ListMode.SNAPSHOT)
            .setTitle(getTitle(compoundStatement))
            .setCode(CodeableConceptUtils.createCodeableConcept(CATEGORY_CODE_CODE, LIST_CODE_SYSTEM, CATEGORY_CODE_DISPLAY, null))
            .setEncounter(topic.getEncounter())
            .setSubject(topic.getSubject())
            .setDateElement(getDate(compoundStatement, topic))
            .setOrderedBy(CodeableConceptUtils.createCodeableConcept(LIST_ORDERED_BY_CODE, LIST_ORDERED_BY_SYSTEM,
                LIST_ORDERED_BY_DISPLAY, null))
            .setMeta(generateMeta(LIST_META_PROFILE))
            .setId(id);

        return category;
    }

    private DateTimeType getDate(RCMRMT030101UKCompoundStatement compoundStatement, ListResource parentList) {
        if (compoundStatement != null && compoundStatement.getAvailabilityTime() != null
            && compoundStatement.getAvailabilityTime().getValue() != null) {
            return DateFormatUtil.parseToDateTimeType(compoundStatement.getAvailabilityTime().getValue());
        }

        return parentList.getDateElement();
    }

    private String getTitle(RCMRMT030101UKCompoundStatement compoundStatement) {
        if (compoundStatement != null) {
            var codeableConcept = codeableConceptMapper.mapToCodeableConcept(compoundStatement.getCode());
            if (codeableConcept.hasText()) {
                return codeableConcept.getText();
            } else if (!compoundStatement.getCode().hasNullFlavor() && codeableConcept.getCodingFirstRep().hasDisplay()) {
                return codeableConcept.getCodingFirstRep().getDisplay();
            }
        }

        return null;
    }
}
