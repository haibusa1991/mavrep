package com.personal.microart.core.scheduling.tasks;

import com.personal.microart.core.scheduling.base.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deletes all files that are on disk but have no URI associated with them in the database at 02:00 every day.
 */
@Component
@RequiredArgsConstructor
public class OrphanedFilesDeleter implements ScheduledTask {
    @Override
    public void runScheduledTask() {

    }
}
