package uk.nhs.adaptors.pss.translator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.connector.model.MessagePersistDuration;
import uk.nhs.adaptors.connector.model.PatientMigrationRequest;
import uk.nhs.adaptors.connector.service.MessagePersistDurationService;
import uk.nhs.adaptors.pss.translator.config.TimeoutProperties;

import java.time.Duration;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class PersistDurationServiceTest {

    private static final int SDS_POLL_FREQUENCY = 3;
    private static final int OVER_RANGE_CALL_AMOUNT = 4;

    @Mock
    private MessagePersistDurationService messagePersistDurationService;

    @Mock
    private SDSService sdsService;

    @Mock
    private MessagePersistDuration mockDuration;

    @Mock
    private PatientMigrationRequest migrationRequest;

    @Mock
    private TimeoutProperties timeoutProperties;

    @InjectMocks
    private PersistDurationService persistDurationService;

    @Test
    public void testSingleCallPopulatesMessageWhenEmpty() {

        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString()))
            .thenReturn(Optional.empty());

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), any(), anyInt(), anyInt()))
            .thenReturn(MessagePersistDuration.builder().build());

        persistDurationService.getPersistDurationFor(migrationRequest, "PTOS");

        verify(sdsService, times(1)).getPersistDurationFor(any(), any(), any());
    }

    @Test
    public void testOverrangeCallPopulatesMessage() {

        val messagePersistDuration = Optional.of(mockDuration);
        when(mockDuration.getPersistDuration()).thenReturn(Duration.ofSeconds(1));
        when(timeoutProperties.getSdsPollFrequency()).thenReturn(SDS_POLL_FREQUENCY);
        when(mockDuration.getCallsSinceUpdate()).thenReturn(OVER_RANGE_CALL_AMOUNT);

        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString()))
            .thenReturn(messagePersistDuration);

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), any(), anyInt(), anyInt()))
            .thenReturn(messagePersistDuration.get());

        persistDurationService.getPersistDurationFor(migrationRequest, "PTOS");

        verify(sdsService, times(1)).getPersistDurationFor(any(), any(), any());
    }

    @Test
    public void testTripleCallPopulatesMessage() {

        val messagePersistDuration = Optional.of(mockDuration);
        when(mockDuration.getPersistDuration()).thenReturn(Duration.ofSeconds(1));
        when(timeoutProperties.getSdsPollFrequency()).thenReturn(SDS_POLL_FREQUENCY);
        when(mockDuration.getCallsSinceUpdate()).thenReturn(SDS_POLL_FREQUENCY);

        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString()))
            .thenReturn(messagePersistDuration);

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), any(), anyInt(), anyInt()))
            .thenReturn(messagePersistDuration.get());

        persistDurationService.getPersistDurationFor(migrationRequest, "PTOS");

        verify(sdsService, times(1)).getPersistDurationFor(any(), any(), any());
    }

    @Test
    public void testUnderRangeCallPopulatesMessage() {

        val messagePersistDuration = Optional.of(mockDuration);
        when(mockDuration.getPersistDuration()).thenReturn(Duration.ofSeconds(1));
        when(timeoutProperties.getSdsPollFrequency()).thenReturn(SDS_POLL_FREQUENCY);
        when(mockDuration.getCallsSinceUpdate()).thenReturn(1);
        when(mockDuration.getMessageType()).thenReturn("PT1S");

        when(messagePersistDurationService.getMessagePersistDuration(anyInt(), anyString()))
            .thenReturn(messagePersistDuration);

        when(messagePersistDurationService.addMessagePersistDuration(anyString(), any(), anyInt(), anyInt()))
            .thenReturn(messagePersistDuration.get());

        persistDurationService.getPersistDurationFor(migrationRequest, "PTOS");

        verify(sdsService, never()).getPersistDurationFor(any(), any(), any());
    }

}
