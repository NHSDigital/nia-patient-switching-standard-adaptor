package org.hl7.v3;

import java.util.List;

@Deprecated
public interface RCMRMT030101UKComponent02 extends LinkableComponent {

    INT getSequenceNumber();

    void setSequenceNumber(INT value);

    BL getSeperatableInd();

    void setSeperatableInd(BL value);

    RCMRMT030101UKCompoundStatement getCompoundStatement();

    void setCompoundStatement(RCMRMT030101UKCompoundStatement value);

    boolean hasCompoundStatement();

    RCMRMT030101UKEhrEmpty getEhrEmpty();

    void setEhrEmpty(RCMRMT030101UKEhrEmpty value);

    boolean hasEhrEmpty();

    RCMRMT030101UKLinkSet getLinkSet();

    void setLinkSet(RCMRMT030101UKLinkSet value);

    boolean hasLinkSet();

    RCMRMT030101UKMedicationStatement getMedicationStatement();

    void setMedicationStatement(RCMRMT030101UKMedicationStatement value);

    boolean hasMedicationStatement();

    RCMRMT030101UKNarrativeStatement getNarrativeStatement();

    void setNarrativeStatement(RCMRMT030101UKNarrativeStatement value);

    boolean hasNarrativeStatement();

    RCMRMT030101UKObservationStatement getObservationStatement();

    void setObservationStatement(RCMRMT030101UKObservationStatement value);

    boolean hasObservationStatement();

    RCMRMT030101UKPlanStatement getPlanStatement();

    void setPlanStatement(RCMRMT030101UKPlanStatement value);

    boolean hasPlanStatement();

    RCMRMT030101UKRegistrationStatement getRegistrationStatement();

    void setRegistrationStatement(RCMRMT030101UKRegistrationStatement value);

    boolean hasRegistrationStatement();

    RCMRMT030101UKRequestStatement getRequestStatement();

    void setRequestStatement(RCMRMT030101UKRequestStatement value);

    boolean hasRequestStatement();

    String getType();

    void setType(String value);

    List<String> getTypeCode();

    boolean isContextConductionInd();

    void setContextConductionInd(Boolean value);

    List<String> getTypeID();

    List<String> getRealmCode();

    String getNullFlavor();

    void setNullFlavor(String value);
}
