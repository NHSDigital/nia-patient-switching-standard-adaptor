package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;
import java.util.Objects;

import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiagnosticReportMapper {

    private static final String CLUSTER_CLASSCODE = "CLUSTER";
    private static final String DR_SNOMED_CODE = "16488004";

    public List<DiagnosticReport> mapDiagnosticReports(RCMRMT030101UK04EhrExtract ehrExtract) {
        var s = getCompositionsContainingClusterCompoundStatement(ehrExtract);
        return List.of();
    }

    private DiagnosticReport createDiagnosticReport() {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        return diagnosticReport;
    }

    private List<RCMRMT030101UK04EhrComposition> getCompositionsContainingClusterCompoundStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().stream()
            .flatMap(component -> component.getEhrFolder().getComponent().stream())
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .filter(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .map(RCMRMT030101UK04Component4::getCompoundStatement)
                .filter(Objects::nonNull)
                .anyMatch(this::isDiagnosticReportCandidate))
            .toList();
    }

    private boolean isDiagnosticReportCandidate(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getClassCode()
            .stream()
            .findFirst()
            .filter(
                classCode -> CLUSTER_CLASSCODE.equals(classCode)
                    && DR_SNOMED_CODE.equals(compoundStatement.getCode().getCode())
            ).isPresent();
    }

}
