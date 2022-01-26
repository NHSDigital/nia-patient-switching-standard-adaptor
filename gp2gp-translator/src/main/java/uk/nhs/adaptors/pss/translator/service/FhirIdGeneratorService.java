package uk.nhs.adaptors.pss.translator.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class FhirIDGeneratorService {

    public String generateUuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }

}

