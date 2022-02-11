package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CsNullFlavor;
import org.hl7.v3.IVXBTS;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.EhrResourceExtractorUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;

@Service
@AllArgsConstructor
public class ImmunizationMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Immunization-1";
    private static final String IMMUNIZATION_SNOMED_CODE = "2.16.840.1.113883.2.1.3.2.3.15";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String VACCINE_PROCEDURE_URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect"
        + "-VaccinationProcedure-1";
    private static final String END_DATE_PREFIX = "End Date: ";
    private static final String VACCINATION_CODING_EXTENSION_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-coding"
        + "-sctdescid";
    private static final String PATIENT_REFERENCE_PREFIX = "Patient/";
    private static final String ENCOUNTER_REFERENCE_PREFIX = "Encounter/";

    private CodeableConceptMapper codeableConceptMapper;

    public List<Immunization> mapToImmunization(RCMRMT030101UK04EhrExtract ehrExtract, String patientID, Optional<String> encounterID) {
        List<Immunization> mappedImmunizationResources = new ArrayList<>();
        var ehrCompositionList = EhrResourceExtractorUtil.extractValidImmunizationEhrCompositions(ehrExtract);

        ehrCompositionList.forEach(ehrComposition -> {
            var immunizationObservationStatements = getImmunizationObservationStatements(ehrComposition);

            immunizationObservationStatements
                .forEach(observationStatement -> {
                    var mappedImmunization = mapImmunization(ehrComposition, observationStatement, patientID, encounterID);
                    mappedImmunizationResources.add(mappedImmunization);
                });
        });

        return mappedImmunizationResources;
    }

    private Immunization mapImmunization(RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04ObservationStatement observationStatement,
        String patientID, Optional<String> encounterID) {

        ImmunizationMapperParameters immunizationMapperParameters = new ImmunizationMapperParameters();

        var id = observationStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var note = buildNote(observationStatement);
        var date = getObservationDate(observationStatement);

        Extension recordedTimeExtension = getRecordedTimeExtension(ehrComposition);
        Extension vaccineExtension = createVaccineProcedureExtension(observationStatement);

        var practitioner = List.of(ParticipantReferenceUtil.getParticipantReference(observationStatement.getParticipant(),
            ehrComposition));

        var patient = new Reference(PATIENT_REFERENCE_PREFIX + patientID);
        var encounter = new Reference();

        encounter = new Reference(encounterID.map(encId -> ENCOUNTER_REFERENCE_PREFIX + encId).orElse(null));

        immunizationMapperParameters.setIdParam(id);
        immunizationMapperParameters.setIdentifierParam(identifier);
        immunizationMapperParameters.setNoteParam(note);
        immunizationMapperParameters.setDateParam(date);
        immunizationMapperParameters.setRecordedTimeExtensionParam(recordedTimeExtension);
        immunizationMapperParameters.setVaccineExtensionParam(vaccineExtension);
        immunizationMapperParameters.setPatientParam(patient);
        immunizationMapperParameters.setEncounterParam(encounter);
        immunizationMapperParameters.setPractitionerParam(practitioner);

        return createImmunization(immunizationMapperParameters);
    }

    private List<RCMRMT030101UK04ObservationStatement> getImmunizationObservationStatements(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component4::getObservationStatement)
            .filter(this::hasImmunizationCode)
            .collect(Collectors.toList());
    }

    private Extension getRecordedTimeExtension(RCMRMT030101UK04EhrComposition ehrComposition) {
        if (ehrComposition.getAuthor() != null) {
            return new Extension()
                .setValue(new StringType(ehrComposition.getAuthor().getTime().getValue()));
        } else if (ehrComposition.getEffectiveTime() != null) {
            if (ehrComposition.getAvailabilityTime().getNullFlavor() == null) {
                return new Extension()
                    .setValue(new StringType(DateFormatUtil.parseToDateTimeType(ehrComposition.getAvailabilityTime().getValue()).asStringValue()));
            }
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
                .setUrl(VACCINATION_CODING_EXTENSION_URL)
                .setValue(codeableConceptMapper.mapToCodeableConcept(observationStatement.getCode()))
            );
    }

    private String getTSStringValue(TS ts) {
        if (ts == null) {
            return null;
        } else if (ts.getValue() != null) {
            return ts.getValue();
        } else if (ts.getNullFlavor().equals(CsNullFlavor.UNK)) {
            return CsNullFlavor.UNK.value();
        }

        return null;
    }

    private String getIVXBTSStringValue(IVXBTS ivxbts) {
        if (ivxbts == null) {
            return null;
        } else if (ivxbts.getValue() != null) {
            return ivxbts.getValue();
        } else if (ivxbts.getNullFlavor().equals(CsNullFlavor.UNK)) {
            return CsNullFlavor.UNK.value();
        }

        return null;
    }

    private Date getObservationDate(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.getEffectiveTime() != null) {
            var center = getTSStringValue(observationStatement.getEffectiveTime().getCenter());
            var low = getIVXBTSStringValue(observationStatement.getEffectiveTime().getLow());
            var high = getIVXBTSStringValue(observationStatement.getEffectiveTime().getHigh());
            var availabilityTimeValue = observationStatement.getAvailabilityTime();

            if (center != null) {
                return DateFormatUtil.parseToDateTimeType(center).getValue();
            } else if (low != null && high != null) {
                return DateFormatUtil.parseToDateTimeType(low).getValue();
            } else if (low != null) {
                return DateFormatUtil.parseToDateTimeType(low).getValue();
            } else if (high != null && availabilityTimeValue.getValue() != null && availabilityTimeValue.getNullFlavor() == null) {
                return DateFormatUtil.parseToDateTimeType(availabilityTimeValue.getValue()).getValue();
            }
        }

        return null;
    }

    private String highValueToNotes(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.getEffectiveTime() != null) {
            var low = getIVXBTSStringValue(observationStatement.getEffectiveTime().getLow());
            var high = getIVXBTSStringValue(observationStatement.getEffectiveTime().getHigh());
            var availabilityTimeValue = observationStatement.getAvailabilityTime();

            if (low != null && high != null) {
                return END_DATE_PREFIX + high;
            } else if (high != null && availabilityTimeValue.getValue() != null) {
                return END_DATE_PREFIX + high;
            } else if (high != null && availabilityTimeValue.getNullFlavor() != null) {
                return END_DATE_PREFIX + high;
            }
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

    private Immunization createImmunization(ImmunizationMapperParameters immunizationMapperParameters) {
        var immunization = new Immunization();

        immunization.getMeta().getProfile().add(new UriType(META_PROFILE));
        immunization.getIdentifier().add(immunizationMapperParameters.getIdentifierParam());
        immunization.getExtension().add(immunizationMapperParameters.getVaccineExtensionParam());
        immunization.getExtension().add(immunizationMapperParameters.getRecordedTimeExtensionParam());
        immunization
            .setStatus(Immunization.ImmunizationStatus.COMPLETED)
            .setNotGiven(false)
            .setPrimarySource(false)
            .setPatient(immunizationMapperParameters.getPatientParam())
            .setId(immunizationMapperParameters.getIdParam());

        if (immunizationMapperParameters.getDateParam() != null) {
            immunization.setDate(immunizationMapperParameters.getDateParam());
        }
        if (immunizationMapperParameters.getEncounterParam() != null) {
            immunization.setEncounter(immunizationMapperParameters.getEncounterParam());
        }
        if (immunizationMapperParameters.getNoteParam() != null) {
            immunization.getNote().add(immunizationMapperParameters.getNoteParam());
        }
        if (immunizationMapperParameters.getPractitionerParam() != null) {
            immunization.setPractitioner(immunizationMapperParameters.practitionerParam);
        }

        return immunization;
    }


    @Getter
    @Setter
    public static class ImmunizationMapperParameters {
        private String idParam;
        private Identifier identifierParam;
        private Annotation noteParam;
        private Extension vaccineExtensionParam;
        private Date dateParam;
        private Extension recordedTimeExtensionParam;
        private Reference patientParam;
        private Reference encounterParam;
        private List practitionerParam;

        public ImmunizationMapperParameters() {

        }
    }
}
