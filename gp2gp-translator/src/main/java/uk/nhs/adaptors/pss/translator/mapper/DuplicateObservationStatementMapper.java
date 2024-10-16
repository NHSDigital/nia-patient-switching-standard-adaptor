package uk.nhs.adaptors.pss.translator.mapper;

import lombok.extern.slf4j.Slf4j;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UKEhrExtract;
import org.hl7.v3.RCMRMT030101UKAnnotation;
import org.hl7.v3.RCMRMT030101UKComponent;
import org.hl7.v3.RCMRMT030101UKComponent3;
import org.hl7.v3.RCMRMT030101UKComponent4;
import org.hl7.v3.RCMRMT030101UKConditionNamed;
import org.hl7.v3.RCMRMT030101UKEhrComposition;
import org.hl7.v3.RCMRMT030101UKEhrFolder;
import org.hl7.v3.RCMRMT030101UKLinkSet;
import org.hl7.v3.RCMRMT030101UKObservationStatement;
import org.hl7.v3.RCMRMT030101UKStatementRef;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.nhs.adaptors.pss.translator.util.CodeableConceptUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class DuplicateObservationStatementMapper {
    public static final int CHAR_LIMIT_FOR_TRUNCATION = 50;
    public static final String ELLIPSIS = "...";

    public void mergeDuplicateObservationStatements(RCMRMT030101UKEhrExtract ehrExtract) {
        ehrExtract.getComponent()
                .stream()
                .map(RCMRMT030101UKComponent::getEhrFolder)
                .map(RCMRMT030101UKEhrFolder::getComponent)
                .flatMap(List::stream)
                .map(RCMRMT030101UKComponent3::getEhrComposition)
                .forEach(DuplicateObservationStatementMapper::mergeDuplicateObservationStatements);
    }

    private static void mergeDuplicateObservationStatements(RCMRMT030101UKEhrComposition ehrComposition) {
        getLinksetsIn(ehrComposition).stream()
                .map(id -> getLinkedObservationStatement(ehrComposition, id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(DuplicateObservationStatementMapper::hasSinglePertinentInformation)
                .filter(DuplicateObservationStatementMapper::isAnnotationTruncated)
                .filter(DuplicateObservationStatementMapper::areAdditionalFieldsEmpty)
                .forEach(truncatedObservationStatement ->
                        findAndMergeNonTruncatedObservationStatementIntoTruncatedObservationStatement(
                                truncatedObservationStatement,
                                ehrComposition.getComponent()
                        ));
    }

    @NotNull
    private static List<String> getLinksetsIn(RCMRMT030101UKEhrComposition ehrComposition) {
        return ehrComposition.getComponent().stream()
                .map(RCMRMT030101UKComponent4::getLinkSet)
                .filter(Objects::nonNull)
                .map(RCMRMT030101UKLinkSet::getConditionNamed)
                .filter(Objects::nonNull)
                .map(RCMRMT030101UKConditionNamed::getNamedStatementRef)
                .map(RCMRMT030101UKStatementRef::getId)
                .map(II::getRoot)
                .toList();
    }

    private static void findAndMergeNonTruncatedObservationStatementIntoTruncatedObservationStatement(
            RCMRMT030101UKObservationStatement truncatedObservationStatement, List<RCMRMT030101UKComponent4> components) {


        RCMRMT030101UKAnnotation truncatedPertinentAnnotation = getPertinentAnnotation(truncatedObservationStatement);
        String truncatedAnnotation = truncatedPertinentAnnotation.getText();
        String annotationPrefix = truncatedAnnotation.substring(0, truncatedAnnotation.length() - CHAR_LIMIT_FOR_TRUNCATION);

        for (Iterator<RCMRMT030101UKComponent4> observationIterator = components.iterator(); observationIterator.hasNext();) {
            var observationStatement = observationIterator.next().getObservationStatement();
            if (observationStatement != null
                    && !areSameObservationStatements(truncatedObservationStatement, observationStatement)
                    && hasSinglePertinentInformation(observationStatement)
                    && doesTruncatedAnnotationMatchOtherAnnotation(truncatedPertinentAnnotation,
                          getPertinentAnnotation(observationStatement))
                    && observationsAreCodedTheSame(truncatedObservationStatement, observationStatement)
                    && areAdditionalFieldsEmpty(observationStatement)) {
                observationIterator.remove();
                truncatedPertinentAnnotation.setText(annotationPrefix + getPertinentAnnotation(observationStatement).getText());
                LOGGER.info(
                        "ObservationStatement: '{}' Is truncated version of '{}' and will be merged into a single observation.",
                        truncatedObservationStatement.getId().getRoot(),
                        observationStatement.getId().getRoot()
                );
                return;
            }
        }
        LOGGER.info(
                "ObservationStatement: '{}' appears to have been truncated but no match was found.",
                truncatedObservationStatement.getId().getRoot()
        );
    }

    @NotNull
    private static Boolean observationsAreCodedTheSame(RCMRMT030101UKObservationStatement a, RCMRMT030101UKObservationStatement b) {
        return CodeableConceptUtil.compareCodeableConcepts(a.getCode(), b.getCode());
    }

    private static boolean areSameObservationStatements(RCMRMT030101UKObservationStatement a, RCMRMT030101UKObservationStatement b) {
        return Objects.equals(a.getId().getRoot(), b.getId().getRoot());
    }

    private static RCMRMT030101UKAnnotation getPertinentAnnotation(RCMRMT030101UKObservationStatement linkedObservationStatement) {
        return linkedObservationStatement.getPertinentInformation().getFirst().getPertinentAnnotation();
    }

    private static boolean hasSinglePertinentInformation(RCMRMT030101UKObservationStatement observation) {
        return observation.getPertinentInformation().size() == 1
                && observation.getPertinentInformation().getFirst().getSequenceNumber().getValue().intValueExact() == 1;
    }

    private static boolean doesTruncatedAnnotationMatchOtherAnnotation(
            RCMRMT030101UKAnnotation truncatedPertinentAnnotation, RCMRMT030101UKAnnotation candidateMatchingPertinentAnnotation) {
        String truncatedObservationText = truncatedPertinentAnnotation.getText();
        String truncatedObservationTextWithoutEllipsis = truncatedObservationText.substring(
                truncatedObservationText.length() - CHAR_LIMIT_FOR_TRUNCATION,
                truncatedObservationText.length() - ELLIPSIS.length()
        );
        return candidateMatchingPertinentAnnotation.getText().contains(truncatedObservationTextWithoutEllipsis);
    }

    private static boolean isAnnotationTruncated(RCMRMT030101UKObservationStatement observationStatement) {
        String annotationText = getPertinentAnnotation(observationStatement).getText();
        return annotationText.endsWith(ELLIPSIS) && annotationText.length() >= CHAR_LIMIT_FOR_TRUNCATION;
    }


    private static boolean areAdditionalFieldsEmpty(RCMRMT030101UKObservationStatement observationStatement) {
        return  observationStatement.getPriorityCode() == null
                && observationStatement.getUncertaintyCode() == null
                && observationStatement.getValue() == null
                && observationStatement.getInterpretationCode() == null
                && observationStatement.getSubject() == null
                && (observationStatement.getSpecimen() == null || observationStatement.getSpecimen().isEmpty())
                && (observationStatement.getReferenceRange() == null || observationStatement.getReferenceRange().isEmpty())
                && (observationStatement.getInformant() == null || observationStatement.getInformant().isEmpty())
                && (observationStatement.getParticipant() == null || observationStatement.getParticipant().isEmpty())
                && (observationStatement.getReplacementOf() == null || observationStatement.getReplacementOf().isEmpty())
                && (observationStatement.getReason() == null || observationStatement.getReason().isEmpty())
                && (observationStatement.getReference() == null || observationStatement.getReference().isEmpty())
                && (observationStatement.getSequelTo() == null || observationStatement.getSequelTo().isEmpty());
    }

    @NotNull
    private static Optional<RCMRMT030101UKObservationStatement> getLinkedObservationStatement(
            RCMRMT030101UKEhrComposition ehrComposition, String observationIdReferencedFromLinkset) {

        return ehrComposition.getComponent()
                .stream()
                .map(RCMRMT030101UKComponent4::getObservationStatement)
                .filter(Objects::nonNull)
                .filter(observationStatement -> Objects.equals(observationIdReferencedFromLinkset, observationStatement.getId().getRoot()))
                .findFirst();
    }
}
