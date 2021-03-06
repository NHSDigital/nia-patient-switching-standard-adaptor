package uk.nhs.adaptors.pss.translator.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class IdGeneratorService {
    public String generateUuid() {
        return UUID.randomUUID().toString();
    }
}
