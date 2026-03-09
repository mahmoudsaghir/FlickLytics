package models;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Functional interface for processing a {@link BufferedReader}.
 * Used as a lambda target for reading and transforming buffered input streams.
 *
 * @author Tasmia Naomi
 */
@FunctionalInterface
public interface BufferedReaderProcessor {

    /**
     * Processes the given BufferedReader and returns a String result.
     *
     * @param b The BufferedReader to process
     * @return The processed result as a String
     * @throws IOException if an I/O error occurs during processing
     * @author Tasmia Naomi
     */
    String process(BufferedReader b) throws IOException;
}
