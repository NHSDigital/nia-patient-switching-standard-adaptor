package uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers;

import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapper.extractMedicationReference;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.buildDosage;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.buildDosageQuantity;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.buildNotes;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.buildPrescriptionTypeExtension;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.buildValidityPeriod;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.createMedicationRequestSkeleton;
import static uk.nhs.adaptors.pss.translator.mapper.MedicationRequestMappers.MedicationMapperUtils.extractEhrSupplyAuthoriseId;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UnsignedIntType;
import org.hl7.fhir.instance.model.api.IIdType;
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

import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

public class MedicationRequestPlanMapper {
    private static final String REPEAT_INFORMATION_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationRepeatInformation-1";
    private static final String STATUS_CHANGE_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatusReason-1";

    private static final String REPEATS_ISSUED_URL = "numberOfRepeatPrescriptionsIssued";
    private static final String REPEATS_ALLOWED_URL = "numberOfRepeatPrescriptionsAllowed";
    private static final String STATUS_CHANGE_DATE_URL = "statusChangeDate";
    private static final String STATUS_REASON = "statusReason";

    private static final String COMPLETE = "COMPLETE";
    private static final String NHS_PRESCRIPTION = "NHS prescription";
    private static final String PRESCRIPTION_TYPE = "Prescription type: ";

    protected MedicationRequest mapToPlanMedicationRequest(RCMRMT030101UK04EhrExtract ehrExtract,
        RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Authorise supplyAuthorise, Patient subject, Encounter context) {

        var ehrSupplyAuthoriseIdExtract = extractEhrSupplyAuthoriseId(supplyAuthorise);

        if (ehrSupplyAuthoriseIdExtract.isPresent()) {
            var ehrSupplyAuthoriseId = ehrSupplyAuthoriseIdExtract.get();
            var discontinue = extractMatchingDiscontinue(ehrSupplyAuthoriseId, new RCMRMT030101UK04EhrExtract());
            MedicationRequest medicationRequest = createMedicationRequestSkeleton(
                supplyAuthorise, subject, context, ehrSupplyAuthoriseId
            );

            medicationRequest.addIdentifier(buildIdentifier(ehrSupplyAuthoriseId, ""));
            medicationRequest.setStatus(buildMedicationRequestStatus(supplyAuthorise));
            medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.PLAN);

            medicationRequest.addDosageInstruction(buildDosage(medicationStatement));
            medicationRequest.setDispenseRequest(buildDispenseRequestForAuthorise(supplyAuthorise));

            List<Extension> repeatInformationExtensions = new ArrayList<>();
            extractSupplyAuthoriseRepeatInformation(supplyAuthorise).ifPresent(repeatInformationExtensions::add);
            extractRepeatInformationIssued(medicationStatement, supplyAuthorise).ifPresent(repeatInformationExtensions::add);

            if (!repeatInformationExtensions.isEmpty()) {
                medicationRequest.addExtension(buildRepeatExtension(repeatInformationExtensions));
            }

            List<Extension> statusChangeExtensions = new ArrayList<>();
            discontinue
                .map(this::buildStatusChangeDateExtension)
                .ifPresent(statusChangeExtensions::add);

            discontinue
                .map(this::extractTermText)
                .map(this::buildStatusReasonCodeableConceptExtension)
                .ifPresent(statusChangeExtensions::add);

            buildStatusChangeExtension(statusChangeExtensions).ifPresent(medicationRequest::addExtension);
            if (!statusChangeExtensions.isEmpty()) {
                medicationRequest.addExtension(buildPrescriptionTypeExtension(supplyAuthorise));
            }

            buildNotesForAuthorise(supplyAuthorise).forEach(medicationRequest::addNote);
            extractPriorPrescription(supplyAuthorise).ifPresent(medicationRequest::setPriorPrescription);
            extractMedicationReference(medicationStatement).ifPresent(medicationRequest::setMedication);

            return medicationRequest;
        }
        return null;
    }

    private Optional<RCMRMT030101UK04Discontinue> extractMatchingDiscontinue(String supplyAuthoriseId,
        RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .map(RCMRMT030101UK04EhrComposition::getComponent)
            .flatMap(List::stream)
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
            return Optional.of(buildSupplyAuthoriseRepeatAllowedExtension(supplyAuthorise.getRepeatNumber().getValue().intValue()));
        }
        return Optional.empty();
    }

    private Optional<Extension> extractRepeatInformationIssued(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Authorise supplyAuthorise) {

        if ((supplyAuthorise.hasRepeatNumber() && supplyAuthorise.getRepeatNumber().getValue().intValue() != 0)
            || !supplyAuthorise.hasRepeatNumber()) {
            var repeatCount = medicationStatement.getComponent()
                .stream()
                .filter(this::hasInFulfillmentOfReference)
                .count();

            return Optional.of(buildSupplyAuthoriseRepeatIssuedExtension((int) repeatCount));
        }

        return Optional.empty();
    }

    private String extractTermText(RCMRMT030101UK04Discontinue discontinue) {
        StringBuilder statusReasonStringBuilder = new StringBuilder();
        if (discontinue.hasCode() && discontinue.getCode().hasOriginalText()) {
            statusReasonStringBuilder.append(discontinue.getCode().getOriginalText());
            statusReasonStringBuilder.append(StringUtils.SPACE);
        }

        if (discontinue.hasCode() && !discontinue.getCode().hasOriginalText() && discontinue.getCode().hasDisplayName()) {
            statusReasonStringBuilder.append(discontinue.getCode().hasDisplayName());
            statusReasonStringBuilder.append(StringUtils.SPACE);
        }

        discontinue.getPertinentInformation()
            .stream()
            .map(RCMRMT030101UK04PertinentInformation2::getPertinentSupplyAnnotation)
            .map(RCMRMT030101UK04SupplyAnnotation::getText)
            .filter(StringUtils::isNotBlank)
            .forEach(text -> {
                statusReasonStringBuilder.append(text);
                statusReasonStringBuilder.append(StringUtils.SPACE);
            });

        return statusReasonStringBuilder.toString();
    }


    private Optional<Reference> extractPriorPrescription(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasId() && supplyAuthorise.getId().hasRoot()) {
            IIdType iIdType = new IdType(ResourceType.MedicationRequest.name(), supplyAuthorise.getId().getRoot());
            return Optional.of(new Reference(iIdType));
        }
        return Optional.empty();
    }

    //remove
    private Extension buildSupplyAuthoriseRepeatAllowedExtension(int repeatCount) {
        return new Extension(REPEATS_ALLOWED_URL, new UnsignedIntType(repeatCount));
    }
    //remove
    private Extension buildSupplyAuthoriseRepeatIssuedExtension(int allowedCount) {
        return new Extension(REPEATS_ISSUED_URL, new UnsignedIntType(allowedCount));
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
        RCMRMT030101UK04Authorise supplyAuthorise) {
        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest
            = new MedicationRequest.MedicationRequestDispenseRequestComponent();

        if (supplyAuthorise.hasQuantity()) {
            buildDosageQuantity(supplyAuthorise.getQuantity()).ifPresent(dispenseRequest::setQuantity);
        }
        dispenseRequest.setValidityPeriod(buildValidityPeriod(supplyAuthorise.getAvailabilityTime()));

        return dispenseRequest;
    }

    // condense
    private Extension buildRepeatExtension(List<Extension> extensionList) {
        Extension extension = new Extension(REPEAT_INFORMATION_URL);
        extension.setExtension(extensionList);
        return extension;
    }

    private Extension buildStatusChangeDateExtension(RCMRMT030101UK04Discontinue discontinue) {
        Extension extension = new Extension(STATUS_CHANGE_DATE_URL);
        if (hasAvailability(discontinue)) {
            extension.setValue(DateFormatUtil.parseToDateTimeType(discontinue.getAvailabilityTime().getValue()));
        } else {
            extension.setValue(new StringType("Unknown Date"));
        }
        return extension;
    }

    private Optional<Extension> buildStatusChangeExtension(List<Extension> innerExtensions) {
        if (innerExtensions.size() > 0) {
            Extension extension = new Extension(STATUS_CHANGE_URL);
            extension.setExtension(innerExtensions);
            return Optional.of(extension);
        }
        return Optional.empty();
    }

    private Extension buildStatusReasonCodeableConceptExtension(String statusReason) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setText(statusReason);

        return new Extension(STATUS_REASON, codeableConcept);
    }

    private List<Annotation> buildNotesForAuthorise(RCMRMT030101UK04Authorise supplyAuthorise) {
        var notes = buildNotes(supplyAuthorise.getPertinentInformation());
        if (supplyAuthorise.hasCode() && supplyAuthorise.getCode().hasDisplayName()
            && !NHS_PRESCRIPTION.equalsIgnoreCase(supplyAuthorise.getCode().getDisplayName())) {
            notes.add(new Annotation(
                new StringType(PRESCRIPTION_TYPE + supplyAuthorise.getCode().getDisplayName())
            ));
        }

        return buildNotes(supplyAuthorise.getPertinentInformation());
    }

    private boolean hasReversalIdMatchingAuthorise(List<RCMRMT030101UK04ReversalOf> reversalOf, String supplyAuthoriseId) {
        return reversalOf.stream()
            .map(RCMRMT030101UK04ReversalOf::getPriorMedicationRef)
            .map(RCMRMT030101UK04MedicationRef::getId)
            .map(II::getRoot)
            .anyMatch(supplyAuthoriseId::equals);
    }

    private boolean hasInFulfillmentOfReference(RCMRMT030101UK04Component2 component) {
        return component.hasEhrSupplyPrescribe()
            && component.getEhrSupplyPrescribe().hasInFulfillmentOf()
            && component.getEhrSupplyPrescribe().getInFulfillmentOf().hasPriorMedicationRef()
            && component.getEhrSupplyPrescribe().getInFulfillmentOf().getPriorMedicationRef().hasId();
    }

    private boolean hasAvailability(RCMRMT030101UK04Discontinue discontinue) {
        return discontinue.hasAvailabilityTime() && discontinue.getAvailabilityTime().hasValue();
    }

}
