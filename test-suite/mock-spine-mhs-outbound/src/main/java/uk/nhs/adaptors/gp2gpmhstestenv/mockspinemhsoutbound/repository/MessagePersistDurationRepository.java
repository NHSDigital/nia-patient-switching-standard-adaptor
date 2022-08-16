package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model.MessagePersistDuration;

@Repository
public interface MessagePersistDurationRepository extends CrudRepository<MessagePersistDuration, String>
{

    //Write methods to query database

    //modify services

}
