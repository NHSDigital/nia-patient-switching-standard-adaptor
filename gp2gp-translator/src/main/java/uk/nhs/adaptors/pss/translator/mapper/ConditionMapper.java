package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.v3.RCMRMT030101UK04LinkSet;

public class ConditionMapper {

    private static final String META_PROFILE = "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-ProblemHeader-Condition-1";
    private static final String IDENTIFIER_SYSTEM = "https://PSSAdaptor/{practiseCode}";

    public Condition mapToCondition(RCMRMT030101UK04LinkSet linkSet) {

        // todo: check if we are generating or reusing id's
        String id = linkSet.getId().getRoot();
        Meta meta = generateConditionMeta();
        Identifier identifier = buildIdentifier(id);
        // need idMapper
        var conditionNamed = linkSet.getConditionNamed().getNamedStatementRef().getId().getRoot();

        return null;
    }

    private Meta generateConditionMeta() {
        Meta meta = new Meta();
        UriType profile = new UriType(META_PROFILE);
        meta.setProfile(List.of(profile));
        return meta;
    }

    private Identifier buildIdentifier(String rootId) {
        Identifier identifier = new Identifier();
        identifier.setSystem(IDENTIFIER_SYSTEM);
        identifier.setValue(rootId);

        return identifier;
    }

}
