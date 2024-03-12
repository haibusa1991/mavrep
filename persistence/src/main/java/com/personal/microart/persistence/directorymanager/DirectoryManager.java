package com.personal.microart.persistence.directorymanager;

import com.personal.microart.persistence.errors.PersistenceError;
import io.vavr.control.Either;

import java.util.List;

/**
 * The DirectoryManager is responsible for managing the directories on the file system where the files are stored.
 * Files must be organized into directories which are limited in size and file count.
 */
public interface DirectoryManager {

    /**
     * Get the active directory where the next file will be stored.
     * @return the name of the active directory
     */
    Either<PersistenceError, String> getActiveDirectory();

    /**
     * Update the active directory to the next directory.
     *
     * @param currentActiveDirectory the current active directory
     * @param persistedFileName the name of the file that was persisted
     * @return the name of the new active directory
     */

    Either<PersistenceError, String> updateActiveDirectory(String currentActiveDirectory, String persistedFileName);

    /**
     * Get all the files in all the managed directories.
     *
     * @return a list of all the directories
     */
    Either<PersistenceError, List<Directory>> getAllFiles();
}
