package uk.nhs.adaptors.pss.translator.mapper.medication;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.CD;
import org.hl7.v3.deprecated.RCMRMT030101UKConsumable;
import org.hl7.v3.deprecated.RCMRMT030101UKManufacturedProduct;
import org.hl7.v3.deprecated.RCMRMT030101UKMaterial;
import org.hl7.v3.deprecated.RCMRMT030101UKMedicationStatement;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;
import uk.nhs.adaptors.pss.translator.util.DegradedCodeableConcepts;

@Service
@AllArgsConstructor
public class MedicationMapper {
    private static final String MEDICATION_URL = "Medication-1";

    private CodeableConceptMapper codeableConceptMapper;
    private MedicationMapperContext medicationMapperContext;

    public Medication createMedication(RCMRMT030101UKConsumable consumable) {
        if (hasManufacturedMaterial(consumable)) {
            CD code = consumable.getManufacturedProduct().getManufacturedMaterial().getCode();
            if (!medicationMapperContext.contains(code)) {
                Medication medication = new Medication();
                medication.setId(medicationMapperContext.getMedicationId(code));
                medication.setMeta(generateMeta(MEDICATION_URL));
                medication.setCode(codeableConceptMapper.mapToCodeableConceptForMedication(code));

                if (medication.getCode() == null) {
                    return medication;
                }

                DegradedCodeableConcepts.addDegradedEntryIfRequired(medication.getCode(), DegradedCodeableConcepts.DEGRADED_MEDICATION);

                return medication;
            }
        }
        return null;
    }

    public Optional<Reference> extractMedicationReference(RCMRMT030101UKMedicationStatement medicationStatement) {
        if (medicationStatement.hasConsumable()) {
            var medicationCode = medicationStatement.getConsumable()
                .stream()
                .filter(MedicationMapper::hasManufacturedMaterial)
                .map(RCMRMT030101UKConsumable::getManufacturedProduct)
                .map(RCMRMT030101UKManufacturedProduct::getManufacturedMaterial)
                .map(RCMRMT030101UKMaterial::getCode)
                .findFirst();

            return medicationCode
                .map(medicationMapperContext::getMedicationId)
                .map(id -> new IdType(ResourceType.Medication.name(), id))
                .map(Reference::new);
        }
        return Optional.empty();
    }

    private static boolean hasManufacturedMaterial(RCMRMT030101UKConsumable consumable) {
        return consumable.hasManufacturedProduct() && consumable.getManufacturedProduct().hasManufacturedMaterial()
            && consumable.getManufacturedProduct().getManufacturedMaterial().hasCode();
    }
}
