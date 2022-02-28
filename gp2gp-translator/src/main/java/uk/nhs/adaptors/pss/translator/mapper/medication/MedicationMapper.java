package uk.nhs.adaptors.pss.translator.mapper.medication;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
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
import uk.nhs.adaptors.pss.translator.service.IdGeneratorService;

@Service
@AllArgsConstructor
public class MedicationMapper {
    private static final String MEDICATION_URL = "Medication-1";
    private static final String HYPHEN = "-";
    private static final Map<String, String> MEDICATION_IDS = new HashMap<>();

    private CodeableConceptMapper codeableConceptMapper;
    private IdGeneratorService idGeneratorService;

    protected Medication createMedication(RCMRMT030101UK04Consumable consumable) {
        if (hasManufacturedMaterial(consumable)) {
            CD code = consumable.getManufacturedProduct().getManufacturedMaterial().getCode();

            Medication medication = new Medication();
            medication.setId(getMedicationId(code));
            medication.setMeta(generateMeta(MEDICATION_URL));
            medication.setCode(codeableConceptMapper.mapToCodeableConcept(code));

            return medication;
        }
        return null;
    }

    public String getMedicationId(CD code) {
        var key = keyBuilder(CD::hasCode, CD::getCode, code)
            + keyBuilder(CD::hasOriginalText, CD::getOriginalText, code)
            + keyBuilder(CD::hasDisplayName, CD::getDisplayName, code);
        var value = MEDICATION_IDS.getOrDefault(key, StringUtils.EMPTY);

        if (StringUtils.isNotBlank(value)) {
            return value;
        } else {
            var newId = idGeneratorService.generateUuid();
            MEDICATION_IDS.put(key, newId);
            return newId;
        }
    }

    private static String keyBuilder(Function<CD, Boolean> checker, Function<CD, String> getter, CD code) {
        if (checker.apply(code)) {
            return getter.apply(code) + HYPHEN;
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
                .map(this::getMedicationId)
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
