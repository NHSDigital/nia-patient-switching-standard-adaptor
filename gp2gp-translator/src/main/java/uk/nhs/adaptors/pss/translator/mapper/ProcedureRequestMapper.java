package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.v3.RCMRMT030101UK04PlanStatement;

public class ProcedureRequestMapper {

    public ProcedureRequest mapToProcedureRequest(RCMRMT030101UK04PlanStatement planStatement) {

        return createProcedureRequest();
    }

    private ProcedureRequest createProcedureRequest() {
        var procedureRequest = new ProcedureRequest();

        return procedureRequest;
    }
}
