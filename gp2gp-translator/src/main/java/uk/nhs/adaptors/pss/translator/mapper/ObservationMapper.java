package uk.nhs.adaptors.pss.translator.mapper;

import java.util.Date;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.CD;
import org.hl7.v3.IVLTS;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.TS;

import lombok.AllArgsConstructor;
import uk.nhs.adaptors.common.util.DateUtils;
import uk.nhs.adaptors.pss.translator.util.DateFormatUtil;

@AllArgsConstructor
public class ObservationMapper {
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Observation-1";

    private CodeableConceptMapper codeableConceptMapper;

    public Observation mapToObservation(RCMRMT030101UK04ObservationStatement observationStatement) {
        //////// TODO: id DONE
        //////// TODO: meta
        //////// TODO: identifier
        //////// TODO: status
        //////// TODO: code
        //////// TODO: subject
        //////// TODO: context
        //////// TODO: effective
        // TODO: issued
        // TODO: performer
        // TODO: valueQuantity
        // TODO: valueQuantity.extension
        // TODO: valueString ("Negative")
        // TODO: valueString ("Female")
        // TODO: interpretation
        // TODO: comment 1
        // TODO: comment 2
        // TODO: referenceRange

        var id = observationStatement.getId().getRoot();
        var identifier = getIdentifier(id);
        var code = getCode(observationStatement.getCode());
        var effective = getEffective(observationStatement.getEffectiveTime(), observationStatement.getAvailabilityTime());

        /**
         * TODO: Known future implementations to this mapper
         * - subject: references a global patient resource for the transaction (NIAD-2024)
         * - context: references an encounter resource if it has been generated from the ehrComposition (NIAD-2025)
         * - concatenate source practice org id to identifier URL (NIAD-2021)
         */

        return null;
    }

    private Identifier getIdentifier(String id) {
        Identifier identifier = new Identifier()
            .setSystem(IDENTIFIER_SYSTEM) // TODO: concatenate source practice org id to URL (NIAD-2021)
            .setValue(id);
        return identifier;
    }

    private CodeableConcept getCode(CD code) {
        if (code != null) {
            return codeableConceptMapper.mapToCodeableConcept(code);
        }
        return null;
    }

    private Object getEffective(IVLTS effectiveTime, TS availabilityTime) {
        if (effectiveTime != null) {
            if (effectiveTimeHasCenter(effectiveTime)) {
                return effectiveTime.getCenter().getValue();
            }

            var effectivePeriod = new Period();

            if (effectiveTimeHasLow(effectiveTime)) {
                effectivePeriod.setStart(DateFormatUtil.parse(effectiveTime.getLow().getValue()).getValue());
            }

            if (effectiveTimeHasHigh(effectiveTime)) {
                effectivePeriod.setEnd(DateFormatUtil.parse(effectiveTime.getLow().getValue()).getValue());
            }

            if (availabilityTimeHasValue(effectiveTime)) {
                if (effectivePeriod.getStart() == null) {
                    if (effectivePeriod.getEnd() == null) {
                        return availabilityTime.getValue();
                    }

                    effectivePeriod.setStart(DateFormatUtil.parse(availabilityTime.getValue()).getValue());

                    return effectivePeriod;
                }
            }
        }
        return null;
    }

    private boolean effectiveTimeHasCenter(IVLTS effectiveTime) {
        return effectiveTime.getCenter() != null && effectiveTime.getCenter().getValue() != null;
    }

    private boolean effectiveTimeHasLow(IVLTS effectiveTime) {
        return effectiveTime.getLow() != null && effectiveTime.getLow().getValue() != null;
    }

    private boolean effectiveTimeHasHigh(IVLTS effectiveTime) {
        return effectiveTime.getHigh() != null && effectiveTime.getHigh().getValue() != null;
    }

    private boolean availabilityTimeHasValue(TS availabilityTime) {
        return availabilityTime != null && availabilityTime.getValue() != null;
    }

    private Observation createObservation() {
        var observation = new Observation();

        observation.getMeta().getProfile().add(new UriType(META_PROFILE));
        observation.setStatus(Observation.ObservationStatus.FINAL);
        return null;
    }
}
