package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Duration;

@Entity
@Getter
@Setter
@Builder
@Table(name="message_persist_duration")
public class MessagePersistDuration {

    @Id
    private int id;
    private String messageType;
    private Duration persistDuration;
    private int callsSinceUpdate;
    private int migrationRequestId;
}
