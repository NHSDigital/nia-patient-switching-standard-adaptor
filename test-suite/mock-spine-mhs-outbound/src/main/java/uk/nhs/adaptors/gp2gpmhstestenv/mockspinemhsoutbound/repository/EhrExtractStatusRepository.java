package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.EhrExtractStatus;

@Repository
public interface EhrExtractStatusRepository extends CrudRepository<EhrExtractStatus, String> {
     EhrExtractStatus findEhrExtractStatusByConversationId(String conversationId);
}
