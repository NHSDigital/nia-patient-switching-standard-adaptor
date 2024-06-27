package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKComponent4 extends LinkableComponent {

    RCMRMT030101UKCompoundStatement getCompoundStatement();

    void setCompoundStatement(RCMRMT030101UKCompoundStatement value);

    boolean hasCompoundStatement();

    RCMRMT030101UKEhrEmpty getEhrEmpty();

    void setEhrEmpty(RCMRMT030101UKEhrEmpty value);

    RCMRMT030101UKLinkSet getLinkSet();

    void setLinkSet(RCMRMT030101UKLinkSet value);

    RCMRMT030101UKMedicationStatement getMedicationStatement();

    void setMedicationStatement(RCMRMT030101UKMedicationStatement value);

    boolean hasMedicationStatement();

    RCMRMT030101UKNarrativeStatement getNarrativeStatement();

    void setNarrativeStatement(RCMRMT030101UKNarrativeStatement value);

    RCMRMT030101UKObservationStatement getObservationStatement();

    void setObservationStatement(RCMRMT030101UKObservationStatement value);

    RCMRMT030101UKPlanStatement getPlanStatement();

    void setPlanStatement(RCMRMT030101UKPlanStatement value);

    RCMRMT030101UKRegistrationStatement getRegistrationStatement();

    void setRegistrationStatement(RCMRMT030101UKRegistrationStatement value);

    RCMRMT030101UKRequestStatement getRequestStatement();

    void setRequestStatement(RCMRMT030101UKRequestStatement value);

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
