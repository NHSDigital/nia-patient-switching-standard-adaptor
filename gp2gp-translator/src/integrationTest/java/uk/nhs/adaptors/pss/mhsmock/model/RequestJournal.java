package uk.nhs.adaptors.pss.mhsmock.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RequestJournal {
    private List<RequestEntry> requests;

    @Getter
    @Setter
    @ToString
    public static class RequestEntry {
        private String id;
        private Request request;
    }
}
