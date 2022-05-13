package uk.nhs.adaptors.pss.translator.service;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersistDurationServiceTest {

    @Mock
    private MessagePersistDurationService messagePersistDurationService;

    @Mock
    private SDSService sdsService;

    @Mock
    private MessagePersistDuration mockDuration;

    @Mock
    private PatientMigrationRequest migrationRequest;

    @InjectMocks
    private PersistDurationService persistDurationService;

    @Test
    public void testSingleCallPopulatesMessageWhenEmpty() {

        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString())).thenReturn(Optional.empty());

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), any(), anyInt(), anyInt())).thenReturn(MessagePersistDuration.builder().build());

        persistDurationService.getPersistDurationFor(migrationRequest, "PTOS");

        verify(sdsService, times(1)).getPersistDurationFor(any(), any(), any());
    }

    @Test
    public void testOverrangeCallPopulatesMessage() {

        val messagePersistDuration = Optional.of(mockDuration);
        when(mockDuration.getPersistDuration()).thenReturn(Duration.ofSeconds(1));
        when(mockDuration.getCallsSinceUpdate()).thenReturn(4);

        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString())).thenReturn(messagePersistDuration);

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), any(), anyInt(), anyInt())).thenReturn(messagePersistDuration.get());

        persistDurationService.getPersistDurationFor(migrationRequest, "PTOS");

        verify(sdsService, times(1)).getPersistDurationFor(any(), any(), any());
    }

    @Test
    public void testTripleCallPopulatesMessage() {

        val messagePersistDuration = Optional.of(mockDuration);
        when(mockDuration.getPersistDuration()).thenReturn(Duration.ofSeconds(1));
        when(mockDuration.getCallsSinceUpdate()).thenReturn(3);

        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString())).thenReturn(messagePersistDuration);

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), any(), anyInt(), anyInt())).thenReturn(messagePersistDuration.get());

        persistDurationService.getPersistDurationFor(migrationRequest, "PTOS");

        verify(sdsService, times(1)).getPersistDurationFor(any(), any(), any());
    }

    @Test
    public void testUnderrangeCallPopulatesMessage() {

        val messagePersistDuration = Optional.of(mockDuration);
        when(mockDuration.getPersistDuration()).thenReturn(Duration.ofSeconds(1));
        when(mockDuration.getCallsSinceUpdate()).thenReturn(1);
        when(mockDuration.getMessageType()).thenReturn("PT1S");

        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString())).thenReturn(messagePersistDuration);

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), any(), anyInt(), anyInt())).thenReturn(messagePersistDuration.get());

        persistDurationService.getPersistDurationFor(migrationRequest, "PTOS");

        verify(sdsService, never()).getPersistDurationFor(any(), any(), any());
    }

}
