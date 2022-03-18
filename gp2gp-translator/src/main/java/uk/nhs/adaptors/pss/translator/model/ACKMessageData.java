package uk.nhs.adaptors.pss.translator.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class ACKMessageData {
    /**
     * The conversation ID the message relates to
     */
    @NonNull
    private String conversationId;
    /**
     * The acknowledgement type
     * <pre>
     *     Accept = AA
     *     Error = AE
     *     Reject = AR
     * </pre>
     */
    @NonNull
    private String ackType;
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
