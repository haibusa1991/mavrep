package com.personal.mavrep.core.processor.upload;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PomModel {
    private String groupId;

    private String artifactId;

    private String version;
}
