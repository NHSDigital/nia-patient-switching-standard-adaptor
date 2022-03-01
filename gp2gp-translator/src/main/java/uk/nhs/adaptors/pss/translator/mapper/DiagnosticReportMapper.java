package uk.nhs.adaptors.pss.translator.mapper;

import static org.hl7.fhir.dstu3.model.Observation.ObservationStatus.FINAL;

import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.EncounterReferenceUtil.getEncounterReference;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Author;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.TS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DiagnosticReportMapper {

    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String EXTENSION_IDENTIFIER_ROOT = "2.16.840.1.113883.2.1.4.5.5";
    private static final String META_PROFILE_URL = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DiagnosticReport-1";
    private static final String CLUSTER_CLASSCODE = "CLUSTER";
    private static final String DR_SNOMED_CODE = "16488004";

    private final CodeableConceptMapper codeableConceptMapper;
    private final ObservationCommentMapper observationCommentMapper;

    public List<DiagnosticReport> mapDiagnosticReports(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient, List<Encounter> encounters) {
        var compositions = getCompositionsContainingClusterCompoundStatement(ehrExtract);
        return compositions.stream()
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream())
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .filter(Objects::nonNull)
            .map(compoundStatement -> createDiagnosticReport(compoundStatement, patient, compositions, encounters))
            .toList();
    }

    private DiagnosticReport createDiagnosticReport(RCMRMT030101UK04CompoundStatement compoundStatement, Patient patient,
        List<RCMRMT030101UK04EhrComposition> compositions, List<Encounter> encounters) {
        final DiagnosticReport diagnosticReport = new DiagnosticReport();
        final String id = compoundStatement.getId().get(0).getRoot();

        diagnosticReport.setMeta(createMeta());

        diagnosticReport.setId(id);
        /**
         * From CompoundStatement/id[0]/@root.
         * Note that pathology reports may have 2 id instances id[1] is the pathology report id
         */

        diagnosticReport.addIdentifier(createIdentifier(id));
        /**
         * From CompoundStatement/id[0[/@root.
         * Note that pathology reports may have 2 id instances id[1]  is the pathology report id
         */

        createIdentifierExtension(compoundStatement.getId()).ifPresent(diagnosticReport::addIdentifier);
        /**
         * If there is an instance of f CompoundStatement/id[1] with @root=="2.16.840.1.113883.2.1.4.5.5",
         * output the @extension as an identifier under the code system OID 2.16.840.1.113883.2.1.4.5.5
         */

        diagnosticReport.setCode(createCodeableConcept(compoundStatement));
        /**
         * Fixed value
         */

        diagnosticReport.setSubject(createPatientReference(patient));
        /**
         * Reference to global patient resource generated for transaction.
         */

        buildContext(compositions, encounters, compoundStatement).ifPresent(diagnosticReport::setContext);
        /**
         * If an Encounter resource is generated from the containing ehrComposition then references the corresponding Encounter.
         * See ehrComposition to Encounter mapping.
         * Encounters are suppressed for certain ehrComposition codes so will not always be populated.
         */

        //getIssued(ehrExtract, compositions, compoundStatement).ifPresent(diagnosticReport::setIssuedElement);
        /**
         * ISSUED
         * 1. From CompoundStatement/availabilityTime/@value
         * 2. From the containing ehtComposition/author/time/@value
         * 3. From EhrExtract/availabilityTime/@value
         */

        //getSpecimen() <- list
        /**
         * For each child CompoundStatement component coded as 123038009 perform the specimen mapping and insert a reference to the
         * generated specimen. AS we intend to re-use the specimen CompoundStatement/id[0] as the Specimen.id
         * then each reference will be just a reference to the specimen CompoundStatement/id[0].
         * There can of course be many specimens per report so this needs to iterate over every instance
         */

        diagnosticReport.setResult(getResultReferences(compoundStatement)); //leave as null when list is empty or add an empty list?
        /**
         * A result reference should be generated for every result Observation generated from the banner CompoundStatement,
         * result ObservationStatement, or result CompoundStatement CLUSTER found within each specimen CompoundStatement
         */

        /**
         * 2. Process all NarrativeStatement direct children of the laboratory level CompoundStatement into Observation (Comment) and
         * reference these from DiagnosticReport.results (See Report Level Comment to Observation (Comment) Map below)
         */

        return diagnosticReport;
    }

    public List<Observation> mapChildrenObservationComments(RCMRMT030101UK04EhrExtract ehrExtract, Patient patient,
        List<Encounter> encounters) {
        var narrativeStatements = getCompositionsContainingClusterCompoundStatement(ehrExtract)
            .stream()
            .flatMap(ehrComposition -> ehrComposition.getComponent().stream())
            .map(RCMRMT030101UK04Component4::getCompoundStatement)
            .flatMap(compoundStatement -> compoundStatement.getComponent().stream())
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .filter(Objects::nonNull)
            .toList();

        return observationCommentMapper.mapDiagnosticChildrenObservations(narrativeStatements, ehrExtract, patient, encounters);
    }

    private List<Reference> getResultReferences(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .filter(Objects::nonNull)
            .map(narrativeStatement -> new Reference(new IdType(ResourceType.Observation.name(), narrativeStatement.getId().getRoot())))
            .toList();
    }

    private Reference createPatientReference(Patient patient) {
        return new Reference(patient);
    }

    private Meta createMeta() {
        return new Meta()
            .addProfile(META_PROFILE_URL);
    }

    private Identifier createIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
    }

    private Optional<Identifier> createIdentifierExtension(List<II> id) {
        if (id.size() > 1) {
            final II idExtension = id.get(1);
            if (idExtension != null && EXTENSION_IDENTIFIER_ROOT.equals(idExtension.getRoot())) {
                return Optional.of(new Identifier()
                    .setSystem(EXTENSION_IDENTIFIER_ROOT)
                    .setValue(idExtension.getExtension()));
            }
        }
        return Optional.empty();
    }

    private Optional<Reference> buildContext(List<RCMRMT030101UK04EhrComposition> compositions, List<Encounter> encounters,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return Optional.ofNullable(getEncounterReference(
            compositions,
            encounters,
            getCurrentEhrComposition(compositions, compoundStatement).getId().getRoot())
        );
    }

    private Optional<InstantType> getIssued(RCMRMT030101UK04EhrExtract ehrExtract, List<RCMRMT030101UK04EhrComposition> compositions,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        var ehrComposition = getCurrentEhrComposition(compositions, compoundStatement);

        if (authorHasValidTimeValue(ehrComposition.getAuthor())) {
            return Optional.of(parseToInstantType(ehrComposition.getAuthor().getTime().getValue()));
        }

        if (availabilityTimeHasValue(ehrExtract.getAvailabilityTime())) {
            return Optional.of(parseToInstantType(ehrExtract.getAvailabilityTime().getValue()));
        }

        return Optional.empty();
    }

    private boolean authorHasValidTimeValue(RCMRMT030101UK04Author author) {
        return author != null && author.getTime() != null
            && author.getTime().getValue() != null
            && author.getTime().getNullFlavor() == null;
    }

    private boolean availabilityTimeHasValue(TS availabilityTime) {
        return availabilityTime != null && availabilityTime.getValue() != null && !timeHasNullFlavor(availabilityTime);
    }

    private boolean timeHasNullFlavor(TS time) {
        return time.getNullFlavor() != null;
    }

    private CodeableConcept createCodeableConcept(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return codeableConceptMapper.mapToCodeableConcept(compoundStatement.getCode());
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

    private RCMRMT030101UK04EhrComposition getCurrentEhrComposition(List<RCMRMT030101UK04EhrComposition> ehrCompositions,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        return ehrCompositions
            .stream()
            .filter(ehrComposition -> ehrComposition.getComponent()
                .stream()
                .anyMatch(component4 -> compoundStatement.equals(component4.getCompoundStatement()))
            ).findFirst().get();
    }
}
