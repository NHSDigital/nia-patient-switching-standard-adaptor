package uk.nhs.adaptors.pss.translator.task.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EHRTimeoutHandler {

    private static final String CRON_TIME = "*/30 * * * * SUN-MON";

    @Scheduled(cron = CRON_TIME)
    public void checkForTimeouts() {
        LOGGER.info("running scheduled task");
    }
}
