package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;

public class EhrResourceExtractorUtil {

    public static RCMRMT030101UK04EhrComposition extractEhrComposition(RCMRMT030101UK04EhrExtract ehrExtract, String resourceId) {
        RCMRMT030101UK04EhrComposition ehrComposition = new RCMRMT030101UK04EhrComposition();
        return ehrComposition;
    }
}
