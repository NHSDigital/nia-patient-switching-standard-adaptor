package uk.nhs.adaptors.pss.translator.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SDSService {
    @SuppressWarnings("checkstyle:MagicNumber")
    public Duration getPersistDurationFor(String messageType) {
        //TODO: query SDS
        LOGGER.debug("Calling SDS service for persist messageType [{}]", messageType);
        return Duration.ofSeconds(2000);
    }
}
