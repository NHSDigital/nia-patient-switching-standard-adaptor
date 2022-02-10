package uk.nhs.adaptors.pss.translator.mapper;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.springframework.stereotype.Component;

import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Component
public class ImmunizationMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Immunization-1";
    private static final String IMMUNIZATION_SNOMED_CODE = "2.16.840.1.113883.2.1.3.2.3.15";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String VACCINE_PROCEDURE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect" +
        "-VaccinationProcedure-1";
    private static final String END_DATE_PREFIX = "End Date: ";
    private static final String URL = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-coding-sctdescid";

    private final CodeableConceptMapper codeableConceptMapper = new CodeableConceptMapper();

    public List<Immunization> mapToImmunization(RCMRMT030101UK04ObservationStatement observationStatement, Patient patient,
        Encounter encounter, Practitioner practitioner, RCMRMT030101UK04EhrComposition ehrComposition) {

        if (hasImmunizationCode(observationStatement)) {
            var id = observationStatement.getId().getRoot();
            var identifier = getIdentifier(id);
            var note = buildNote(observationStatement);
            var date = getObservationDate(observationStatement);

            Extension recordedTimeExtension = getRecordedTimeExtension(ehrComposition);
            Extension vaccineExtension = createVaccineProcedureExtension(observationStatement);

            return createImmunization(id, identifier, note, vaccineExtension, date, recordedTimeExtension);
        }

        return null;
    }

    private Extension getRecordedTimeExtension(RCMRMT030101UK04EhrComposition ehrComposition) {
        if (ehrComposition.getAuthor() != null) {
            return new Extension()
                .setValue(new StringType(ehrComposition.getAuthor().getTime().getValue()));
        } else if (ehrComposition.getEffectiveTime() != null) {
            return new Extension()
                .setValue(new StringType(DateFormatUtil.parse(ehrComposition.getAvailabilityTime().getValue()).asStringValue()));
        }

        return null;
    }

    private Identifier getIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
    }

    private boolean hasImmunizationCode(RCMRMT030101UK04ObservationStatement observationStatement) {
        String snomedCode = observationStatement.getCode().getCodeSystem();

        return IMMUNIZATION_SNOMED_CODE.equals(snomedCode);
    }

    private Extension createVaccineProcedureExtension(RCMRMT030101UK04ObservationStatement observationStatement) {
        return new Extension()
            .setUrl(VACCINE_PROCEDURE_URL)
            .setValue(new Extension()
                .setUrl(URL)
                .setValue(codeableConceptMapper.mapToCodeableConcept(observationStatement.getCode()))
            );
    }

    private Date getObservationDate(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.getEffectiveTime().getLow() != null && observationStatement.getEffectiveTime().getHigh() != null) {
            return DateFormatUtil.parse(observationStatement.getEffectiveTime().getLow().getValue()).getValue();
        } else if (observationStatement.getEffectiveTime().getCenter() != null) {
            return DateFormatUtil.parse(observationStatement.getEffectiveTime().getCenter().getValue()).getValue();
        } else if (observationStatement.getEffectiveTime().getLow() != null) {
            return DateFormatUtil.parse(observationStatement.getEffectiveTime().getLow().getValue()).getValue();
        } else if (observationStatement.getEffectiveTime().getHigh() != null
            && observationStatement.getAvailabilityTime() != null
            && observationStatement.getAvailabilityTime().getValue() != null) {
            return DateFormatUtil.parse(observationStatement.getAvailabilityTime().getValue()).getValue();
        } else if (observationStatement.getEffectiveTime() == null && observationStatement.getAvailabilityTime().getValue() != null) {
            return DateFormatUtil.parse(observationStatement.getAvailabilityTime().getValue()).getValue();
        }

        return null;
    }

    private String highValueToNotes(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.getEffectiveTime().getLow() != null && observationStatement.getEffectiveTime().getHigh() != null) {
            return END_DATE_PREFIX + observationStatement.getEffectiveTime().getHigh().getValue();
        } else if (observationStatement.getEffectiveTime().getHigh() != null
            && observationStatement.getAvailabilityTime().getValue() != null) {
            return END_DATE_PREFIX + observationStatement.getEffectiveTime().getHigh().getValue();
        } else if (observationStatement.getEffectiveTime().getHigh() != null
            && observationStatement.getAvailabilityTime().getNullFlavor() != null) {
            return END_DATE_PREFIX + observationStatement.getEffectiveTime().getHigh().getValue();
        }

        return null;
    }

    private Annotation buildNote(RCMRMT030101UK04ObservationStatement observationStatement) {

        var pertinentText = getPertinentAnnotation(observationStatement);
        var dateText = highValueToNotes(observationStatement);

        if (pertinentText != null && dateText != null) {
            var noteStringType = new StringType(pertinentText + StringUtils.SPACE + dateText);
            return new Annotation(noteStringType);
        } else if (pertinentText != null) {
            var noteStringType = new StringType(pertinentText);
            return new Annotation(noteStringType);
        } else if (dateText != null) {
            var noteStringType = new StringType(dateText);
            return new Annotation(noteStringType);
        }

        return null;
    }

    private String getPertinentAnnotation(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.getPertinentInformation() != null && observationStatement.getPertinentInformation().size() > 0) {
            var pertinentInformation = observationStatement.getPertinentInformation().get(0);

            if (pertinentInformation.getPertinentAnnotation() != null && pertinentInformation.getPertinentAnnotation().getText() != null) {
                return pertinentInformation.getPertinentAnnotation().getText();
            }
        }

        return null;
    }

    private List<Immunization> createImmunization(String id, Identifier identifier, Annotation note,
        Extension vaccineExtension, Date date, Extension recordedTimeExtension) {
        var immunization = new Immunization();

        immunization.getMeta().getProfile().add(new UriType(META_PROFILE));
        immunization.getIdentifier().add(identifier);
        immunization.getNote().add(note);

        immunization.setId(id);
        immunization.getExtension().add(vaccineExtension);
        immunization.getExtension().add(recordedTimeExtension);

        immunization
            .setStatus(Immunization.ImmunizationStatus.COMPLETED)
            .setNotGiven(false)
            .setPrimarySource(false)
            .setDate(date);

        return List.of(immunization);
    }
}
