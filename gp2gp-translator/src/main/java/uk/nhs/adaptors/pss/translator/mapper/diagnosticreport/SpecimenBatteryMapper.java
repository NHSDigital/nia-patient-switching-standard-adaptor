package uk.nhs.adaptors.pss.translator.mapper.diagnosticreport;

import static org.hl7.fhir.dstu3.model.ResourceType.Specimen;

import static uk.nhs.adaptors.pss.translator.util.CompoundStatementUtil.extractResourcesFromCompound;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToDateTimeType;
import static uk.nhs.adaptors.pss.translator.util.DateFormatUtil.parseToInstantType;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Observation.ObservationRelatedComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.v3.CD;
import org.hl7.v3.RCMRMT030101UK04Author;
import org.hl7.v3.RCMRMT030101UK04Component02;
import org.hl7.v3.RCMRMT030101UK04CompoundStatement;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04NarrativeStatement;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.TS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.nhs.adaptors.pss.translator.mapper.CodeableConceptMapper;
import uk.nhs.adaptors.pss.translator.util.TextUtil;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecimenBatteryMapper {

    private static final String META_PROFILE_URL_SUFFIX = "Observation-1";
    private static final String USER_COMMENT_HEADER = "USER COMMENT";

    private final CodeableConceptMapper codeableConceptMapper;

    public Observation mapBatteryObservation(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04CompoundStatement batteryCompoundStatement, RCMRMT030101UK04CompoundStatement specimenCompoundStatement,
        RCMRMT030101UK04EhrComposition ehrComposition, Patient patient, List<Encounter> encounters, String practiseCode) {
        final Observation observation = new Observation();
        observation.setId(batteryCompoundStatement.getId().get(0).getRoot());
        observation.setMeta(generateMeta(META_PROFILE_URL_SUFFIX));
        observation.addIdentifier(buildIdentifier(batteryCompoundStatement.getId().get(0).getRoot(), practiseCode));
        observation.setSubject(new Reference(patient));
        observation.setSpecimen(new Reference(new IdType(Specimen.name(), specimenCompoundStatement.getId().get(0).getRoot())));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        observation.addCategory(createCategory());
        observation.setCode(new CodeableConcept()); //line 162
        observation.setComment(getAllNarrativeStatementComments(batteryCompoundStatement));
        observation.setRelated(getRelated(batteryCompoundStatement));
        getContext(encounters, ehrComposition).ifPresent(observation::setContext);
        getEffective(ehrExtract, batteryCompoundStatement).ifPresent(observation::setEffective); //line 165
        getIssued(ehrExtract, ehrComposition).ifPresent(observation::setIssuedElement);

        return observation;
    }

    private Optional<Reference> getContext(List<Encounter> encounters, RCMRMT030101UK04EhrComposition ehrComposition) {
        return encounters.stream()
            .filter(encounter -> ehrComposition.getId().getRoot().equals(encounter.getId()))
            .findFirst()
            .map(encounter -> new IdType(ResourceType.Encounter.name(), encounter.getId()))
            .map(Reference::new);
    }

    private Optional<DateTimeType> getEffective(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04CompoundStatement compoundStatement) {
        if (compoundStatementHasValidAvailabilityTime(compoundStatement)) {
            return Optional.of(parseToDateTimeType(compoundStatement.getAvailabilityTime().getValue()));
        }

        if (availabilityTimeHasValue(ehrExtract.getAvailabilityTime())) {
            return Optional.of(parseToDateTimeType(ehrExtract.getAvailabilityTime().getValue()));
        }

        return Optional.empty();
    }

    private CodeableConcept createCategory() {
        final CD cd = new CD();
        cd.setCodeSystem("http://hl7.org/fhir/observation-category");
        cd.setCode("laboratory");
        cd.setDisplayName("Laboratory");
        return codeableConceptMapper.mapToCodeableConcept(cd);
    }

    private Optional<InstantType> getIssued(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04EhrComposition ehrComposition) {
        /**
         * Taken from TransformHL7TSToFHIRinstant(containing ehrComposition/author/time/@value) or if
         * value is nullFlavor taken from EhrEXtract/availabilityTime/@value converted to FHIR instant
         * ** need to get something that is guaranteed not to be nullFlavor to fall back to.
         * The  TransformHL7TSToFHIRinstant needs to be able to format and pad any valid non nullFlavor
         * HL7 TS to the FHIR instant type
         */
        if (authorHasValidTimeValue(ehrComposition.getAuthor())) {
            return Optional.of(parseToInstantType(ehrComposition.getAuthor().getTime().getValue()));
        }

        if (availabilityTimeHasValue(ehrExtract.getAvailabilityTime())) {
            return Optional.of(parseToInstantType(ehrExtract.getAvailabilityTime().getValue()));
        }

        return Optional.empty();
    }

    private boolean authorHasValidTimeValue(RCMRMT030101UK04Author author) {
        return author != null && author.hasTime()
            && author.getTime().hasValue()
            && !author.getTime().hasNullFlavor();
    }

    private boolean compoundStatementHasValidAvailabilityTime(RCMRMT030101UK04CompoundStatement compoundStatement) {
        return compoundStatement != null && compoundStatement.getAvailabilityTime() != null
            && compoundStatement.getAvailabilityTime().hasValue()
            && !compoundStatement.getAvailabilityTime().hasNullFlavor();
    }

    private boolean availabilityTimeHasValue(TS availabilityTime) {
        return availabilityTime != null && availabilityTime.hasValue() && !availabilityTime.hasNullFlavor();
    }

    private List<ObservationRelatedComponent> getRelated(RCMRMT030101UK04CompoundStatement batteryCompoundStatement) {
        return Stream.concat(
            getDirectUserCommentNarrativeStatements(batteryCompoundStatement),
            getObservationReferences(batteryCompoundStatement)
        ).toList();
    }

    private String getAllNarrativeStatementComments(RCMRMT030101UK04CompoundStatement batteryCompoundStatement) {
        return extractResourcesFromCompound(batteryCompoundStatement, RCMRMT030101UK04Component02::hasNarrativeStatement,
            RCMRMT030101UK04Component02::getNarrativeStatement)
            .stream()
            .map(RCMRMT030101UK04NarrativeStatement.class::cast)
            .map(RCMRMT030101UK04NarrativeStatement::getText)
            .filter(StringUtils::isNotEmpty)
            .filter(text -> !text.contains(USER_COMMENT_HEADER))
            .map(TextUtil::getLastLine)
            .collect(Collectors.joining(StringUtils.LF));
    }

    private Stream<ObservationRelatedComponent> getDirectUserCommentNarrativeStatements(RCMRMT030101UK04CompoundStatement batteryCompoundStatement) {
        return batteryCompoundStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component02::hasNarrativeStatement)
            .map(RCMRMT030101UK04Component02::getNarrativeStatement)
            .filter(narrativeStatement -> narrativeStatement.getText().contains(USER_COMMENT_HEADER))
            .map(narrativeStatement -> new Reference(new IdType(ResourceType.Observation.name(), narrativeStatement.getId().getRoot())))
            .map(reference -> new ObservationRelatedComponent().setTarget(reference));
    }
    private Stream<ObservationRelatedComponent> getObservationReferences(RCMRMT030101UK04CompoundStatement batteryCompoundStatement) {
        return extractResourcesFromCompound(batteryCompoundStatement, RCMRMT030101UK04Component02::hasObservationStatement,
            RCMRMT030101UK04Component02::getObservationStatement)
            .stream()
            .map(RCMRMT030101UK04ObservationStatement.class::cast)
            .map(observationStatement -> new Reference(new IdType(ResourceType.Observation.name(), observationStatement.getId().getRoot())))
            .map(reference -> new ObservationRelatedComponent().setTarget(reference));
    }
}
