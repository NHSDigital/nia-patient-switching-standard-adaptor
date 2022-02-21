package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.ListResource.ListMode;
import org.hl7.fhir.dstu3.model.ListResource.ListStatus;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ListMapper {
    private static final String LIST_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-List-1";
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

    public ListResource mapToConsultation(RCMRMT030101UK04EhrExtract ehrExtract, Encounter encounter) {
        ListResource consultation = new ListResource();
        consultation
            .setStatus(ListStatus.CURRENT)
            .setMode(ListMode.SNAPSHOT)
            .setTitle(getConsultationTitle(encounter.getType()))
            .setCode(getListCoding(LIST_CODE_SYSTEM, CONSULTATION_CODE_CODE, CONSULTATION_CODE_DISPLAY))
            .setSubject(encounter.getSubject())
            .setDateElement(getConsultationDate(encounter.getPeriod(), ehrExtract))
            .setOrderedBy(getListCoding(LIST_ORDERED_BY_SYSTEM, LIST_ORDERED_BY_CODE, LIST_ORDERED_BY_DISPLAY))
            .setEncounter(new Reference(encounter))
            .setMeta(getListMeta())
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
            } else if (codeableConcept.getCoding().get(0).hasDisplay()) {
                return codeableConcept.getCoding().get(0).getDisplay();
            }
        }

        return null;
    }

    private DateTimeType getConsultationDate(Period period, RCMRMT030101UK04EhrExtract ehrExtract) {
        if (period != null && period.hasStart()) {
            return period.getStartElement();
        } else {
            return DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
        }
    }

    public ListResource mapToTopic(ListResource consultation, RCMRMT030101UK04CompoundStatement compoundStatement) {
        ListResource topic = new ListResource();

        topic
            .setStatus(ListStatus.CURRENT)
            .setMode(ListMode.SNAPSHOT)
            .setTitle(getTitle(compoundStatement))
            .setCode(getListCoding(LIST_CODE_SYSTEM, TOPIC_CODE_CODE, TOPIC_CODE_DISPLAY))
            .setEncounter(consultation.getEncounter())
            .setSubject(consultation.getSubject())
            .setDateElement(getDate(compoundStatement, consultation))
            .setOrderedBy(getListCoding(LIST_ORDERED_BY_SYSTEM, LIST_ORDERED_BY_CODE, LIST_ORDERED_BY_DISPLAY))
            .setMeta(getListMeta())
            .setId(getTopicId(compoundStatement));

        return topic;
    }

    private String getTopicId(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null ? compoundStatement.getId().get(0).getRoot() : idGenerator.generateUuid(); // TODO: double check
    }

    public ListResource mapToCategory(ListResource topic, RCMRMT030101UK04CompoundStatement compoundStatement) {
        ListResource category = new ListResource();

        category
            .setStatus(ListStatus.CURRENT)
            .setMode(ListMode.SNAPSHOT)
            .setTitle(getTitle(compoundStatement))
            .setCode(getListCoding(LIST_CODE_SYSTEM, CATEGORY_CODE_CODE, CATEGORY_CODE_DISPLAY))
            .setEncounter(topic.getEncounter())
            .setSubject(topic.getSubject())
            .setDateElement(getDate(compoundStatement, topic))
            .setOrderedBy(getListCoding(LIST_ORDERED_BY_SYSTEM, LIST_ORDERED_BY_CODE, LIST_ORDERED_BY_DISPLAY))
            .setMeta(getListMeta())
            .setId(compoundStatement.getId().get(0).getRoot());

        return category;
    }

    private DateTimeType getDate(RCMRMT030101UK04CompoundStatement compoundStatement, ListResource parentList) {
        if (compoundStatement != null && compoundStatement.getAvailabilityTime().getValue() != null) {
            return DateFormatUtil.parseToDateTimeType(compoundStatement.getAvailabilityTime().getValue());
        }

        return parentList.getDateElement();
    }

    private String getTitle(RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (compoundStatement != null) {
            var codeableConcept = codeableConceptMapper.mapToCodeableConcept(compoundStatement.getCode());
            if (codeableConcept.hasText()) {
                return codeableConcept.getText();
            } else if (codeableConcept.getCoding().get(0).hasDisplay()) {
                return codeableConcept.getCoding().get(0).getDisplay();
            }
        }

        return null;
    }

    private Meta getListMeta() {
        return new Meta().addProfile(LIST_META_PROFILE);
    }

    private CodeableConcept getListCoding(String system, String code, String display) {
        Coding coding = new Coding(system, code, display);
        return new CodeableConcept(coding);
    }
}
