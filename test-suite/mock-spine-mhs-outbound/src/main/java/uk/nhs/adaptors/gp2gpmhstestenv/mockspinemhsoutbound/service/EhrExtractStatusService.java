package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.EhrExtractStatus;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository.EhrExtractStatusRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EhrExtractStatusService {

    private EhrExtractStatusRepository extractStatusRepository;

    public Optional<EhrExtractStatus.EhrReceivedAcknowledgement> findReceivedAcknowledgmentForConversationId(String conversationId) {
        Optional<EhrExtractStatus> extractStatus = Optional.ofNullable(extractStatusRepository.findEhrExtractStatusByConversationId(conversationId));

        return extractStatus.map(EhrExtractStatus::getEhrReceivedAcknowledgement);
    }

    public Optional<EhrExtractStatus.AckToRequester> findSentAcknowledgementForConversationId(String conversationId) {
        Optional<EhrExtractStatus> extractStatus = Optional.ofNullable(extractStatusRepository.findEhrExtractStatusByConversationId(conversationId));

        return extractStatus.map(EhrExtractStatus::getAckToRequester);
    }
}
