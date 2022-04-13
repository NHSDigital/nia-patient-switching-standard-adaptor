package uk.nhs.adaptors.pss.translator.mapper.medication;

import static org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestIntent.PLAN;

import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDispenseRequestPeriodEnd;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDosage;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildDosageQuantity;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildNotes;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.buildPrescriptionTypeExtension;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.createMedicationRequestSkeleton;
import static uk.nhs.adaptors.pss.translator.mapper.medication.MedicationMapperUtils.extractEhrSupplyAuthoriseId;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UnsignedIntType;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04Discontinue;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04MedicationRef;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation2;
import org.hl7.v3.RCMRMT030101UK04ReversalOf;
import org.hl7.v3.RCMRMT030101UK04SupplyAnnotation;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@Service
@AllArgsConstructor
public class MedicationRequestPlanMapper {
    private static final String REPEAT_INFORMATION_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationRepeatInformation-1";
    private static final String STATUS_CHANGE_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatusReason-1";

    private static final String REPEATS_ISSUED_URL = "numberOfRepeatPrescriptionsIssued";
    private static final String REPEATS_ALLOWED_URL = "numberOfRepeatPrescriptionsAllowed";
    private static final String REPEATS_EXPIRY_DATE_URL = "authorisationExpiryDate";
    private static final String STATUS_CHANGE_DATE_URL = "statusChangeDate";
    private static final String STATUS_REASON = "statusReason";

    private static final String COMPLETE = "COMPLETE";
    private static final String NHS_PRESCRIPTION = "NHS prescription";
    private static final String PRESCRIPTION_TYPE = "Prescription type: ";

    private final MedicationMapper medicationMapper;

    public MedicationRequest mapToPlanMedicationRequest(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04MedicationStatement medicationStatement, RCMRMT030101UK04Authorise supplyAuthorise, String practiseCode) {

        var ehrSupplyAuthoriseIdExtract = extractEhrSupplyAuthoriseId(supplyAuthorise);

        if (ehrSupplyAuthoriseIdExtract.isPresent()) {
            var ehrSupplyAuthoriseId = ehrSupplyAuthoriseIdExtract.get();
            var discontinue = extractMatchingDiscontinue(ehrSupplyAuthoriseId, ehrExtract);
            MedicationRequest medicationRequest = createMedicationRequestSkeleton(ehrSupplyAuthoriseId);

            medicationRequest.addIdentifier(buildIdentifier(ehrSupplyAuthoriseId, practiseCode));
            medicationRequest.setStatus(buildMedicationRequestStatus(supplyAuthorise));
            medicationRequest.setIntent(PLAN);
            medicationRequest.addDosageInstruction(buildDosage(medicationStatement.getPertinentInformation()));
            medicationRequest.setDispenseRequest(buildDispenseRequestForAuthorise(supplyAuthorise, medicationStatement));

            List<Extension> repeatInformationExtensions = new ArrayList<>();
            extractSupplyAuthoriseRepeatInformation(supplyAuthorise).ifPresent(repeatInformationExtensions::add);
            extractRepeatInformationIssued(ehrExtract, supplyAuthorise, ehrSupplyAuthoriseId).ifPresent(repeatInformationExtensions::add);
            extractAuthorisationExpiryDate(supplyAuthorise).ifPresent(repeatInformationExtensions::add);

            buildCondensedExtensions(REPEAT_INFORMATION_URL, repeatInformationExtensions)
                .ifPresent(medicationRequest::addExtension);

            List<Extension> statusChangeExtensions = new ArrayList<>();
            discontinue
                .map(this::buildStatusChangeDateExtension)
                .ifPresent(statusChangeExtensions::add);

            discontinue
                .map(this::extractTermText)
                .map(this::buildStatusReasonCodeableConceptExtension)
                .ifPresent(statusChangeExtensions::add);

            buildCondensedExtensions(STATUS_CHANGE_URL, statusChangeExtensions)
                .ifPresent(medicationRequest::addExtension);
            buildPrescriptionTypeExtension(supplyAuthorise).ifPresent(medicationRequest::addExtension);

            buildNotesForAuthorise(supplyAuthorise).forEach(medicationRequest::addNote);
            extractPriorPrescription(supplyAuthorise).ifPresent(medicationRequest::setPriorPrescription);
            medicationMapper.extractMedicationReference(medicationStatement).ifPresent(medicationRequest::setMedication);

            return medicationRequest;
        }
        return null;
    }

    private Optional<RCMRMT030101UK04Discontinue> extractMatchingDiscontinue(String supplyAuthoriseId,
        RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(RCMRMT030101UK04Component3::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .map(RCMRMT030101UK04EhrComposition::getComponent)
            .flatMap(List::stream)
            .filter(RCMRMT030101UK04Component4::hasMedicationStatement)
            .map(RCMRMT030101UK04Component4::getMedicationStatement)
            .map(RCMRMT030101UK04MedicationStatement::getComponent)
            .flatMap(List::stream)
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyDiscontinue)
            .map(RCMRMT030101UK04Component2::getEhrSupplyDiscontinue)
            .filter(discontinue1 -> hasReversalIdMatchingAuthorise(discontinue1.getReversalOf(), supplyAuthoriseId))
            .findFirst();
    }

    private Optional<Extension> extractSupplyAuthoriseRepeatInformation(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasRepeatNumber() && supplyAuthorise.getRepeatNumber().getValue().intValue() != 0) {
            return Optional.of(
                new Extension(REPEATS_ALLOWED_URL, new UnsignedIntType(supplyAuthorise.getRepeatNumber().getValue().intValue())));
        }
        return Optional.empty();
    }

    private Optional<Extension> extractRepeatInformationIssued(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04Authorise supplyAuthorise, String supplyAuthoriseId) {

        if ((supplyAuthorise.hasRepeatNumber() && supplyAuthorise.getRepeatNumber().getValue().intValue() != 0)
            || !supplyAuthorise.hasRepeatNumber()) {

            var repeatCount = ehrExtract.getComponent().stream()
                .map(RCMRMT030101UK04Component::getEhrFolder)
                .map(RCMRMT030101UK04EhrFolder::getComponent)
                .flatMap(List::stream)
                .map(RCMRMT030101UK04Component3::getEhrComposition)
                .map(RCMRMT030101UK04EhrComposition::getComponent)
                .flatMap(List::stream)
                .flatMap(MedicationMapperUtils::extractAllMedications)
                .filter(Objects::nonNull)
                .map(RCMRMT030101UK04MedicationStatement::getComponent)
                .flatMap(List::stream)
                .filter(prescribe -> hasInFulfillmentOfReference(prescribe, supplyAuthoriseId))
                .count();

            return Optional.of(
                new Extension(REPEATS_ISSUED_URL, new UnsignedIntType(repeatCount)));
        }
        return Optional.empty();
    }

    private Optional<Extension> extractAuthorisationExpiryDate(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasEffectiveTime()) {
            if (supplyAuthorise.getEffectiveTime().hasHigh()) {
                return Optional.of(new Extension(
                    REPEATS_EXPIRY_DATE_URL, DateFormatUtil.parseToDateTimeType(supplyAuthorise.getEffectiveTime().getHigh().getValue())));
            }
        }
        return Optional.empty();
    }

    private String extractTermText(RCMRMT030101UK04Discontinue discontinue) {
        StringBuilder statusReasonStringBuilder = new StringBuilder();
        if (discontinue.hasCode() && discontinue.getCode().hasOriginalText()) {
            statusReasonStringBuilder.append(discontinue.getCode().getOriginalText())
                .append(StringUtils.SPACE);
        }

        if (discontinue.hasCode() && !discontinue.getCode().hasOriginalText() && discontinue.getCode().hasDisplayName()) {
            statusReasonStringBuilder.append(discontinue.getCode().hasDisplayName())
                .append(StringUtils.SPACE);
        }

        discontinue.getPertinentInformation()
            .stream()
            .map(RCMRMT030101UK04PertinentInformation2::getPertinentSupplyAnnotation)
            .map(RCMRMT030101UK04SupplyAnnotation::getText)
            .filter(StringUtils::isNotBlank)
            .forEach(text -> {
                statusReasonStringBuilder.append(text)
                    .append(StringUtils.SPACE);
            });

        if (discontinue.hasAvailabilityTime() && discontinue.getAvailabilityTime().hasNullFlavor()
            && discontinue.getAvailabilityTime().getNullFlavor().value().equals("UNK")) {
            statusReasonStringBuilder.append("Unknown date");
        }

        return statusReasonStringBuilder.toString();
    }

    private Optional<Reference> extractPriorPrescription(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasPredecessor() && supplyAuthorise.getPredecessorFirstRep().hasPriorMedicationRef()
            && supplyAuthorise.getPredecessorFirstRep().getPriorMedicationRef().hasId()
            && supplyAuthorise.getPredecessorFirstRep().getPriorMedicationRef().getId().hasRoot()) {
            return Optional.of(new Reference(
                new IdType(ResourceType.MedicationRequest.name(),
                    supplyAuthorise.getPredecessorFirstRep().getPriorMedicationRef().getId().getRoot()
                )));
        }
        return Optional.empty();
    }

    private MedicationRequest.MedicationRequestStatus buildMedicationRequestStatus(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasStatusCode() && supplyAuthorise.getStatusCode().hasCode()
            && COMPLETE.equals(supplyAuthorise.getStatusCode().getCode())) {
            return MedicationRequest.MedicationRequestStatus.COMPLETED;
        } else {
            return MedicationRequest.MedicationRequestStatus.ACTIVE;
        }
    }

    private MedicationRequest.MedicationRequestDispenseRequestComponent buildDispenseRequestForAuthorise(
        RCMRMT030101UK04Authorise supplyAuthorise, RCMRMT030101UK04MedicationStatement medicationStatement) {
        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest
            = new MedicationRequest.MedicationRequestDispenseRequestComponent();

        if (supplyAuthorise.hasQuantity()) {
            buildDosageQuantity(supplyAuthorise.getQuantity()).ifPresent(dispenseRequest::setQuantity);
        }

        var period = buildDispenseRequestPeriodEnd(supplyAuthorise, medicationStatement);
        MedicationMapperUtils.extractDispenseRequestPeriodStart(supplyAuthorise).ifPresent(period::setStartElement);

        return dispenseRequest.setValidityPeriod(period);
    }

    private Optional<Extension> buildCondensedExtensions(String url, List<Extension> extensionList) {
        if (!extensionList.isEmpty()) {
            return Optional.of((Extension) new Extension(url).setExtension(extensionList));
        }
        return Optional.empty();
    }

    private Extension buildStatusChangeDateExtension(RCMRMT030101UK04Discontinue discontinue) {
        if (discontinue.hasAvailabilityTime() && discontinue.getAvailabilityTime().hasValue()) {
            return new Extension(STATUS_CHANGE_DATE_URL,
                DateFormatUtil.parseToDateTimeType(discontinue.getAvailabilityTime().getValue()));
        }
        return null;
    }

    private Extension buildStatusReasonCodeableConceptExtension(String statusReason) {
        return new Extension(STATUS_REASON, new CodeableConcept().setText(statusReason));
    }

    private List<Annotation> buildNotesForAuthorise(RCMRMT030101UK04Authorise supplyAuthorise) {
        var notes = buildNotes(supplyAuthorise.getPertinentInformation());
        if (supplyAuthorise.hasCode() && supplyAuthorise.getCode().hasDisplayName()
            && !NHS_PRESCRIPTION.equalsIgnoreCase(supplyAuthorise.getCode().getDisplayName())) {
            notes.add(new Annotation(
                new StringType(PRESCRIPTION_TYPE + supplyAuthorise.getCode().getDisplayName())
            ));
        }
        return notes;
    }

    private boolean hasReversalIdMatchingAuthorise(List<RCMRMT030101UK04ReversalOf> reversalOf, String supplyAuthoriseId) {
        return reversalOf.stream()
            .map(RCMRMT030101UK04ReversalOf::getPriorMedicationRef)
            .map(RCMRMT030101UK04MedicationRef::getId)
            .map(II::getRoot)
            .anyMatch(supplyAuthoriseId::equals);
    }

    private boolean hasInFulfillmentOfReference(RCMRMT030101UK04Component2 component, String id) {
        return component.hasEhrSupplyPrescribe()
            && component.getEhrSupplyPrescribe().hasInFulfillmentOf()
            && component.getEhrSupplyPrescribe().getInFulfillmentOf().hasPriorMedicationRef()
            && component.getEhrSupplyPrescribe().getInFulfillmentOf().getPriorMedicationRef().hasId()
            && component.getEhrSupplyPrescribe().getInFulfillmentOf().getPriorMedicationRef().getId().hasRoot()
            && component.getEhrSupplyPrescribe().getInFulfillmentOf().getPriorMedicationRef().getId().getRoot().equals(id);
    }
}
