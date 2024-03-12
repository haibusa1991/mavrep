package com.personal.microart.core.scheduling.base;

import com.personal.microart.core.scheduling.base.ScheduledTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DemoScheduled implements ScheduledTask {
    @Override
    public void runScheduledTask() {
        log.info("Running scheduled task");
    }

}
