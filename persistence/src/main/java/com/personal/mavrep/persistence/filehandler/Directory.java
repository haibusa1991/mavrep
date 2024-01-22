package com.personal.mavrep.persistence.filehandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class Directory {

    String name;

    String[] content;
}
