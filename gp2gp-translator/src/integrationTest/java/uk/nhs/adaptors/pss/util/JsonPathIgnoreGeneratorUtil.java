package uk.nhs.adaptors.pss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class JsonPathIgnoreGeneratorUtil {

    private static final List<IgnoreParameters> IGNORED_RESOURCES_BUILDER = List.of(
        new IgnoreParameters(ResourceType.Medication, "id"),
        new IgnoreParameters(ResourceType.MedicationRequest, "medicationReference.reference",
            JsonPathIgnoreGeneratorUtil::hasMedicationReference),
        new IgnoreParameters(ResourceType.MedicationStatement, "medicationReference.reference",
            JsonPathIgnoreGeneratorUtil::hasMedicationReference)
    );

    public static List<String> generateJsonPathIgnores(Bundle fhirBundle) {
        var resources = fhirBundle.getEntry();
        List<String> ignores = new ArrayList<>();
        for (int i = 0; i < resources.size(); i++) {
            String ignore = "entry[" + i + "].resource.";
            var resource = resources.get(i);
            for (IgnoreParameters resourceTypeAndField : IGNORED_RESOURCES_BUILDER) {
                if (resourceTypeAndField.getResourceType().equals(resource.getResource().getResourceType())) {
                    if (resourceTypeAndField.hasChecker() && resourceTypeAndField.getChecker().apply(resource.getResource())) {
                        ignores.add(ignore + resourceTypeAndField.getFieldName());
                    } else if (!resourceTypeAndField.hasChecker()) {
                        ignores.add(ignore + resourceTypeAndField.getFieldName());
                    }
                }
            }
        }
        return ignores;
    }

    private static boolean hasMedicationReference(Resource resource) {
        if (ResourceType.MedicationRequest.equals(resource.getResourceType())) {
            return ((MedicationRequest) resource).hasMedicationReference();
        }
        if (ResourceType.MedicationStatement.equals(resource.getResourceType())) {
            return ((MedicationStatement) resource).hasMedicationReference();
        }
        return false;
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    private static class IgnoreParameters {
        private final ResourceType resourceType;
        private final String fieldName;
        private Function<Resource, Boolean> checker;

        public boolean hasChecker() {
            return checker != null;
        }
    }

}