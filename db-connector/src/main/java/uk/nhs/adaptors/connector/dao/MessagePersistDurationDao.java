package uk.nhs.adaptors.connector.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.locator.UseClasspathSqlLocator;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import uk.nhs.adaptors.connector.model.MessagePersistDuration;

public interface MessagePersistDurationDao {

    @SqlUpdate("save_message_persist_duration")
    @UseClasspathSqlLocator
    void saveMessagePersistDuration(@Bind("messageType") String messageType, @Bind("persistDuration") long persistDuration,
        @Bind("callsSinceUpdate") int callsSinceUpdate);

    @SqlQuery("select_message_persist_duration")
    @UseClasspathSqlLocator
    MessagePersistDuration getMessagePersistDuration(@Bind("messageType") String messageType);

    @SqlQuery("exists_message_persist_duration")
    @UseClasspathSqlLocator
    boolean messageTypeExists(@Bind("messageType") String messageType);
}
