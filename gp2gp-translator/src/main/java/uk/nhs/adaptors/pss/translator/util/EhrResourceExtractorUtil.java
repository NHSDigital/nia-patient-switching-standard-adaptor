package uk.nhs.adaptors.pss.translator.util;

import java.util.List;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;

public class EhrResourceExtractorUtil {

    public static List<RCMRMT030101UK04EhrComposition> extractEhrCompositionsFromEhrExtract(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent()
            .stream()
            .filter(EhrResourceExtractorUtil::hasEhrFolder)
            .map(RCMRMT030101UK04Component::getEhrFolder)
            .map(RCMRMT030101UK04EhrFolder::getComponent)
            .flatMap(List::stream)
            .filter(EhrResourceExtractorUtil::hasEhrComposition)
            .map(RCMRMT030101UK04Component3::getEhrComposition)
            .toList();
    }

    public static boolean hasEhrComposition(RCMRMT030101UK04Component3 component) {
        return component.getEhrComposition() != null;
    }

    public static boolean hasEhrFolder(RCMRMT030101UK04Component component) {
        return component.getEhrFolder() != null;
    }
}
