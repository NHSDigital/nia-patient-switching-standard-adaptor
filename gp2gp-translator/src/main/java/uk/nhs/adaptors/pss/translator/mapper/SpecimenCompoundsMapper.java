package uk.nhs.adaptors.pss.translator.mapper;

import java.util.List;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.v3.RCMRMT030101UK04EhrExtract;
import org.hl7.v3.RCMRMT030101UK04ObservationStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecimenCompoundsMapper {

    /**
     * TODO
     * The Observation resources generated for each of these components will
     * need to be refernced from DiagnosticReport.results and
     * each Observation will include a reference to the Specimen resource
     */

    private final ObservationMapper observationMapper;
    private final ObservationCommentMapper observationCommentMapper;


    public List<Observation> mapObservations(RCMRMT030101UK04EhrExtract ehrExtract, RCMRMT030101UK04ObservationStatement observationStatement, Patient patient,
        List<Encounter> encounters, String practiseCode) {
        /**
         * TODO: Modify observationMapper to map observations from this level
         * Case 1 - ObservationStatement component
         * This is the simplest of cases - a single ObservationStatement component
         * This should be mapped through a variant of the general ObservationStatement -> Observation transform.
         */
        return observationMapper.mapObservations(ehrExtract, patient, encounters, practiseCode);
    }

    public List<Observation> mapObservations1() {
        /**
         * Case 2 - CompoundStatement classCode="CLUSTER"
         * A CompoundStatement with classCode CLUSTER is the mechanism for grouping result text and
         * any user filing comments in the form of NarrativeStatement with the ObservationStatement carrying the result
         *
         * The single (there will always be only one) ObservationStatement contained within the CompoundStatement is transformed using the same approach as Case 1
         *
         * Every NarrativeStatement within the CompoundStatement is processed in order
         *
         * If the CommentType header is USER COMMENT, then an Observation (Comment) is generated using the NarrativeSatatement to Observation (Comment)
         * map with the GP2GP pathology hreader (CommentType and blank line) stripped. The generated Observation (Comment) resource
         * is referenced by the result Observation generated from the ObservationStatement ( related linkaged of type 'has-member' )
         * and in turn the generated Observation (Comment) references the result observation via a related linkage of type 'derived-from'
         *
         * All other NarrativeStatement within the CompoundStatement are processed into the result Observation comment field - new line seperated
         */
        return List.of();
    }

    public List<Observation> mapObservations2() {
        /**
         * Case 3 - CompoundStatement classCode="BATTERY"
         * For the Battery we want to generate a Panel/Battery header Observation resource using the mapping specified below
         *
         * For every CompoundStatement CLUSTER and ObservationStatement within the BATTERY CompoundStatement we process as Case 2 and Case 1
         * with the difference that we use mutual related linkages between the Panel/Battery header Observation and the result Observations to link results
         * to the battery/panel header. Thes etake the same form as described for user comment observations i.e panel battery has related (has-member)
         * and the result has related (derived-from)
         *
         * Every NarrativeStatement compounent as a direct child of the BATTERY CompoundStatement is processed.
         * USER COMMENT NarrativeStatement are processed in an identical manner to case 2 i.e. output as Observation (Comment) but linked
         * to the panel/battery Observation via .related. Other NarrativeStatement hace their text appended to the panel/battery header Observation Comment
         */
        return List.of();
    }

}
