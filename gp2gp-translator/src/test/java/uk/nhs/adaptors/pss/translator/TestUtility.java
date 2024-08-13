package uk.nhs.adaptors.pss.translator;

import org.hl7.v3.CD;
import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKComponent3;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKEhrFolder;

import java.util.List;
import java.util.Objects;

public final class TestUtility {
    private TestUtility() { }

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

    public static CD createCd(String code, String codeSystem, String displayName) {
        final CD cd = new CD();
        cd.setCode(code);
        cd.setCodeSystem(codeSystem);
        cd.setDisplayName(displayName);
        return cd;
    }

    /**
     * An EHR Extract has a cardinality of one to many components, each component (based
     * of the UK05 schema) can contain one and only one EHR Folder. This utility method provides
     * a means of extracting ALL components from within a target EHR Folder.
     * @param extract The EHR Extract.
     * @param extractComponentIndex The index of the RCMRMT030101UKComponent which houses the EHR Folder.
     * @return A list of RCMRMT030101UKComponent3.
     */
    public static List<RCMRMT030101UKComponent3> getEhrFolderComponents(RCMRMT030101UKEhrExtract extract,
                                                                        int extractComponentIndex) {
        final RCMRMT030101UKEhrFolder targetFolder = extract
            .getComponent()
            .get(extractComponentIndex)
            .getEhrFolder();

        return targetFolder.getComponent();
    }

    public static RCMRMT030101UKEhrComposition getEhrComposition(RCMRMT030101UKEhrExtract extract,
                                                                 int ehrExtractComponentIndex,
                                                                 int ehrFolderComponentIndex) {
        if (ehrExtractComponentIndex < 0 || ehrFolderComponentIndex < 0) {
            throw new IllegalArgumentException("Indexes must be >= 0");
        }

        return Objects.requireNonNull(extract)
            .getComponent().get(ehrExtractComponentIndex)
            .getEhrFolder()
            .getComponent().get(ehrFolderComponentIndex)
            .getEhrComposition();
    }

    public static class NoConfidentialityCodePresentException extends RuntimeException {
        private static final String EXCEPTION_MESSAGE = "No confidentiality code is present within the test file.";

        public NoConfidentialityCodePresentException() {
            super(EXCEPTION_MESSAGE);
        }
    }
}