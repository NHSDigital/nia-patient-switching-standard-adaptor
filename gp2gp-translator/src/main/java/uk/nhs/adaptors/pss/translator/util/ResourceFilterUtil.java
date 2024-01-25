package uk.nhs.adaptors.pss.translator.util;

import static uk.nhs.adaptors.pss.translator.util.CDUtil.extractSnomedCode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UKCompoundStatement;
import org.hl7.v3.RCMRMT030101UKNarrativeStatement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceFilterUtil {

    private static final List<String> ALLERGY_CODES = List.of("SN53.00", "14L..00");
    private static final String CODE_SYSTEM_READ_CODE_V2 = "2.16.840.1.113883.2.1.6.2";
    private static final String PATHOLOGY_CODE = "16488004";
    private static final String SPECIMEN_CODE = "123038009";
    private static final String BATTERY_VALUE = "BATTERY";
    private static final String CLUSTER_VALUE = "CLUSTER";

    public static boolean isDocumentReference(RCMRMT030101UKNarrativeStatement narrativeStatement) {
        return narrativeStatement.getReference()
            .stream()
            .anyMatch(reference -> reference.getReferredToExternalDocument() != null);
    }

    public static boolean isBloodPressure(RCMRMT030101UKCompoundStatement compoundStatement) {
        return compoundStatement != null && BloodPressureValidatorUtil.isBloodPressureWithBatteryAndBloodPressureTriple(compoundStatement);
    }

    public static boolean isAllergyIntolerance(RCMRMT030101UKCompoundStatement compoundStatement) {
        return compoundStatement != null
            && hasCode(compoundStatement)
            && ALLERGY_CODES.contains(compoundStatement.getCode().getCode())
            && CODE_SYSTEM_READ_CODE_V2.equals(compoundStatement.getCode().getCodeSystem())
            && compoundStatement.getComponent().size() == 1
            && compoundStatement.getComponent().get(0).hasObservationStatement();
    }

    public static boolean isDiagnosticReport(RCMRMT030101UKCompoundStatement compoundStatement) {
        if (compoundStatement != null && hasCode(compoundStatement)
            && compoundStatement.getClassCode().stream().anyMatch(CLUSTER_VALUE::equals)) {

            Optional<String> code = extractSnomedCode(compoundStatement.getCode());
            return code.map(PATHOLOGY_CODE::equals).orElse(false);
        }
        return false;
    }

    public static boolean hasDiagnosticReportParent(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return ehrExtract.getComponent().get(0).getEhrFolder().getComponent().stream()
            .flatMap(component3 -> component3.getEhrComposition().getComponent().stream())
            .flatMap(CompoundStatementResourceExtractors::extractAllCompoundStatements)
            .filter(ResourceFilterUtil::isDiagnosticReport)
            .flatMap(compoundStatement1 -> compoundStatement1.getComponent().stream())
            .flatMap(CompoundStatementResourceExtractors::extractAllChildCompoundStatements)
            .filter(Objects::nonNull)
            .anyMatch(compoundStatement::equals);
    }

    public static boolean isSpecimen(RCMRMT030101UKCompoundStatement compoundStatement) {
        if (compoundStatement != null && hasCode(compoundStatement)) {
            Optional<String> code = extractSnomedCode(compoundStatement.getCode());
            return code.map(SPECIMEN_CODE::equals).orElse(false);
        }

        return false;
    }

    public static boolean isTemplate(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null
            && !isBloodPressure(compoundStatement)
            && !isDiagnosticReport(compoundStatement)
            && !isSpecimen(compoundStatement)
            && List.of(BATTERY_VALUE, CLUSTER_VALUE).contains(compoundStatement.getClassCode().get(0));
    }

    private static boolean hasCode(RCMRMT030101UKCompoundStatement compoundStatement) {
        return compoundStatement.hasCode() && compoundStatement.getCode().hasCode();
    }
}
