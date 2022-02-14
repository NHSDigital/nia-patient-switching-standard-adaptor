package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Dosage;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationStatement;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UnsignedIntType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.v3.II;
import org.hl7.v3.PQ;
import org.hl7.v3.RCMRMT030101UK04Authorise;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component2;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04Discontinue;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrExtractSpecification;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04MedicationDosage;
import org.hl7.v3.RCMRMT030101UK04MedicationRef;
import org.hl7.v3.RCMRMT030101UK04MedicationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation2;
import org.hl7.v3.RCMRMT030101UK04Prescribe;
import org.hl7.v3.RCMRMT030101UK04ReversalOf;
import org.hl7.v3.RCMRMT030101UK04SupplyAnnotation;
import org.hl7.v3.TS;

import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.generateMeta;
import static uk.nhs.adaptors.pss.translator.util.ResourceUtil.buildIdentifier;

public class MedicationRequestMapper {

    private static final String META_PROFILE = "MedicationRequest";
    private static final String ACUTE = "Acute";
    private static final String REPEAT = "Repeat";
    private static final String NHS_PRESCRIPTION = "NHS prescription";
    private static final String PRESCRIPTION_TYPE = "Prescription type: ";
    private static final String NO_INFORMATION_AVAILABLE = "No Information available";
    private static final String PRESCRIPTION_TYPE_EXTENSION_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-PrescriptionType-1";
    private static final String MEDICATION_STATEMENT_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-MedicationStatement-1";
    private static final String PRESCRIPTION_TYPE_CODING_SYSTEM
        = "https://fhir.nhs.uk/STU3/CodeSystem/CareConnect-PrescriptionType-1";
    private static final String REPEAT_INFORMATION_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationRepeatInformation-1";
    private static final String STATUS_CHANGE_URL
        = "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-MedicationStatusReason-1";
    private static final String REPEATS_ALLOWED_URL = "numberOfRepeatPrescriptionsAllowed";
    private static final String REPEATS_ISSUED_URL = "numberOfRepeatPrescriptionsIssued";
    private static final String STATUS_CHANGE_DATE_URL = "statusChangeDate";
    private static final String STATUS_REASON = "statusReason";
    private static final String COMPLETE = "COMPLETE";

    public MedicationRequest mapToMedicationRequest(RCMRMT030101UK04EhrComposition ehrComposition,
        RCMRMT030101UK04MedicationStatement medicationStatement, Date ehrExtractAvailabilityTime, Patient subject, Encounter context) {

        var authoredOn = extractAuthoredOn(ehrComposition, ehrExtractAvailabilityTime);

        List<MedicationRequest> medicationRequestsOrder = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyPrescribe)
            .map(RCMRMT030101UK04Component2::getEhrSupplyPrescribe)
            .map(supplyPrescribe -> mapToOrderMedicationRequest(medicationStatement, supplyPrescribe, subject, context))
            .map(medicationRequest -> medicationRequest.setAuthoredOnElement(authoredOn))
            .collect(Collectors.toList());

        List<MedicationRequest> medicationRequestsPlan = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> mapToPlanMedicationRequest(medicationStatement, supplyAuthorise, subject, context))
            .map(medicationRequest -> medicationRequest.setAuthoredOnElement(authoredOn))
            .collect(Collectors.toList());

        List<MedicationStatement> medicationStatements = medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .map(supplyAuthorise -> mapToMedicationStatement(medicationStatement, supplyAuthorise, subject, context))
            .collect(Collectors.toList());

        return null;
    }

    private MedicationStatement mapToMedicationStatement(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Authorise supplyAuthorise, Patient subject, Encounter context) {

        MedicationStatement medicationStatement1 = new MedicationStatement();
        var ehrSupplyAuthoriseId = extractEhrSupplyAuthoriseId(supplyAuthorise);

        ehrSupplyAuthoriseId.ifPresent(medicationStatement1::setId);
        ehrSupplyAuthoriseId
            .map(id -> buildIdentifier(id, ""))
            .ifPresent(medicationStatement1::addIdentifier);
        medicationStatement1.setMeta(generateMeta(MEDICATION_STATEMENT_URL));

        medicationStatement1.setContext(new Reference(context));
        medicationStatement1.setSubject(new Reference(subject));

        medicationStatement1.setStatus(buildMedicationStatementStatus(supplyAuthorise));
        medicationStatement1.setTaken(MedicationStatement.MedicationStatementTaken.UNK);

        return medicationStatement1;
    }

    private MedicationRequest mapToOrderMedicationRequest(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Prescribe supplyPrescribe, Patient subject, Encounter context) {

        MedicationRequest medicationRequest = new MedicationRequest();
        var ehrSupplyPrescribeId = extractEhrSupplyPrescribeId(supplyPrescribe);

        ehrSupplyPrescribeId.ifPresent(medicationRequest::setId);
        ehrSupplyPrescribeId
            .map(id -> buildIdentifier(id, ""))
            .ifPresent(medicationRequest::addIdentifier);

        medicationRequest.setMeta(generateMeta(META_PROFILE));
        medicationRequest.setContext(new Reference(context));
        medicationRequest.setSubject(new Reference(subject));

        medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
        medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

        ehrSupplyPrescribeId
            .map(supplyAuthoriseId -> extractSupplyAuthorise(medicationStatement, supplyAuthoriseId))
            .map(this::buildPrescriptionTypeExtension)
            .ifPresent(medicationRequest::addExtension);

        ehrSupplyPrescribeId
            .map(this::buildMedicationRequestReference)
            .ifPresent(medicationRequest::addBasedOn);

        buildNotesForPrescribe(supplyPrescribe)
            .forEach(medicationRequest::addNote);

        medicationRequest.addDosageInstruction(buildDosage(medicationStatement));
        medicationRequest.setDispenseRequest(buildDispenseRequestForPrescribe(supplyPrescribe));

        return medicationRequest;
    }

    private MedicationRequest mapToPlanMedicationRequest(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Authorise supplyAuthorise, Patient subject, Encounter context) {

        MedicationRequest medicationRequest = new MedicationRequest();

        var ehrSupplyAuthoriseId = extractEhrSupplyAuthoriseId(supplyAuthorise);

        ehrSupplyAuthoriseId.ifPresent(medicationRequest::setId);
        ehrSupplyAuthoriseId
            .map(id -> buildIdentifier(id, ""))
            .ifPresent(medicationRequest::addIdentifier);

        medicationRequest.setMeta(generateMeta(META_PROFILE));

        extractSupplyAuthoriseRepeatInformation(supplyAuthorise)
            .ifPresent(medicationRequest::addExtension);

        extractRepeatInformationIssued(medicationStatement, supplyAuthorise)
            .ifPresent(medicationRequest::addExtension);

        //todo pass in ehrExtract
        var discontinue = extractMatchingDiscontinue(ehrSupplyAuthoriseId.get(), new RCMRMT030101UK04EhrExtract());
        List<Extension> statusChangeExtensions = new ArrayList<>();
        discontinue
            .map(this::buildStatusChangeDateExtension)
            .ifPresent(statusChangeExtensions::add);

        discontinue
            .map(this::extractTermText)
            .map(this::buildStatusReasonCodeableConceptExtension)
            .ifPresent(statusChangeExtensions::add);

        buildStatusChangeExtension(statusChangeExtensions)
            .ifPresent(medicationRequest::addExtension);

        medicationRequest.addExtension(buildPrescriptionTypeExtension(supplyAuthorise));
        medicationRequest.setStatus(buildMedicationRequestStatus(supplyAuthorise));
        medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.PLAN);

        medicationRequest.setContext(new Reference(context));
        medicationRequest.setSubject(new Reference(subject));
        buildNotesForAuthorise(supplyAuthorise)
            .forEach(medicationRequest::addNote);

        medicationRequest.addDosageInstruction(buildDosage(medicationStatement));
        medicationRequest.setDispenseRequest(buildDispenseRequestForAuthorise(supplyAuthorise));

//        extractPriorPrescription(supplyAuthorise);

        return medicationRequest;
    }

    private MedicationRequest.MedicationRequestStatus buildMedicationRequestStatus(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasStatusCode() && supplyAuthorise.getStatusCode().hasCode()
            && COMPLETE.equals(supplyAuthorise.getStatusCode().getCode())) {
            return MedicationRequest.MedicationRequestStatus.COMPLETED;
        } else {
            return MedicationRequest.MedicationRequestStatus.ACTIVE;
        }
    }

    private MedicationStatement.MedicationStatementStatus buildMedicationStatementStatus(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasStatusCode() && supplyAuthorise.getStatusCode().hasCode()
            && COMPLETE.equals(supplyAuthorise.getStatusCode().getCode())) {
            return MedicationStatement.MedicationStatementStatus.COMPLETED;
        } else {
            return MedicationStatement.MedicationStatementStatus.ACTIVE;
        }
    }

    private Optional<Extension> buildStatusChangeExtension(List<Extension> innerExtensions) {
        if (innerExtensions.size() > 0) {
            Extension extension = new Extension();
            extension.setUrl(STATUS_CHANGE_URL);
            extension.setExtension(innerExtensions);
            return Optional.of(extension);
        }
        return Optional.empty();
    }

    private Extension buildStatusReasonCodeableConceptExtension(String statusReason) {
        Extension extension = new Extension();
        extension.setUrl(STATUS_REASON);

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setText(statusReason);

        extension.setValue(codeableConcept);
        return extension;
    }

    private Extension buildStatusChangeDateExtension(RCMRMT030101UK04Discontinue discontinue) {
        Extension extension = new Extension();
        extension.setUrl(STATUS_CHANGE_DATE_URL);
        if (hasAvailability(discontinue)) {
            extension.setValue(DateFormatUtil.parse(discontinue.getAvailabilityTime().getValue()));
        } else {
            extension.setValue(new StringType("Unknown Date"));
        }
        return extension;
    }

    private Extension buildPrescriptionTypeExtension(RCMRMT030101UK04Authorise supplyAuthorise) {
        Extension extension = new Extension();
        extension.setUrl(PRESCRIPTION_TYPE_EXTENSION_URL);
        Coding coding = new Coding();
        coding.setSystem(PRESCRIPTION_TYPE_CODING_SYSTEM);

        if (supplyAuthorise.hasRepeatNumber() && supplyAuthorise.getRepeatNumber().getValue().intValue() == 0) {
            coding.setDisplay(ACUTE);
            coding.setCode(ACUTE.toLowerCase(Locale.ROOT));
        } else {
            coding.setDisplay(REPEAT);
            coding.setCode(REPEAT.toLowerCase(Locale.ROOT));
        }

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(coding);
        extension.setValue(codeableConcept);

        return extension;
    }

    private List<Annotation> buildNotesForPrescribe(RCMRMT030101UK04Prescribe supplyPrescribe) {
        var notes = buildNotes(supplyPrescribe.getPertinentInformation());

        if (supplyPrescribe.hasCode() && supplyPrescribe.getCode().hasDisplayName()
            && !NHS_PRESCRIPTION.equalsIgnoreCase(supplyPrescribe.getCode().getDisplayName())) {
            notes.add(new Annotation(
                new StringType(PRESCRIPTION_TYPE + supplyPrescribe.getCode().getDisplayName())
            ));
        }

        return notes;
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

    private List<Annotation> buildNotes(List<RCMRMT030101UK04PertinentInformation2> pertinentInformation2s) {
        return pertinentInformation2s
            .stream()
            .filter(RCMRMT030101UK04PertinentInformation2::hasPertinentSupplyAnnotation)
            .map(RCMRMT030101UK04PertinentInformation2::getPertinentSupplyAnnotation)
            .filter(RCMRMT030101UK04SupplyAnnotation::hasText)
            .map(RCMRMT030101UK04SupplyAnnotation::getText)
            .map(text -> text + System.lineSeparator())
            .map(StringType::new)
            .map(Annotation::new)
            .collect(Collectors.toList());
    }

    private Dosage buildDosage(RCMRMT030101UK04MedicationStatement medicationStatement) {
        Dosage dosage = new Dosage();
        var pertinentInformationDosage = medicationStatement.getPertinentInformation()
            .stream()
            .filter(RCMRMT030101UK04PertinentInformation::hasPertinentMedicationDosage)
            .map(RCMRMT030101UK04PertinentInformation::getPertinentMedicationDosage)
            .filter(RCMRMT030101UK04MedicationDosage::hasText)
            .map(RCMRMT030101UK04MedicationDosage::getText)
            .findFirst();

        pertinentInformationDosage.ifPresentOrElse(dosage::setText,
            () -> dosage.setText(NO_INFORMATION_AVAILABLE));

        return dosage;
    }

    private MedicationRequest.MedicationRequestDispenseRequestComponent buildDispenseRequestForPrescribe(RCMRMT030101UK04Prescribe supplyPrescribe) {
        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest
            = new MedicationRequest.MedicationRequestDispenseRequestComponent();

        if (supplyPrescribe.hasQuantity()) {
            buildDosageQuantity(supplyPrescribe.getQuantity()).ifPresent(dispenseRequest::setQuantity);
        }
        dispenseRequest.setValidityPeriod(buildValidityPeriod(supplyPrescribe.getAvailabilityTime()));

        return dispenseRequest;
    }

    private MedicationRequest.MedicationRequestDispenseRequestComponent buildDispenseRequestForAuthorise(RCMRMT030101UK04Authorise supplyAuthorise) {
        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest
            = new MedicationRequest.MedicationRequestDispenseRequestComponent();

        if (supplyAuthorise.hasQuantity()) {
            buildDosageQuantity(supplyAuthorise.getQuantity()).ifPresent(dispenseRequest::setQuantity);
        }
        dispenseRequest.setValidityPeriod(buildValidityPeriod(supplyAuthorise.getAvailabilityTime()));

        return dispenseRequest;
    }

    private Optional<SimpleQuantity> buildDosageQuantity(PQ quantitySupplied) {
        SimpleQuantity quantity = new SimpleQuantity();
        quantity.setValue(quantitySupplied.getValue());
        if (quantitySupplied.hasTranslation()
            && quantitySupplied.getTranslation().get(0).hasOriginalText()) {
            quantity.setUnit(quantitySupplied.getTranslation().get(0).getOriginalText());
        }
        return Optional.of(quantity);
    }

    private Period buildValidityPeriod(TS timestamp) {
        Period period = new Period();
        period.setStart(DateFormatUtil.parsePathwaysDate(timestamp.getValue()));
        return period;
    }

    private Reference buildMedicationRequestReference(String id) {
        IIdType iIdType = new IdType(ResourceType.MedicationRequest.name(), id);
        return new Reference(iIdType);
    }

    private Extension buildSupplyAuthoriseRepeatAllowedExtension(int repeatCount) {
        return buildRepeatExtension(repeatCount, REPEATS_ALLOWED_URL);
    }

    private Extension buildSupplyAuthoriseRepeatIssuedExtension(int allowedCount) {
        return buildRepeatExtension(allowedCount, REPEATS_ISSUED_URL);
    }

    private Extension buildRepeatExtension(int value, String innerExtensionUrl) {
        Extension extension = new Extension();
        extension.setUrl(REPEAT_INFORMATION_URL);

        Extension innerExtension = new Extension();
        innerExtension.setUrl(innerExtensionUrl);
        innerExtension.setValue(new UnsignedIntType(value));

        extension.addExtension(innerExtension);
        return extension;
    }

    private Optional<String> extractEhrSupplyPrescribeId(RCMRMT030101UK04Prescribe supplyPrescribe) {
        if (supplyPrescribe.hasInFulfillmentOf()
            && supplyPrescribe.getInFulfillmentOf().hasPriorMedicationRef()
            && supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().hasId()
            && supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().getId().hasRoot()) {

            return Optional.of(supplyPrescribe.getInFulfillmentOf().getPriorMedicationRef().getId().getRoot());
        }
        return Optional.empty();
    }

    private Optional<String> extractEhrSupplyAuthoriseId(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasId() && supplyAuthorise.getId().hasRoot()) {
            return Optional.of(supplyAuthorise.getId().getRoot());
        }
        return Optional.empty();
    }

    private Optional<Extension> extractSupplyAuthoriseRepeatInformation(RCMRMT030101UK04Authorise supplyAuthorise) {
        if (supplyAuthorise.hasRepeatNumber() && supplyAuthorise.getRepeatNumber().getValue().intValue() != 0) {
            return Optional.of(buildSupplyAuthoriseRepeatAllowedExtension(supplyAuthorise.getRepeatNumber().getValue().intValue()));
        }
        return Optional.empty();
    }

    private RCMRMT030101UK04Authorise extractSupplyAuthorise(RCMRMT030101UK04MedicationStatement medicationStatement, String id) {
        return medicationStatement.getComponent()
            .stream()
            .filter(RCMRMT030101UK04Component2::hasEhrSupplyAuthorise)
            .map(RCMRMT030101UK04Component2::getEhrSupplyAuthorise)
            .filter(authorise -> authorise.getId().getRoot().equals(id))
            .findFirst()
            .orElse(null);
    }

    private Optional<Extension> extractRepeatInformationIssued(RCMRMT030101UK04MedicationStatement medicationStatement,
        RCMRMT030101UK04Authorise supplyAuthorise) {

        if ((supplyAuthorise.hasRepeatNumber() && supplyAuthorise.getRepeatNumber().getValue().intValue() != 0) || !supplyAuthorise.hasRepeatNumber()) {
            var repeatCount = medicationStatement.getComponent()
                .stream()
                .filter(this::hasInFulfillmentOfReference)
                .count();

            return Optional.of(buildSupplyAuthoriseRepeatIssuedExtension((int)repeatCount));
        }

        return Optional.empty();
    }

    private Optional<RCMRMT030101UK04Discontinue> extractMatchingDiscontinue(String supplyAuthoriseId, RCMRMT030101UK04EhrExtract ehrExtract) {
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

    private boolean hasReversalIdMatchingAuthorise(List<RCMRMT030101UK04ReversalOf> reversalOf, String supplyAuthoriseId) {
        return reversalOf.stream()
            .map(RCMRMT030101UK04ReversalOf::getPriorMedicationRef)
            .map(RCMRMT030101UK04MedicationRef::getId)
            .map(II::getRoot)
            .anyMatch(supplyAuthoriseId::equals);
    }

    private boolean hasAvailability(RCMRMT030101UK04Discontinue discontinue) {
        return discontinue.hasAvailabilityTime() && discontinue.getAvailabilityTime().hasValue();
    }

    private DateTimeType extractAuthoredOn(RCMRMT030101UK04EhrComposition ehrComposition, Date ehrExtractAvailabilityTime) {
        if (ehrComposition.hasAuthor() && ehrComposition.getAuthor().hasTime() && ehrComposition.getAuthor().getTime().hasValue()) {
            return DateFormatUtil.parse(ehrComposition.getAuthor().getTime().getValue());
        } else {
            return new DateTimeType(ehrExtractAvailabilityTime);
        }
    }

    private boolean hasInFulfillmentOfReference(RCMRMT030101UK04Component2 component) {
        return component.hasEhrSupplyPrescribe()
            && component.getEhrSupplyPrescribe().hasInFulfillmentOf()
            && component.getEhrSupplyPrescribe().getInFulfillmentOf().hasPriorMedicationRef()
            && component.getEhrSupplyPrescribe().getInFulfillmentOf().getPriorMedicationRef().hasId();
    }
}
