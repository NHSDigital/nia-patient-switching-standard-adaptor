package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllCompoundStatements;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.v3.CD;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.ParticipantReferenceUtil;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;

@Service
@AllArgsConstructor
public class AllergyIntoleranceMapper extends AbstractMapper<AllergyIntolerance> {
    private static final String DRUG_ALLERGY_CODE = "14L..00";
    private static final String NON_DRUG_ALLERGY_CODE = "SN53.00";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-AllergyIntolerance-1";
    private static final String ENCOUNTER_URL = "http://hl7.org/fhir/StructureDefinition/encounter-associatedEncounter";
    private static final String CODING_EXTENSION_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-coding-sctdescid";
    private static final String CODE_SYSTEM = "2.16.840.1.113883.2.1.6.3";
    private static final String ALLERGY_TERM_TEXT = "H/O: drug allergy";
    private static final String ALLERGY_NOTE = "Allergy Code: %s";

    private CodeableConceptMapper codeableConceptMapper;

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

    public AllergyIntolerance mapAllergyIntolerance(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04CompoundStatement compoundStatement, String practiseCode, List<Encounter> encounterList, Patient patientResource) {
        AllergyIntolerance allergyIntolerance = new AllergyIntolerance();

        var id = compoundStatement.getId().get(0).getRoot();
        var encounterReference = getEncounterReference(encounterList, ehrComposition.getId());
        var practitioner = ParticipantReferenceUtil.getParticipantReference(compoundStatement.getParticipant(), ehrComposition);

        allergyIntolerance.setId(id);
        allergyIntolerance.setMeta(generateMeta(META_PROFILE));
        allergyIntolerance.addIdentifier(buildIdentifier(id, practiseCode));
        allergyIntolerance.setClinicalStatus(ACTIVE);
        allergyIntolerance.setVerificationStatus(UNCONFIRMED);
        allergyIntolerance.setPatient(new Reference(patientResource));
        allergyIntolerance.setAssertedDateElement(getAssertedDateElement(ehrExtract, ehrComposition));
        allergyIntolerance.setRecorder(practitioner);

        if (compoundStatement.getCode().getCode().equals(DRUG_ALLERGY_CODE)) {
            allergyIntolerance.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION);
            createAllergyIntoleranceCodeForDrugAllergy(allergyIntolerance, compoundStatement);
        } else if (compoundStatement.getCode().getCode().equals(NON_DRUG_ALLERGY_CODE)) {
            allergyIntolerance.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT);
            createAllergyIntoleranceCodeForNonDrugAllergy(allergyIntolerance, ehrComposition);
        }

        if (encounterReference != null) {
            allergyIntolerance.addExtension(createEncounterExtension(encounterReference));
        }

        if (compoundStatement.hasEffectiveTime()) {
            allergyIntolerance.setOnset(getOnsetDate(compoundStatement));
        }

        if (practitioner != null) {
            allergyIntolerance.setAsserter(practitioner);
        }

        buildNote(allergyIntolerance, compoundStatement);
        buildCode(allergyIntolerance, compoundStatement);

        return allergyIntolerance;
    }

    private void buildCode(AllergyIntolerance allergyIntolerance, RCMRMT030101UK04CompoundStatement compoundStatement) {
        var observationStatement =
            compoundStatement.getComponent().stream().map(RCMRMT030101UK04Component02::getObservationStatement).findFirst().get();
        var codeableConceptFromCode = codeableConceptMapper.mapToCodeableConcept(observationStatement.getCode());
        var compoundCode = compoundStatement.getCode().getCode();

        if (NON_DRUG_ALLERGY_CODE.equals(compoundCode)) {
            allergyIntolerance.setCode(codeableConceptFromCode);
        }

        if (DRUG_ALLERGY_CODE.equals(compoundCode)) {
            if (observationStatement.hasValue() && observationStatement.getValue() instanceof CD value) {
                if (CODE_SYSTEM.equals(value.getCodeSystem())) {
                    var codeableConceptFromValue = codeableConceptMapper.mapToCodeableConcept(value);
                    allergyIntolerance.setCode(codeableConceptFromValue);

                    var codeDisplayName = codeableConceptFromCode.getCodingFirstRep().getDisplay();
                    if (!ALLERGY_TERM_TEXT.equals(codeDisplayName) && codeDisplayName.equals(codeableConceptFromValue.getCodingFirstRep().getDisplay())) {
                        allergyIntolerance.getNote().add(new Annotation().setText(ALLERGY_NOTE.formatted(codeDisplayName)));
                    }
                } else {
                    allergyIntolerance.setCode(codeableConceptFromCode);
                }
            } else {
                allergyIntolerance.setCode(codeableConceptFromCode);
            }
        }
    }

    private void createAllergyIntoleranceCodeForDrugAllergy(AllergyIntolerance allergyIntolerance,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (compoundStatement.getComponent().get(0).getObservationStatement().hasValue()
            && !compoundStatement.getComponent().get(0).getObservationStatement().getValue().equals("@xsi:type='CD' ")) {

        }
    }

    private void createAllergyIntoleranceCodeForNonDrugAllergy(AllergyIntolerance allergyIntolerance,
        RCMRMT030101UK04EhrComposition ehrComposition) {
        allergyIntolerance.setCode(
            codeableConceptMapper.mapToCodeableConcept(ehrComposition.getComponent().get(0)
                .getCompoundStatement()
                .getCode()
            )
        );
    }

    private Extension createEncounterExtension(Reference encounterReference) {
        return new Extension()
            .setUrl(ENCOUNTER_URL)
            .setValue(encounterReference);
    }

    private Reference getEncounterReference(List<Encounter> encounterList, II ehrCompositionId) {
        if (ehrCompositionId != null) {
            var matchingEncounter = encounterList.stream()
                .filter(encounter -> hasMatchingId(encounter.getId(), ehrCompositionId))
                .findFirst();

            if (matchingEncounter.isPresent()) {
                return new Reference(matchingEncounter.get());
            }
        }

        return null;
    }

    private DateTimeType getOnsetDate(RCMRMT030101UK04CompoundStatement compoundStatement) {
        var effectiveTime = compoundStatement.getEffectiveTime();

        if (effectiveTime.hasLow()) {
            return DateFormatUtil.parseToDateTimeType(effectiveTime.getLow().getValue());
        } else if (effectiveTime.hasCenter()) {
            return DateFormatUtil.parseToDateTimeType(effectiveTime.getCenter().getValue());
        } else if (effectiveTime.hasHigh()) {
            return DateFormatUtil.parseToDateTimeType(effectiveTime.getHigh().getValue());
        }

        return null;
    }

    private DateTimeType getAssertedDateElement(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition) {
        if (ehrComposition.hasAuthor()) {
            return DateFormatUtil.parseToDateTimeType(ehrComposition.getAuthor().getTime().getValue());
        }

        return DateFormatUtil.parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue());
    }

    private void buildNote(AllergyIntolerance allergyIntolerance, RCMRMT030101UK04CompoundStatement compoundStatement) {
        compoundStatement.getComponent().get(0)
            .getObservationStatement()
            .getPertinentInformation()
            .forEach(rcmrmt030101UK04PertinentInformation02 ->
                allergyIntolerance.setNote(List.of(
                    new Annotation(
                        new StringType(rcmrmt030101UK04PertinentInformation02.getPertinentAnnotation().getText())
                    )
                ))
            );
    }

    private boolean hasMatchingId(String encounterId, II ehrCompositionId) {
        return encounterId.equals(ehrCompositionId.getRoot());
    }
}
