package uk.nhs.adaptors.pss.translator.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.v3.II;
import org.hl7.v3.RCMRMT030101UK04AgentRef;
import org.hl7.v3.RCMRMT030101UK04EhrComposition;
import org.hl7.v3.RCMRMT030101UK04Participant;
import org.hl7.v3.RCMRMT030101UK04Participant2;

public class ParticipantReferenceUtil {
    private static final String PRF_TYPE_CODE = "PRF";
    private static final String PPRF_TYPE_CODE = "PPRF";
    private static final String PRACTITIONER_REFERENCE_PREFIX = "Practitioner/%s";

    public static Reference getParticipantReference(List<RCMRMT030101UK04Participant> participantList,
        RCMRMT030101UK04EhrComposition ehrComposition) {
        var nonNullFlavorParticipants = participantList.stream()
            .filter(ParticipantReferenceUtil::isNotNullFlavour)
            .collect(Collectors.toList());

        var pprfParticipants = getParticipantReference(nonNullFlavorParticipants, PPRF_TYPE_CODE);
        if (pprfParticipants.isPresent()) {
            return new Reference(PRACTITIONER_REFERENCE_PREFIX.formatted(pprfParticipants.get()));
        }

        var prfParticipants = getParticipantReference(nonNullFlavorParticipants, PRF_TYPE_CODE);
        if (prfParticipants.isPresent()) {
            return new Reference(PRACTITIONER_REFERENCE_PREFIX.formatted(prfParticipants.get()));
        }

        var participant2Reference = getParticipant2Reference(ehrComposition);
        if (participant2Reference.isPresent()) {
            return new Reference(PRACTITIONER_REFERENCE_PREFIX.formatted(participant2Reference.get()));
        }

        return null;
    }

    private static Optional<String> getParticipantReference(List<RCMRMT030101UK04Participant> participantList, String typeCode) {
        return participantList.stream()
            .filter(participant -> hasTypeCode(participant, typeCode))
            .filter(ParticipantReferenceUtil::hasAgentReference)
            .map(RCMRMT030101UK04Participant::getAgentRef)
            .map(RCMRMT030101UK04AgentRef::getId)
            .filter(II::hasRoot)
            .map(II::getRoot)
            .findFirst();
    }

    private static Optional<String> getParticipant2Reference(RCMRMT030101UK04EhrComposition ehrComposition) {
        return ehrComposition.getParticipant2().stream()
            .filter(participant2 -> participant2.getNullFlavor() == null)
            .map(RCMRMT030101UK04Participant2::getAgentRef)
            .map(RCMRMT030101UK04AgentRef::getId)
            .filter(II::hasRoot)
            .map(II::getRoot)
            .findFirst();
    }

    private static boolean hasAgentReference(RCMRMT030101UK04Participant participant) {
        return participant.getAgentRef() != null && participant.getAgentRef().getId() != null;
    }

    private static boolean hasTypeCode(RCMRMT030101UK04Participant participant, String typeCode) {
        return !participant.getTypeCode().isEmpty() && participant.getTypeCode().get(0).equals(typeCode);
    }

    private static boolean isNotNullFlavour(RCMRMT030101UK04Participant participant) {
        return participant.getNullFlavor() == null;
    }
}
