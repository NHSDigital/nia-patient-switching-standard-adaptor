package uk.nhs.adaptors.pss.translator.config;

public interface QueueProperties {

    String getQueueName();

    String getBroker();

    String getUsername();

    String getPassword();

    int getCloseTimeout();

    void setQueueName(String queueName);

    void setBroker(String broker);

    void setUsername(String username);

    void setPassword(String password);

    void setCloseTimeout(int closeTimeoutValue);
}
