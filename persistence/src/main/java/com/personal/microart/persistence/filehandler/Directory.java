package com.personal.microart.persistence.filehandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a directory and its contents in the file system.
 */
@AllArgsConstructor
@Builder
@Getter
public class Directory {

    String name;

    String[] content;
}
