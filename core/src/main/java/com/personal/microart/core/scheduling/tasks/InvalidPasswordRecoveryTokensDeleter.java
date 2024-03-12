package com.personal.microart.core.scheduling.tasks;

import com.personal.microart.core.scheduling.base.ScheduledTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvalidPasswordRecoveryTokensDeleter implements ScheduledTask {
    @Override
    public void runScheduledTask() {

    }
}
