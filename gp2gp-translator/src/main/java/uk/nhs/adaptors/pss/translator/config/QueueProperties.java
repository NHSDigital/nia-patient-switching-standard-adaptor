package uk.nhs.adaptors.pss.translator.config;

public interface QueueProperties {

    String getQueueName();

    String getBroker();

    String getUsername();

    String getPassword();

    int getCloseTimeout();

    int getSendTimeout();

    void setQueueName(String queueName);

    void setBroker(String broker);

    void setUsername(String username);

    void setPassword(String password);

    void setSendTimeout(int sendTimeoutValue);
}
