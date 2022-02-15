package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@AllArgsConstructor
public class ConsultationListMapper {
    private static final String CONSULTATION_META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-List-1";
    private static final String CONSULTATION_ID_SUFFIX = "-CONS";
    private static final String CONSULTATION_CODE_SYSTEM = "http://snomed.info/sct";
    private static final String CONSULTATION_CODE_CODE = "325851000000107";
    private static final String CONSULTATION_CODE_DISPLAY = "Consultation";
    private static final String CONSULTATION_ORDERED_BY_SYSTEM = "http://hl7.org/fhir/list-order";
    private static final String CONSULTATION_ORDERED_BY_CODE = "system";
    private static final String CONSULTATION_ORDERED_BY_DISPLAY = "Sorted by System";

    public ListResource mapToConsultation(RCMRMT030101UK04EhrExtract ehrExtract, Encounter encounter) {
        ListResource consultation = new ListResource();
        consultation
            .setStatus(ListResource.ListStatus.CURRENT)
            .setMode(ListResource.ListMode.SNAPSHOT)
            .setCode(getCoding(CONSULTATION_CODE_SYSTEM, CONSULTATION_CODE_CODE, CONSULTATION_CODE_DISPLAY))
            .setSubject(encounter.getSubject())
            .setDateElement(getConsultationDate(encounter.getPeriod(), ehrExtract))
            .setOrderedBy(getCoding(CONSULTATION_ORDERED_BY_SYSTEM, CONSULTATION_ORDERED_BY_CODE, CONSULTATION_ORDERED_BY_DISPLAY))
            .setEncounter(new Reference(encounter))
            .setMeta(getConsultationMeta())
            .setId(getConsultationId(encounter.getId()));

        setConsultationTitle(consultation, encounter.getType());

        return consultation;
    }

    private Meta getConsultationMeta() {
        return new Meta().addProfile(CONSULTATION_META_PROFILE);
    }

    private String getConsultationId(String id) {
        return id + CONSULTATION_ID_SUFFIX;
    }

    private void setConsultationTitle(ListResource consultation, List<CodeableConcept> codeableConceptList) {
        if (!CollectionUtils.isEmpty(codeableConceptList)) {
            var codeableConcept = codeableConceptList.get(0);
            if (codeableConcept.hasText()) {
                consultation.setTitle(codeableConcept.getText());
            } else if (codeableConcept.getCoding().get(0).hasDisplay()) {
                consultation.setTitle(codeableConcept.getCoding().get(0).getDisplay());
            }
        }
    }

    private DateTimeType getConsultationDate(Period period, RCMRMT030101UK04EhrExtract ehrExtract) {
        if (period != null && period.hasStart()) {
            return period.getStartElement();
        } else {
            return DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
        }
    }

    private CodeableConcept getCoding(String system, String code, String display) {
        Coding coding = new Coding(system, code, display);
        return new CodeableConcept(coding);
    }
}
