package uk.nhs.adaptors.pss.translator.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;

import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

public class ImmunizationMapper {
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Immunization-1";
    private static final String SNOMED_CODE = "2.16.840.1.113883.2.1.3.2.3.15";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String URL = "https://fhir.hl7.org.uk/STU3/StructureDefinition/Extension-CareConnect-VaccinationProcedure-1";
    private static final String END_DATE = "End Date: ";

    private final CodeableConceptMapper codeableConceptMapper = new CodeableConceptMapper();


    public Immunization mapToImmunization(RCMRMT030101UK04ObservationStatement observationStatement) {

        /**
         * TODO: Known future implementations to this mapper
         * - Patient: references a global patient resource for the transaction (NIAD-2024)
         * - Encounter: references an encounter resource if it has been generated from the ehrComposition (NIAD-2025)
         * - Practitioner: references an Practitioner resource if it has been generated from the any agents (NIAD-1991)
         */

        var id = observationStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var note = buildNotes(Optional.of(observationStatement));

        if (highValueToNotes(observationStatement) != null) {
            var newNote = new Annotation(new StringType(note.get(0).getText()
                + StringUtils.SPACE + (new StringType(END_DATE + highValueToNotes(observationStatement)))));
            note.add(newNote);
        }

        var date = getDate(observationStatement);

        Extension extension = createExtension(observationStatement);

        if (checkSnomedCode(observationStatement)) {
            return createImmunization(id, identifier, note, extension, date);
        }
        return null;
    }

    private Identifier getIdentifier(String id) {
        return new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
    }

    private boolean checkSnomedCode(RCMRMT030101UK04ObservationStatement observationStatement) {
        String snomedCode = observationStatement.getCode().getCodeSystem();

        return SNOMED_CODE.equals(snomedCode);
    }

    private Extension createExtension(RCMRMT030101UK04ObservationStatement observationStatement) {
        return new Extension(URL, codeableConceptMapper.mapToCodeableConcept(observationStatement.getCode()));
    }

    private Date getDate(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.getEffectiveTime().getLow() != null && observationStatement.getEffectiveTime().getHigh() != null) {
            return DateFormatUtil.parse(observationStatement.getEffectiveTime().getLow().getValue()).getValue();
        } else if (observationStatement.getEffectiveTime().getCenter() != null) {
            return DateFormatUtil.parse(observationStatement.getEffectiveTime().getCenter().getValue()).getValue();
        } else if (observationStatement.getEffectiveTime().getLow() != null) {
            return DateFormatUtil.parse(observationStatement.getEffectiveTime().getLow().getValue()).getValue();
        } else if (observationStatement.getEffectiveTime().getHigh() != null
            && observationStatement.getAvailabilityTime() != null
            && observationStatement.getAvailabilityTime().getValue() != null) {
            return DateFormatUtil.parse(observationStatement.getAvailabilityTime().getValue()).getValue();
        } else if (observationStatement.getEffectiveTime() == null && observationStatement.getAvailabilityTime().getValue() != null) {
            return DateFormatUtil.parse(observationStatement.getAvailabilityTime().getValue()).getValue();
        }

        return null;
    }

    private String highValueToNotes(RCMRMT030101UK04ObservationStatement observationStatement) {
        if (observationStatement.getEffectiveTime().getLow() != null && observationStatement.getEffectiveTime().getHigh() != null) {
            return observationStatement.getEffectiveTime().getHigh().getValue();
        } else if (observationStatement.getEffectiveTime().getHigh() != null
            && observationStatement.getAvailabilityTime().getValue() != null) {
            return observationStatement.getEffectiveTime().getHigh().getValue();
        } else if (observationStatement.getEffectiveTime().getHigh() != null
            && observationStatement.getAvailabilityTime().getNullFlavor() != null) {
            return observationStatement.getEffectiveTime().getHigh().getValue();
        }
        return null;
    }

    private List<Annotation> buildNotes(Optional<RCMRMT030101UK04ObservationStatement> observationStatement) {
        List<Annotation> annotationList = new ArrayList<>();

        observationStatement.ifPresent(observationStatement1 -> observationStatement1.getPertinentInformation()
            .stream()
            .map(RCMRMT030101UK04PertinentInformation02::getPertinentAnnotation)
            .filter(Objects::nonNull)
            .map(RCMRMT030101UK04Annotation::getText)
            .filter(StringUtils::isNotBlank)
            .map(StringType::new)
            .map(Annotation::new)
            .forEach(annotationList::add));

        if (observationStatement.get().getCode().getOriginalText() != null) {
            StringType text = new StringType(observationStatement.get().getCode().getOriginalText());
            annotationList.add(new Annotation(text));
        }

        return annotationList;
    }

    private Immunization createImmunization(String id, Identifier identifier, List<Annotation> note,
        Extension extension, Date date) {
        var immunization = new Immunization();

        immunization.getMeta().getProfile().add(new UriType(META_PROFILE));
        immunization.getIdentifier().add(identifier);

        try {
            immunization.getNote().add(note.get(1));
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            immunization.getNote().add(note.get(0));
        }


        immunization.addExtension(extension);
        immunization.setId(id);

        immunization
            .setStatus(Immunization.ImmunizationStatus.COMPLETED)
            .setNotGiven(false)
            .setPrimarySource(false)
            .setDate(date);

        return immunization;
    }
}
