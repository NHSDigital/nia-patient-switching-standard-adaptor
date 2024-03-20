package uk.nhs.adaptors.pss.translator.util;

import org.hl7.v3.CD;
import org.hl7.v3.CR;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CodeableConceptUtilTest {

    @Test
    public void compareCodeableConceptsWithNullParams() {
        assertTrue(CodeableConceptUtil.compareCodeableConcepts(null, null));
    }

    @Test
    public void compareCodeableConceptsWithFirstNullParam() {
        CD c2 = new CD();
        assertFalse(CodeableConceptUtil.compareCodeableConcepts(null, c2));
    }

    @Test
    public void compareCodeableConceptsWithSecondNullParam() {
        CD c1 = new CD();
        assertFalse(CodeableConceptUtil.compareCodeableConcepts(c1, null));
    }

    @Test
    public void compareCodeableConceptsWithOneTransaction() {

        CD c1 = new CD();
        c1.setCode("Code1");
        c1.setCodeSystem("CodeSystem1");
        c1.setDisplayName("DisplayName1");
        c1.setOriginalText("OriginalText");

        CD translationCD1 = new CD();
        translationCD1.setCode("161586000");
        translationCD1.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.15");
        translationCD1.setDisplayName("H/O: injury");
        c1.getTranslation().add(translationCD1);

        CD c2 = new CD();
        c2.setCode("Code1");
        c2.setCodeSystem("CodeSystem1");
        c2.setDisplayName("DisplayName1");
        c2.setOriginalText("OriginalText");

        CD translationCD2 = new CD();
        translationCD2.setCode("161586000");
        translationCD2.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.15");
        translationCD2.setDisplayName("H/O: injury");
        c2.getTranslation().add(translationCD2);

        assertTrue(CodeableConceptUtil.compareCodeableConcepts(c1, c2));
    }

    @Test
    public void compareCodeableConceptsWithOneQualifierPair() {

        CD c1 = new CD();
        c1.setCode("Code1");
        c1.setCodeSystem("CodeSystem1");
        c1.setDisplayName("DisplayName1");
        c1.setOriginalText("OriginalText");

        CR cr1 = new CR();
        cr1.setCode("QualifierCode");
        cr1.setCodeSystem("QualifierCodeSystemCode");
        cr1.setDisplayName("QualifierDisplayName");
        c1.getQualifier().add(cr1);

        CD c2 = new CD();
        c2.setCode("Code1");
        c2.setCodeSystem("CodeSystem1");
        c2.setDisplayName("DisplayName1");
        c2.setOriginalText("OriginalText");

        CR cr2 = new CR();
        cr2.setCode("QualifierCode");
        cr2.setCodeSystem("QualifierCodeSystemCode");
        cr2.setDisplayName("QualifierDisplayName");
        c2.getQualifier().add(cr2);

        assertTrue(CodeableConceptUtil.compareCodeableConcepts(c1, c2));
    }

    @Test
    public void compareCodeableConceptsWithTwoTransactionsPairsNotIdenticalTranslations() {

        CD c1 = new CD();
        c1.setCode("Code1");
        c1.setCodeSystem("CodeSystem1");
        c1.setDisplayName("DisplayName1");
        c1.setOriginalText("OriginalText");

        CD translationCD1 = new CD();
        translationCD1.setCode("161586000");
        translationCD1.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.15");
        translationCD1.setDisplayName("H/O: injury");
        c1.getTranslation().add(translationCD1);

        CD translationCD2 = new CD();
        translationCD2.setCode("161586007");
        translationCD2.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.17");
        translationCD2.setDisplayName("H/O: injury7");
        c1.getTranslation().add(translationCD2);

        CD c2 = new CD();
        c2.setCode("Code1");
        c2.setCodeSystem("CodeSystem1");
        c2.setDisplayName("DisplayName1");
        c2.setOriginalText("OriginalText");

        CD translationCD3 = new CD();
        translationCD3.setCode("161586000");
        translationCD3.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.15");
        translationCD3.setDisplayName("H/O: injury");

        CD translationCD4 = new CD();
        translationCD4.setCode("161586008");
        translationCD4.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.17");
        translationCD4.setDisplayName("H/O: injury7");

        c2.getTranslation().add(translationCD3);
        c2.getTranslation().add(translationCD4);

        assertFalse(CodeableConceptUtil.compareCodeableConcepts(c1, c2));
    }

    @Test
    public void compareCodeableConceptsWithUniqualNumberOfTransactionPairs() {

        CD c1 = new CD();
        c1.setCode("Code1");
        c1.setCodeSystem("CodeSystem1");
        c1.setDisplayName("DisplayName1");
        c1.setOriginalText("OriginalText");

        CD translationCD1 = new CD();
        translationCD1.setCode("161586000");
        translationCD1.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.15");
        translationCD1.setDisplayName("H/O: injury");
        c1.getTranslation().add(translationCD1);

        CD translationCD2 = new CD();
        translationCD2.setCode("161586007");
        translationCD2.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.17");
        translationCD2.setDisplayName("H/O: injury7");
        c1.getTranslation().add(translationCD2);

        CD c2 = new CD();
        c2.setCode("Code1");
        c2.setCodeSystem("CodeSystem1");
        c2.setDisplayName("DisplayName1");
        c2.setOriginalText("OriginalText");

        CD translationCD3 = new CD();
        translationCD3.setCode("161586000");
        translationCD3.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.15");
        translationCD3.setDisplayName("H/O: injury");

        CD translationCD4 = new CD();
        translationCD4.setCode("161586008");
        translationCD4.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.17");
        translationCD4.setDisplayName("H/O: injury7");

        CD translationCD5 = new CD();
        translationCD5.setCode("161586009");
        translationCD5.setCodeSystem("2.16.840.1.113883.2.1.3.2.4.19");
        translationCD5.setDisplayName("H/O: injury9");

        c2.getTranslation().add(translationCD2);
        c2.getTranslation().add(translationCD3);
        c2.getTranslation().add(translationCD5);

        assertFalse(CodeableConceptUtil.compareCodeableConcepts(c1, c2));
    }

}
