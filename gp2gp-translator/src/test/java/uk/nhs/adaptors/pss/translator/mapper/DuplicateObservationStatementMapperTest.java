package uk.nhs.adaptors.pss.translator.mapper;

import org.hl7.v3.CD;
import org.hl7.v3.CV;
import org.hl7.v3.II;
import org.hl7.v3.INT;
import org.hl7.v3.RCMRMT030101UK04Annotation;
import org.hl7.v3.RCMRMT030101UK04Component;
import org.hl7.v3.RCMRMT030101UK04Component3;
import org.hl7.v3.RCMRMT030101UK04Component4;
import org.hl7.v3.RCMRMT030101UK04ConditionNamed;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04EhrFolder;
import org.hl7.v3.RCMRMT030101UK04Informant;
import org.hl7.v3.RCMRMT030101UK04LinkSet;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.hl7.v3.RCMRMT030101UK04Participant;
import org.hl7.v3.RCMRMT030101UK04PertinentInformation02;
import org.hl7.v3.RCMRMT030101UK04Reason;
import org.hl7.v3.RCMRMT030101UK04Reference;
import org.hl7.v3.RCMRMT030101UK04ReferenceRange;
import org.hl7.v3.RCMRMT030101UK04ReplacementOf;
import org.hl7.v3.RCMRMT030101UK04SequelTo;
import org.hl7.v3.RCMRMT030101UK04Specimen;
import org.hl7.v3.RCMRMT030101UK04StatementRef;
import org.hl7.v3.RCMRMT030101UK04Subject;
import org.hl7.v3.RCMRMT030101UKComponent4;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrFolder;
import org.hl7.v3.RCMRMT030101UKObservationStatement;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("checkstyle:MagicNumber")
class DuplicateObservationStatementMapperTest {
    private final DuplicateObservationStatementMapper mapper = new DuplicateObservationStatementMapper();

    @Test
    public void ignoresLinksetsWithoutAConditionNamedElement() {
        var ehrExtract = createExtract(List.of(
                generateLinksetComponent()
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(1);
    }

    @Test
    public void removesUnreferencedObservationFromEhrExtractWhenSharedDescription() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-2", "101", "This is an observation which ends with ellipses but there is more."),
                createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(2);
        assertThat(firstEhrComposition(ehrExtract).get(0).getObservationStatement().getId().getRoot()).isEqualTo("ID-1");
    }

    @Test
    public void removesFirstObservationWhenThereAreMultipleWithMatchingDescriptions() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-2", "101", "This is an observation which ends with ellipses but there is more"),
                createObservation("ID-3", "101", "This is an observation which ends with ellipses but there is MORE"),
                createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
        assertThat(firstEhrComposition(ehrExtract).get(0).getObservationStatement().getId().getRoot()).isEqualTo("ID-3");
    }

    @Test
    public void removesObservationInSecondEhrComposition() {
        var ehrExtract = createExtract(
                List.of(),
                List.of(
                        createObservation("ID-2", "101", "This is an observation which ends with ellipses removed."),
                        createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                        generateLinksetComponent("ID-1")
                )
        );

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(secondEhrComposition(firstEhrFolder(ehrExtract)).getComponent()).hasSize(2);
    }

    @Test
    public void removesObservationInSecondEhrFolder() {
        var ehrExtract = createExtract(
                createEhrFolder(List.of()),
                createEhrFolder(List.of(
                        createObservation("ID-2", "101", "This is an observation which ends with ellipses removed."),
                        createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                        generateLinksetComponent("ID-1")
                ))
        );

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(secondEhrFolder(ehrExtract)).getComponent()).hasSize(2);
    }

    @Test
    public void doesntMergeObservationsWhichAreNotReferencedByALinkset() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                createObservation("ID-2", "101", "This is an observation which ends with ellipses..."),
                createObservation("ID-1", "101", "This is an observation which ends with ellipses...")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }

    @Test
    public void doesntMergeObservationWhichIsntReferencedByDifferentLinkset() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                createObservation("ID-2", "101", "This is an observation which ends with ellipses..."),
                generateLinksetComponent("ID-3")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }

    @Test
    public void doesntMergeObservationWhereTheDescriptionDoesntSubStringOfAnother() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                createObservation("ID-2", "101", "ThIS iS An oBSerVATion whIch EnDs wItH eLliPseS bUt there is more."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }


    @Test
    public void doesntMergeObservationWhereTheCodesArentTheSame() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                createObservation("ID-2", "102", "This is an observation which ends with ellipses but there is more."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }


    @Test
    public void mergesWhenTextIsPrefixedWithTextObservationWhereTheCodesArentTheSame() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "PREFIXED STUFF This is an observation which ends with ellipses..."),
                createObservation("ID-2", "101", "This is an observation which ends with ellipses but there is more"),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(2);
    }

    @Test
    public void doesntMergeObservationWhereTheLinkedObservationSequenceNumberIsNot1() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", generateCodeableConcept("101"), "This is an observation which ends with ellipses...", 0),
                createObservation("ID-2", "101", "This is an observation which ends with ellipses removed."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }

    @Test
    public void doesntMergeObservationWhereTheMatchingObservationSequenceNumberIsNot1() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                createObservation("ID-2", generateCodeableConcept("101"), "This is an observation which ends with ellipses removed.", 0),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }

    @Test
    public void doesntMergeObservationWhereTheLinkedObservationHasMultiplePertinentInformation() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-2", "101", "This is an observation which ends with ellipses removed."),
                createObservationWithMultiplePertinentInformation("ID-1", "101", "This is an observation which ends with ellipses..."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }

    @Test
    public void doesntMergeObservationWhereTheMatchingObservationHasMultiplePertinentInformation() {
        var ehrExtract = createExtract(List.of(
            createObservationWithMultiplePertinentInformation("ID-2", "101", "This is an observation which ends with ellipses removed."),
            createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
            generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }

    @Test
    public void doesntMergeObservationWhereTheObservationDoesNotEndInEllipses() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "This is an observation doesnt end with ellipses:::"),
                createObservation("ID-2", "101", "This is an observation which ends with ellipses removed."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }

    @Test
    public void doesntMergeObservationWhereTheObservationIsLessThan47Chars() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "This text is too short..."),
                createObservation("ID-2", "101", "This text is too short to be replaced."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);
    }

    @Test
    public void replacesTextWithinTruncatedObservationWithFullObservation() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-1", "101", "FIRST PREFIX This is an observation which ends with ellipses..."),
                createObservation("ID-2", "101", "SECOND PREFIX This is an observation which ends with ellipses removed."),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstPertinentInformationText(firstEhrComposition(ehrExtract).get(0).getObservationStatement())).isEqualTo(
                "FIRST PREFIX SECOND PREFIX This is an observation which ends with ellipses removed."
        );
    }

    @Test
    public void mergesMultipleTruncatedLinksets() {
        var ehrExtract = createExtract(List.of(
                createObservation("ID-2", "101", "This is an observation which ends with ellipses removed."),
                createObservation("ID-1", "101", "This is an observation which ends with ellipses..."),
                generateLinksetComponent("ID-1"),
                createObservation("ID-4", "102", "THIS IS AN OBSERVATION which ends with ellipses removed."),
                createObservation("ID-3", "102", "THIS IS AN OBSERVATION which ends with ellipses..."),
                generateLinksetComponent("ID-3")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);

        assertThat(firstEhrComposition(ehrExtract)).hasSize(4);
    }

    @ParameterizedTest
    @MethodSource("generator")
    public void doesntMergeObservationWhereTheLinkedObservationFieldIsPopulated(
            Consumer<RCMRMT030101UK04ObservationStatement> observation) {

        var ehrExtract = createExtract(List.of(
                createObservation("ID-2", "101", "This is an observation which ends with ellipses but there is more."),
                createObservationWithFollowingFieldIsPopulated("ID-1", "101", "This is an observation which ends with ellipses...",
                        1, observation),
                generateLinksetComponent("ID-1")
        ));

        mapper.mergeDuplicateObservationStatements(ehrExtract);
        assertThat(firstEhrComposition(ehrExtract)).hasSize(3);

    }

    private static String firstPertinentInformationText(RCMRMT030101UKObservationStatement observationStatement) {
        return observationStatement.getPertinentInformation().get(0).getPertinentAnnotation().getText();
    }

    private static List<RCMRMT030101UKComponent4> firstEhrComposition(RCMRMT030101UK04EhrExtract ehrExtract) {
        return firstEhrComposition(firstEhrFolder(ehrExtract)).getComponent();
    }

    private static RCMRMT030101UKEhrComposition firstEhrComposition(RCMRMT030101UKEhrFolder rcmrmt030101UKEhrFolder) {
        return rcmrmt030101UKEhrFolder.getComponent().get(0).getEhrComposition();
    }

    private static RCMRMT030101UKEhrComposition secondEhrComposition(RCMRMT030101UKEhrFolder rcmrmt030101UKEhrFolder) {
        return rcmrmt030101UKEhrFolder.getComponent().get(1).getEhrComposition();
    }

    private static RCMRMT030101UKEhrFolder firstEhrFolder(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(0).getEhrFolder();
    }

    private static RCMRMT030101UKEhrFolder secondEhrFolder(RCMRMT030101UK04EhrExtract ehrExtract) {
        return ehrExtract.getComponent().get(1).getEhrFolder();
    }

    @NotNull
    private static RCMRMT030101UK04Component4 generateLinksetComponent(String namedConditionId) {
        RCMRMT030101UK04Component4 component = generateLinksetComponent();
        RCMRMT030101UK04ConditionNamed namedCondition = new RCMRMT030101UK04ConditionNamed();
        RCMRMT030101UK04StatementRef value2 = new RCMRMT030101UK04StatementRef();
        value2.setId(generateId(namedConditionId));
        namedCondition.setNamedStatementRef(value2);
        component.getLinkSet().setConditionNamed(namedCondition);
        return component;
    }

    @NotNull
    private static RCMRMT030101UK04Component4 generateLinksetComponent() {
        RCMRMT030101UK04Component4 component = new RCMRMT030101UK04Component4();
        component.setLinkSet(new RCMRMT030101UK04LinkSet());
        return component;
    }

    @NotNull
    private static RCMRMT030101UK04Component4 createObservation(String id, String code, String pertinentAnnotation) {
        return createObservation(id, generateCodeableConcept(code), pertinentAnnotation, 1);
    }

    @NotNull
    private static RCMRMT030101UK04Component4 createObservation(
            String id, @NotNull CD code, String pertinentAnnotation, int annotationSequenceNumber) {
        RCMRMT030101UK04Component4 rcmrmt030101UK04Component4 = new RCMRMT030101UK04Component4();
        RCMRMT030101UK04ObservationStatement observationStatement = new RCMRMT030101UK04ObservationStatement();
        observationStatement.setCode(code);
        observationStatement.setId(generateId(id));
        observationStatement.getPertinentInformation().add(getPertinentInformation(pertinentAnnotation, annotationSequenceNumber));
        rcmrmt030101UK04Component4.setObservationStatement(observationStatement);
        return rcmrmt030101UK04Component4;
    }

    @NotNull
    private static RCMRMT030101UK04Component4 createObservationWithFollowingFieldIsPopulated(
            String id, String code, String pertinentAnnotation, int annotationSequenceNumber,
            Consumer<RCMRMT030101UK04ObservationStatement> observationLambda) {
        RCMRMT030101UK04Component4 rcmrmt030101UK04Component4 = new RCMRMT030101UK04Component4();
        RCMRMT030101UK04ObservationStatement observationStatement = new RCMRMT030101UK04ObservationStatement();
        observationStatement.setCode(generateCodeableConcept(code));
        observationStatement.setId(generateId(id));
        observationStatement.getPertinentInformation().add(getPertinentInformation(pertinentAnnotation, annotationSequenceNumber));
        observationLambda.accept(observationStatement);
        rcmrmt030101UK04Component4.setObservationStatement(observationStatement);
        return rcmrmt030101UK04Component4;
    }

    @NotNull
    private static RCMRMT030101UK04Component4 createObservationWithMultiplePertinentInformation(
            String id, String code, String pertinentAnnotation) {
        var observation = createObservation(id, code, pertinentAnnotation);
        observation.getObservationStatement().getPertinentInformation().add(
                getPertinentInformation(pertinentAnnotation, -1)
        );
        return observation;
    }

    @NotNull
    private static RCMRMT030101UK04PertinentInformation02 getPertinentInformation(String pertinentAnnotation, int sequenceNumber) {
        RCMRMT030101UK04PertinentInformation02 e = new RCMRMT030101UK04PertinentInformation02();
        e.setSequenceNumber(getAnInt(sequenceNumber));
        e.setPertinentAnnotation(getAnnotation(pertinentAnnotation));
        return e;
    }

    @NotNull
    private static RCMRMT030101UK04Annotation getAnnotation(String pertinentAnnotation) {
        RCMRMT030101UK04Annotation annotation = new RCMRMT030101UK04Annotation();
        annotation.setText(pertinentAnnotation);
        return annotation;
    }

    @NotNull
    private static INT getAnInt(int anInt) {
        INT i = new INT();
        i.setValue(BigInteger.valueOf(anInt));
        return i;
    }

    @NotNull
    private static II generateId(String id) {
        II value = new II();
        value.setRoot(id);
        return value;
    }

    @NotNull
    private static CD generateCodeableConcept(String code) {
        CD value = new CD();
        value.setCode(code);
        return value;
    }

    private static RCMRMT030101UK04EhrExtract createExtract(RCMRMT030101UK04Component... folders) {
        RCMRMT030101UK04EhrExtract rcmrmt030101UK04EhrExtract = new RCMRMT030101UK04EhrExtract();
        for (var folder : folders) {
            rcmrmt030101UK04EhrExtract.getComponent().add(folder);
        }
        return rcmrmt030101UK04EhrExtract;
    }

    @SafeVarargs
    private static RCMRMT030101UK04EhrExtract createExtract(List<RCMRMT030101UK04Component4>... components) {
        return createExtract(createEhrFolder(components));
    }

    @NotNull
    @SafeVarargs
    private static RCMRMT030101UK04Component createEhrFolder(List<RCMRMT030101UK04Component4>... components) {
        RCMRMT030101UK04Component rcmrmt030101UK04Component = new RCMRMT030101UK04Component();
        RCMRMT030101UK04EhrFolder rcmrmt030101UK04EhrFolder = new RCMRMT030101UK04EhrFolder();
        for (List<RCMRMT030101UK04Component4> component : components) {
            rcmrmt030101UK04EhrFolder.getComponent().add(generateEhrComposition(component));
        }
        rcmrmt030101UK04Component.setEhrFolder(rcmrmt030101UK04EhrFolder);
        return rcmrmt030101UK04Component;
    }

    @NotNull
    private static RCMRMT030101UK04Component3 generateEhrComposition(List<RCMRMT030101UK04Component4> components) {
        RCMRMT030101UK04Component3 rcmrmt030101UK04Component3 = new RCMRMT030101UK04Component3();
        RCMRMT030101UK04EhrComposition rcmrmt030101UK04EhrComposition = new RCMRMT030101UK04EhrComposition();
        rcmrmt030101UK04EhrComposition.getComponent().addAll(components);
        rcmrmt030101UK04Component3.setEhrComposition(rcmrmt030101UK04EhrComposition);
        return rcmrmt030101UK04Component3;
    }

    private static Stream<Arguments> generator() {

        return Stream.of(
                Arguments.of(Named.of("Priority Code", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.setPriorityCode(new CV()))),
                Arguments.of(Named.of("Uncertainity Code", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.setUncertaintyCode(new CV()))),
                Arguments.of(Named.of("Value Code", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.setValue(new CV()))),
                Arguments.of(Named.of("Interpretation Value", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.setValue(new CV()))),
                Arguments.of(Named.of("Subject Value", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.setSubject(new RCMRMT030101UK04Subject()))),
                Arguments.of(Named.of("Specimen Value", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.getSpecimen()
                                .add(new RCMRMT030101UK04Specimen()))),
                Arguments.of(Named.of("Reference Range Value", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.getReferenceRange()
                                .add(new RCMRMT030101UK04ReferenceRange()))),
                Arguments.of(Named.of("Informant Value", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.getInformant()
                                .add(new RCMRMT030101UK04Informant()))),
                Arguments.of(Named.of("Participant Value", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.getParticipant()
                                .add(new RCMRMT030101UK04Participant()))),
                Arguments.of(Named.of("ReplacementOf Value ", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.getReplacementOf()
                                .add(new RCMRMT030101UK04ReplacementOf()))),
                Arguments.of(Named.of("Reason Value ", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.getReason()
                                .add(new RCMRMT030101UK04Reason()))),
                Arguments.of(Named.of("Reference Value ", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.getReference()
                                .add(new RCMRMT030101UK04Reference()))),
                Arguments.of(Named.of("SequelTo Value ", (Consumer<RCMRMT030101UK04ObservationStatement>)
                        (RCMRMT030101UK04ObservationStatement observation) -> observation.getSequelTo()
                                .add(new RCMRMT030101UK04SequelTo())))
        );
    }

}