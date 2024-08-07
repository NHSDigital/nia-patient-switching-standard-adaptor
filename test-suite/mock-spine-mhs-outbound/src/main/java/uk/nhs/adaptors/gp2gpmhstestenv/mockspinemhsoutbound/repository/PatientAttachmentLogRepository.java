package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.PatientAttachmentLog;

@Repository
public interface PatientAttachmentLogRepository extends CrudRepository<PatientAttachmentLog, String> {


}
