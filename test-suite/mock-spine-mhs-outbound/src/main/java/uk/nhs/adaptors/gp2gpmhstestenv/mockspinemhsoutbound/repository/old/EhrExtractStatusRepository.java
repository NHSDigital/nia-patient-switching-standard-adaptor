package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository.old;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.previousmodels.EhrExtractStatus;

@Repository
public interface EhrExtractStatusRepository extends CrudRepository<EhrExtractStatus, String> {
     EhrExtractStatus findEhrExtractStatusByConversationId(String conversationId);
}
