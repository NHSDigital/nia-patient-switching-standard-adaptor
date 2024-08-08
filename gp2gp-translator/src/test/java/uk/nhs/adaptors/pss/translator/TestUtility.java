package uk.nhs.adaptors.pss.translator;

import org.hl7.v3.CV;
import org.hl7.v3.RCMRMT030101UKComponent3;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKEhrFolder;

import java.util.function.Function;
import java.util.function.Predicate;

public final class TestUtility {
    private TestUtility() { }

    public static final Function<RCMRMT030101UKEhrExtract, RCMRMT030101UKEhrComposition> GET_EHR_COMPOSITION =
        extract -> extract
            .getComponent().getFirst()
            .getEhrFolder()
            .getComponent().getFirst()
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

    /**
     * An EHR Extract has a cardinality of one to many components, each component (based
     * of the UK05 schema) can contain one and only one EHR Folder. This utility method provides
     * a means of extracting a single component from within a target EHR Folder based on a predicate.
     * @param extract The EHR Extract to be filtered.
     * @param extractComponentIndex The index of the component which houses the EHR Folder.
     * @param searchCriteriaPredicate The predicate to find the component.
     * @return The found RCMRMT030101UKComponent3, or else a ComponentDoesNotExistException will be thrown.
     */
    public static RCMRMT030101UKComponent3 getEhrFolderComponent(RCMRMT030101UKEhrExtract extract,
                                                                 int extractComponentIndex,
                                                                 Predicate<RCMRMT030101UKComponent3> searchCriteriaPredicate) {
        final RCMRMT030101UKEhrFolder targetFolder = extract
            .getComponent()
            .get(extractComponentIndex)
            .getEhrFolder();

        return targetFolder.getComponent().stream()
            .filter(searchCriteriaPredicate)
            .findFirst()
            .orElseThrow(ComponentDoesNotExistException::new);
    }

    public static class NoConfidentialityCodePresentException extends RuntimeException {
        private static final String EXCEPTION_MESSAGE = "No confidentiality code is present within the test file.";

        public NoConfidentialityCodePresentException() {
            super(EXCEPTION_MESSAGE);
        }
    }

    public static class ComponentDoesNotExistException extends RuntimeException {
        private static final String EXCEPTION_MESSAGE = "No component could not be found.";

        public ComponentDoesNotExistException() {
            super(EXCEPTION_MESSAGE);
        }
    }
}
