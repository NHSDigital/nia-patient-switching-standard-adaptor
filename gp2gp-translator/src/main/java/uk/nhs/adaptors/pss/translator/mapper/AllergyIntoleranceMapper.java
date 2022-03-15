package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceClinicalStatus.ACTIVE;
import static org.hl7.fhir.dstu3.model.AllergyIntolerance.AllergyIntoleranceVerificationStatus.UNCONFIRMED;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementResourceExtractors.extractAllCompoundStatements;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.ResourceFilterUtil;

@Service
@AllArgsConstructor
public class AllergyIntoleranceMapper extends AbstractMapper<AllergyIntolerance> {
    private static final String DRUG_ALLERGY_CODE = "14L..00";
    private static final String NON_DRUG_ALLERGY_CODE = "SN53.00";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-AllergyIntolerance-1";
    private static final String ENCOUNTER_URL = "http://hl7.org/fhir/StructureDefinition/encounter-associatedEncounter";
    private static final String CODING_EXTENSION_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-coding-sctdescid";

    private CodeableConceptMapper codeableConceptMapper;

    @Override
    public List<AllergyIntolerance> mapResources(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters,
        String practiseCode) {

        return mapEhrExtractToFhirResource(ehrExtract, (extract, composition, component) ->
            extractAllCompoundStatements(component)
                .filter(Objects::nonNull)
                .filter(ResourceFilterUtil::isAllergyIntolerance)
                .map(compoundStatement -> mapAllergyIntolerance(composition, compoundStatement, practiseCode, encounters, patient))
        ).toList();
    }

    public AllergyIntolerance mapAllergyIntolerance(RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04CompoundStatement compoundStatement, String practiseCode, List<Encounter> encounterList, Patient patientResource) {
        AllergyIntolerance allergyIntolerance = new AllergyIntolerance();

        var id = compoundStatement.getId().get(0).getRoot();
        var encounterReference = getEncounterReference(encounterList, ehrComposition.getId());

        allergyIntolerance.setId(id);
        allergyIntolerance.setMeta(generateMeta(META_PROFILE));
        allergyIntolerance.addIdentifier(buildIdentifier(id, practiseCode));
        allergyIntolerance.setClinicalStatus(ACTIVE);
        allergyIntolerance.setVerificationStatus(UNCONFIRMED);
        allergyIntolerance.setPatient(new Reference(patientResource));

        if (compoundStatement.getCode().getCode().equals(DRUG_ALLERGY_CODE)) {
            //allergyIntolerance.setCategory(List.of((Enumeration<AllergyIntolerance.AllergyIntoleranceCategory>) List.of(MEDICATION)));
            createAllergyIntoleranceCodeForDrugAllergy(allergyIntolerance, compoundStatement);
        } else if (compoundStatement.getCode().getCode().equals(NON_DRUG_ALLERGY_CODE)) {
            //allergyIntolerance.setCategory(List.of((Enumeration<AllergyIntolerance.AllergyIntoleranceCategory>) List.of(ENVIRONMENT)));
            createAllergyIntoleranceCodeForNonDrugAllergy(allergyIntolerance, ehrComposition);
        }

        if (!encounterReference.isEmpty()) {
            allergyIntolerance.addExtension(createEncounterExtension(encounterReference));
        }

        if (compoundStatement.hasEffectiveTime()) {
            allergyIntolerance.setOnset(getOnsetDate(compoundStatement));
        }

        return allergyIntolerance;
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

        } else if (effectiveTime.hasCenter()) {

        } else if (effectiveTime.hasHigh()) {

        }

        return null;
    }

    private boolean hasMatchingId(String encounterId, II ehrCompositionId) {
        return encounterId.equals(ehrCompositionId.getRoot());
    }
}
