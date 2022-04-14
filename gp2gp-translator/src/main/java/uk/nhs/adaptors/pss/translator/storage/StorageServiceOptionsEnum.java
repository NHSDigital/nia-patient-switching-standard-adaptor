package uk.nhs.adaptors.pss.translator.storage;

import lombok.Getter;

@Getter
public enum StorageServiceOptionsEnum {
    S3("S3"),
    AZURE("Azure"),
    LOCALMOCK("LocalMock");

    private final String stringValue;

    StorageServiceOptionsEnum(String stringValue) {
        this.stringValue = stringValue;
    }

    public static StorageServiceOptionsEnum enumOf(String enumValue) {
        return valueOf(StorageServiceOptionsEnum.class, enumValue.toUpperCase());
    }
}