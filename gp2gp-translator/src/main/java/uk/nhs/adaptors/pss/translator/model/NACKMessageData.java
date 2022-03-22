package uk.nhs.adaptors.pss.translator.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
@EqualsAndHashCode
public class NACKMessageData {
    /**
     * The conversation ID the message relates to
     */
    @NonNull
    private String conversationId;
    /**
     * The reason for the negative acknowledgement:
     * <ul>
     *    <li>Large message re-assembly failure = 29</li>
     *    <li>Rejection because one or more attachments were not received = 31</li>
     *    <li>Large message general failure = 30</li>
     *    <li>Large messaging timeout = 25</li>
     * </ul>
     */
    @NonNull
    private String nackCode;
    /**
     * The ODS code of the incumbent system
     */
    @NonNull
    private String toOdsCode;
    /**
     * The UUID of the messages being acknowledged
     */
    @NonNull
    private String messageRef;
    /**
     * The ASID of the incumbent system
     */
    @NonNull
    private String toAsid;
    /**
     * The ASID of the NME system
     */
    @NonNull
    private String fromAsid;
}
