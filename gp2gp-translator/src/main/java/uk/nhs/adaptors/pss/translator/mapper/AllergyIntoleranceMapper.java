package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllCompoundStatements;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil.getParticipantReference;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.TS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AllergyIntoleranceMapper extends AbstractMapper<AllergyIntolerance> {
    private static final String DRUG_ALLERGY_CODE = "14L..00";
    private static final String NON_DRUG_ALLERGY_CODE = "SN53.00";
    // the meta profile is pre-processed with the url so only the final section is required here
    private static final String META_PROFILE = "AllergyIntolerance-1";
    private static final String ENCOUNTER_URL = "http://hl7.org/fhir/StructureDefinition/encounter-associatedEncounter";
    private static final String EGTON_CODE_SYSTEM = "2.16.840.1.113883.2.1.6.3";
    private static final String ALLERGY_TERM_TEXT = "H/O: drug allergy";
    private static final String ALLERGY_NOTE = "Allergy Code: %s";

    private final CodeableConceptMapper codeableConceptMapper;

    @Override
    public List<AllergyIntolerance> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {
        return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllCompoundStatements(component)
                .filter(Objects::nonNull)
                .filter(ResourceFilterUtil::isAllergyIntolerance)
                .map(compoundStatement -> mapAllergyIntolerance(ehrExtract, composition, compoundStatement, practiseCode, encounters,
                    patient))
        ).toList();
    }

    private AllergyIntolerance mapAllergyIntolerance(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04CompoundStatement compoundStatement, String practiseCode, List<Encounter> encounters, Patient patient) {
        AllergyIntolerance allergyIntolerance = new AllergyIntolerance();

        var id = compoundStatement.getComponent().get(0).getObservationStatement().getId().getRoot();

        allergyIntolerance
            .addCategory(getCategory(compoundStatement))
            .setAssertedDateElement(getAssertedDateElement(compoundStatement.getAvailabilityTime(), ehrExtract, ehrComposition))
            .setPatient(new Reference(patient))
            .setClinicalStatus(ACTIVE)
            .setVerificationStatus(UNCONFIRMED)
            .addIdentifier(buildIdentifier(id, practiseCode))
            .setMeta(generateMeta(META_PROFILE))
            .setId(id);

        buildOnset(compoundStatement, allergyIntolerance);
        buildParticipantReferences(ehrComposition, compoundStatement, allergyIntolerance);
        buildExtension(ehrComposition, encounters, allergyIntolerance);
        buildNote(allergyIntolerance, compoundStatement);
        buildCode(allergyIntolerance, compoundStatement);

        return allergyIntolerance;
    }

    private void buildParticipantReferences(RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04CompoundStatement compoundStatement, AllergyIntolerance allergyIntolerance) {
        var practitioner = Optional.ofNullable(getParticipantReference(compoundStatement.getParticipant(), ehrComposition));

        practitioner.ifPresent(reference -> allergyIntolerance
            .setRecorder(reference)
            .setAsserter(reference));
    }

    private void buildExtension(RCMRMT030101UK04EhrComposition ehrComposition, List<Encounter> encounters,
        AllergyIntolerance allergyIntolerance) {
        encounters
            .stream()
            .filter(encounter -> encounter.getId().equals(ehrComposition.getId().getRoot()))
            .map(Reference::new)
            .findFirst()
            .ifPresent(reference -> allergyIntolerance.addExtension(new Extension(ENCOUNTER_URL, reference)));
    }

    private AllergyIntolerance.AllergyIntoleranceCategory getCategory(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getCode().getCode().equals(DRUG_ALLERGY_CODE) ? MEDICATION : ENVIRONMENT;
    }

    private Annotation getAllergyNote(CodeableConcept codeableConceptFromCode, CodeableConcept codeableConceptFromValue) {
        var codeDisplayName = codeableConceptFromCode.getCodingFirstRep().getDisplay();
        if (codeDisplayName != null
            && !ALLERGY_TERM_TEXT.equals(codeDisplayName)
            && !codeDisplayName.equals(codeableConceptFromValue.getCodingFirstRep().getDisplay())) {

            return new Annotation().setText(ALLERGY_NOTE.formatted(codeDisplayName));
        }
        return null;
    }

    private CodeableConcept getCodeableConceptFromNonEgtonCodeValue(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.hasValue()
            && observationStatement.getValue() instanceof CD value
            && !EGTON_CODE_SYSTEM.equals(value.getCodeSystem())) {

            return codeableConceptMapper.mapToCodeableConcept(value);
        }
        return null;
    }

    private void buildCode(AllergyIntolerance allergyIntolerance, RCMRMT030101UK04CompoundStatement compoundStatement) {
        var observationStatement = compoundStatement.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component02::getObservationStatement)
            .findFirst()
            .get();

        var codeableConceptFromCode = codeableConceptMapper.mapToCodeableConcept(observationStatement.getCode());
        var compoundCode = compoundStatement.getCode().getCode();

        if (NON_DRUG_ALLERGY_CODE.equals(compoundCode)) {
            allergyIntolerance.setCode(codeableConceptFromCode);

            if (allergyIntolerance.getCode() == null) {
                return;
            }

            DegradedCodeableConcepts.addDegradedEntryIfRequired(allergyIntolerance.getCode(),
                DegradedCodeableConcepts.DEGRADED_NON_DRUG_ALLERGY);
        }

        if (DRUG_ALLERGY_CODE.equals(compoundCode)) {
            var codeableConceptFromValue = getCodeableConceptFromNonEgtonCodeValue(observationStatement);
            if (codeableConceptFromValue == null && codeableConceptFromCode == null) {
                return;
            }
            if (codeableConceptFromValue == null) {
                allergyIntolerance.setCode(codeableConceptFromCode);

                buildAllergyIntoleranceText(observationStatement, allergyIntolerance);

                DegradedCodeableConcepts.addDegradedEntryIfRequired(
                    allergyIntolerance.getCode(),
                    DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
                return;
            }

            allergyIntolerance.setCode(codeableConceptFromValue);

            var allergyNote = getAllergyNote(codeableConceptFromCode, codeableConceptFromValue);
            if (allergyNote != null) {
                allergyIntolerance.getNote().add(allergyNote);
            }
        } else {
            allergyIntolerance.setCode(codeableConceptFromCode);
        }

        if (allergyIntolerance.getCode() == null) {
            return;
        }

        buildAllergyIntoleranceText(observationStatement, allergyIntolerance);

        DegradedCodeableConcepts.addDegradedEntryIfRequired(
            allergyIntolerance.getCode(),
            DegradedCodeableConcepts.DEGRADED_DRUG_ALLERGY);
    }

    private void buildOnset(RCMRMT030101UK04CompoundStatement compoundStatement, AllergyIntolerance allergyIntolerance) {
        var effectiveTime = compoundStatement.getEffectiveTime();
        var availabilityTime = compoundStatement.getAvailabilityTime();

        if (effectiveTime.hasLow()) {
            allergyIntolerance.setOnset(parseToDateTimeType(effectiveTime.getLow().getValue()));
        } else if (effectiveTime.hasCenter()) {
            allergyIntolerance.setOnset(parseToDateTimeType(effectiveTime.getCenter().getValue()));
        } else if (availabilityTime.hasValue()) {
            allergyIntolerance.setOnset(parseToDateTimeType(availabilityTime.getValue()));
        }
    }

    private DateTimeType getAssertedDateElement(TS availabilityTime, RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04EhrComposition ehrComposition) {
        if (availabilityTime != null && availabilityTime.hasValue()) {
            return parseToDateTimeType(availabilityTime.getValue());
        } else {
            if (ehrComposition.getAvailabilityTime() != null && ehrComposition.getAvailabilityTime().hasValue()) {
                return parseToDateTimeType(ehrComposition.getAvailabilityTime().getValue());
            } else if (ehrExtract.getAvailabilityTime() != null && ehrExtract.getAvailabilityTime().hasValue()) {
                return parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
            }
        }

        return null;
    }

    private void buildAllergyIntoleranceText(RCMRMT030101UK04ObservationStatement observationStatement,
        AllergyIntolerance allergyIntolerance) {
        if (allergyIntolerance.getCode() != null) {
            if (observationStatement.hasValue() && observationStatement.getValue() instanceof CD value) {
                var valueDisplayName = value.getDisplayName();
                if (!StringUtils.isEmpty(valueDisplayName)) {
                    allergyIntolerance.getCode().setText(valueDisplayName);
                    return;
                }
            }
            var originalTextFromCode = observationStatement.getCode().getOriginalText();
            if (!StringUtils.isEmpty(originalTextFromCode)) {
                allergyIntolerance.getCode().setText(originalTextFromCode);
            }
        }
    }

    private void buildNote(AllergyIntolerance allergyIntolerance, RCMRMT030101UK04CompoundStatement compoundStatement) {
        compoundStatement.getComponent().get(0)
            .getObservationStatement()
            .getPertinentInformation()
            .forEach(pertinentInformation ->
                allergyIntolerance
                    .addNote(new Annotation().setText(pertinentInformation.getPertinentAnnotation().getText()))
        );
    }
}
