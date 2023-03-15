package uk.nhs.adaptors.pss.translator.mapper.medication;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Medication;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04Consumable;
import org.hl7.v3.RCMRMT030101UK04ManufacturedProduct;
import org.hl7.v3.RCMRMT030101UK04Material;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
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

    public Medication createMedication(RCMRMT030101UK04Consumable consumable) {
        if (hasManufacturedMaterial(consumable)) {
            CD code = consumable.getManufacturedProduct().getManufacturedMaterial().getCode();
            if (!medicationMapperContext.contains(code)) {
                Medication medication = new Medication();
                medication.setId(medicationMapperContext.getMedicationId(code));
                medication.setMeta(generateMeta(MEDICATION_URL));
                medication.setCode(codeableConceptMapper.mapToCodeableConceptForMedication(code));

                if (medication.getCode() != null && !medication.getCode().hasCoding()) {
                    medication.getCode()
                        .setCoding(List.of(DegradedCodeableConcepts.DEGRADED_MEDICATION));
                }
                return medication;
            }
        }
        return null;
    }

    public Optional<Reference> extractMedicationReference(RCMRMT030101UK04MedicationStatement medicationStatement) {
        if (medicationStatement.hasConsumable()) {
            var medicationCode = medicationStatement.getConsumable()
                .stream()
                .filter(MedicationMapper::hasManufacturedMaterial)
                .map(RCMRMT030101UK04Consumable::getManufacturedProduct)
                .map(RCMRMT030101UK04ManufacturedProduct::getManufacturedMaterial)
                .map(RCMRMT030101UK04Material::getCode)
                .findFirst();

            return medicationCode
                .map(medicationMapperContext::getMedicationId)
                .map(id -> new IdType(ResourceType.Medication.name(), id))
                .map(Reference::new);
        }
        return Optional.empty();
    }

    private static boolean hasManufacturedMaterial(RCMRMT030101UK04Consumable consumable) {
        return consumable.hasManufacturedProduct() && consumable.getManufacturedProduct().hasManufacturedMaterial()
            && consumable.getManufacturedProduct().getManufacturedMaterial().hasCode();
    }
}
