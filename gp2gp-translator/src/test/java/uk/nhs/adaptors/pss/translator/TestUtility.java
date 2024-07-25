package uk.nhs.adaptors.pss.translator;

import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;

import java.util.function.Function;

public final class TestUtility {
    private TestUtility() { }

    public static final Function<RCMRMT030101UKEhrExtract, RCMRMT030101UKEhrComposition> GET_EHR_COMPOSITION =
        extract -> extract
            .getComponent().get(0)
            .getEhrFolder()
            .getComponent().get(0)
            .getEhrComposition();

    public static CV createCv(String code, String codeSystem, String displayName) {
        final CV cv = new CV();
        cv.setCode(code);
        cv.setCodeSystem(codeSystem);
        cv.setDisplayName(displayName);
        return cv;
    }

    public static CV createCv(String code) {
        return createCv(code, "", "");
    }
}
