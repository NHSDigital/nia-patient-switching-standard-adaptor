package uk.nhs.adaptors.pss.translator.mapper;

import static org.springframework.util.ResourceUtils.getFile;
import static org.assertj.core.api.Assertions.assertThat;

import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

import org.hl7.fhir.dstu3.model.Immunization;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

public class ImmunizationMapperTest {
    private static final String XML_RESOURCES_BASE = "xml/Immunization/";
    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Immunization-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/";

    private final ImmunizationMapper immunizationMapper = new ImmunizationMapper();


    @Test
    public void mapObservationToImmunizationWithValidData() {
        var observationStatement = unmarshallCodeElement("full_valid_immunization.xml");
        Immunization immunization = immunizationMapper.mapToImmunization(observationStatement);

        assertFixedValues(observationStatement, immunization);
    }

    private void assertFixedValues(RCMRMT030101UK04ObservationStatement observationStatement, Immunization immunization) {
        assertThat(immunization.getId()).isEqualTo(observationStatement.getId().getRoot());
        assertThat(immunization.getIdentifier().get(0).getSystem()).isEqualTo(IDENTIFIER_SYSTEM);
        assertThat(immunization.getMeta().getProfile().get(0).getValue()).isEqualTo(META_PROFILE);
        assertThat(immunization.getExtension()).isEqualTo(observationStatement.getCode());
        assertThat(immunization.getStatus()).isEqualTo(Immunization.ImmunizationStatus.COMPLETED);
        assertThat(immunization.getPrimarySource()).isEqualTo(false);
        assertThat(immunization.getDate()).isEqualTo("2010-01-01T12:30:00+00:00");
    }

    @SneakyThrows
    private RCMRMT030101UK04ObservationStatement unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04ObservationStatement.class);
    }


}
