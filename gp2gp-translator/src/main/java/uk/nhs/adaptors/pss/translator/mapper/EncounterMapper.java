package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterStatus;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CsNullFlavor;
import org.hl7.v3.IVLTS;
import org.hl7.v3.IVXBTS;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.TS;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;
import uk.nhs.adaptors.pss.translator.util.IdentifierUtil;

@Service
@AllArgsConstructor
public class EncounterMapper {
    private static final List<String> INVALID_CODES = List.of("196401000000100", "196391000000103");
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Encounter-1";
    private static final String PATIENT_REFERENCE_PREFIX = "Patient/";

    private CodeableConceptMapper codeableConceptMapper;

    public Encounter mapToEncounter(RCMRMT030101UK04EhrComposition ehrComposition, String patientId) {
        if (!INVALID_CODES.contains(ehrComposition.getCode().getCode())) { // check if suppressed as well

            var id = ehrComposition.getId().getRoot();
            var identifier = IdentifierUtil.getIdentifier(id);
            var type = codeableConceptMapper.mapToCodeableConcept(ehrComposition.getCode());
            var subject = new Reference(PATIENT_REFERENCE_PREFIX + patientId);
            // participant
            var period = getPeriod(ehrComposition.getEffectiveTime(), ehrComposition.getAvailabilityTime());
            // location

            return createEncounter(id, identifier, type, subject, period);
        }

        return null;
    }

    private Period getPeriod(IVLTS effectiveTime, TS availabilityTime) {
        Period period = new Period();

        var center = getTSStringValue(effectiveTime.getCenter());
        var low = getIVXBTSStringValue(effectiveTime.getLow());
        var high = getIVXBTSStringValue(effectiveTime.getHigh());
        var availabilityTimeValue = availabilityTime.getValue();

        if (validValue(center)) {
            return period.setStartElement(DateFormatUtil.parse(center));
        } else if (validValue(low) && validValue(high)) {
            return period.setStartElement(DateFormatUtil.parse(low)).setEndElement(DateFormatUtil.parse(high));
        } else if (validValue(low) && !validValue(high)) {
            return period.setStartElement(DateFormatUtil.parse(low));
        } else if (!validValue(low) && validValue(high) && !validValue(availabilityTimeValue)) {
            return period.setEndElement(DateFormatUtil.parse(high));
        } else if (CsNullFlavor.UNK.value().equals(center)) {
            return null;
        } else if (validValue(availabilityTimeValue)) {
            return period.setStartElement(DateFormatUtil.parse(availabilityTimeValue));
        }

        return null;
    }

    private boolean validValue(String value) {
        return value != null && !CsNullFlavor.UNK.value().equals(value);
    }

    private String getTSStringValue(TS ts) {
        if (ts == null) {
            return null;
        } else if (ts.getValue() != null) {
            return ts.getValue();
        } else if (ts.getNullFlavor().equals(CsNullFlavor.UNK)) {
            return CsNullFlavor.UNK.value();
        }

        return null;
    }

    private String getIVXBTSStringValue(IVXBTS ivxbts) {
        if (ivxbts == null) {
            return null;
        } else if (ivxbts.getValue() != null) {
            return ivxbts.getValue();
        } else if (ivxbts.getNullFlavor().equals(CsNullFlavor.UNK)) {
            return CsNullFlavor.UNK.value();
        }

        return null;
    }

    private Encounter createEncounter(String id, Identifier identifier, CodeableConcept type, Reference subject, Period period) {
        var encounter = new Encounter();

        encounter.getMeta().getProfile().add(new UriType(META_PROFILE));
        encounter.getIdentifier().add(identifier);
        encounter.getType().add(type);
        encounter
            .setPeriod(period)
            .setStatus(EncounterStatus.FINISHED)
            .setSubject(subject)
            .setId(id);

        return encounter;
    }
}
