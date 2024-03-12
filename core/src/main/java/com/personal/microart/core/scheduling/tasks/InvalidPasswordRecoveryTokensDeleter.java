package com.personal.microart.core.scheduling.tasks;

import com.personal.microart.core.scheduling.base.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deletes all the invalid password recovery tokens from the database at 02:00 every day.
 */
@Component
@RequiredArgsConstructor
public class InvalidPasswordRecoveryTokensDeleter implements ScheduledTask {
    @Override
    public void runScheduledTask() {

    }
}
