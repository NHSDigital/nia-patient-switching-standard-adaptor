package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.PatientMigrationRequest;

@Repository
public interface PatientMigrationRequestRepository extends CrudRepository<PatientMigrationRequest, Integer> {
    PatientMigrationRequest findByConversationId(String conversationId);
}
