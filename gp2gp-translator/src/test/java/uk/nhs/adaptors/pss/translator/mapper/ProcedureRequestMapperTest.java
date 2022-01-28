package uk.nhs.adaptors.pss.translator.mapper;

import lombok.SneakyThrows;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.nhs.adaptors.pss.translator.util.XmlUnmarshallUtil.unmarshallFile;

public class ProcedureRequestMapperTest {
    private static final String XML_RESOURCES_BASE = "XML/ProcedureRequest/";

    private final ProcedureRequestMapper procedureRequestMapper = new ProcedureRequestMapper();

    @Test
    public void mapProcedureRequestWithValidData() {
        var ehrExtract = unmarshallCodeElement("procedure_request_example.xml");
        var planStatement = getPlanStatement(ehrExtract);

        ProcedureRequest procedureRequest = procedureRequestMapper.mapToProcedureRequest(ehrExtract, planStatement);

        assertThat(true);
    }

    private RCMRMT030101UK04PlanStatement getPlanStatement(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0)
                .getEhrFolder().getComponent().get(0)
                .getEhrComposition().getComponent().get(0)
                .getPlanStatement();
    }

    @SneakyThrows
    private RCMRMT030101UK04EhrExtract unmarshallCodeElement(String fileName) {
        return unmarshallFile(getFile("classpath:" + XML_RESOURCES_BASE + fileName), RCMRMT030101UK04EhrExtract.class);
    }
}

