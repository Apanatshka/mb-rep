package org.spoofax.interpreter.library.index;

import java.net.URI;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface INotificationService {
    /**
     * Notify listener of a added/removed/changed file with optional partition.
     * 
     * @param file The URI of the file
     * @param partition The partition, or null if not applicable.
     */
    void notifyChanges(URI file, String partition);

    /**
     * Notify listeners of multiple added/removed/changed files with optional partitions.
     * 
     * @param files The changed files.
     */
    void notifyChanges(FilePartition[] files);

    /**
     * Notify listener of a new project.
     * 
     * @param project The new project.
     */
    void notifyNewProject(URI project);
}
