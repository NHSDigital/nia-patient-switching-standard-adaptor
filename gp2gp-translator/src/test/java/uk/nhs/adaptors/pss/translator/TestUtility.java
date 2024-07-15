package uk.nhs.adaptors.pss.translator;

import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;

import java.util.function.Function;

public final class TestUtility {
    private TestUtility() { }

    public static final Function<RCMRMT030101UKEhrExtract, RCMRMT030101UKEhrComposition> getEhrComposition = extract -> extract
        .getComponent().get(0)
        .getEhrFolder()
        .getComponent().get(0)
        .getEhrComposition();
}
