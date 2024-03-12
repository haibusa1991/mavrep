package com.personal.microart.core.scheduling.tasks;

import com.personal.microart.core.scheduling.base.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
/**
 * Deletes all file records that have a URI, but no file associated with them from the database at 02:00 every day.
 */
@Component
@RequiredArgsConstructor
public class OrphanedFileRecordsDeleter implements ScheduledTask {
    @Override
    public void runScheduledTask() {

    }
}
