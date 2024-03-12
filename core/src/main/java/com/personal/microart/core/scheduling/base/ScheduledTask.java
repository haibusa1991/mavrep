package com.personal.microart.core.scheduling.base;

public interface ScheduledTask {

    /**
     * Represents a scheduled job. You must add the @Scheduled annotation to the implementation of this method.
     */
    void runScheduledTask();
}
