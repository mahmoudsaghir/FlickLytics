package models;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link BufferedReaderProcessor} functional interface.
 * <p>
 * This test verifies that the interface correctly processes a BufferedReader
 * and returns the expected output.
 * @author Mahmoud Saghir
 */
public class BufferedReaderProcessorTest {

    /**
     * Tests {@link BufferedReaderProcessor#process(BufferedReader)} with a single line input.
     * <p>
     * Verifies that the processor correctly reads and returns the first line from the BufferedReader.
     *
     * @throws IOException if an I/O error occurs while reading the string
     * @author Mahmoud Saghir
     */
    @Test
    public void testProcessSingleLine() throws IOException {
        String input = "Hello World";
        BufferedReader reader = new BufferedReader(new StringReader(input));

        // Lambda implementation for the functional interface
        BufferedReaderProcessor processor = b -> b.readLine();

        String result = processor.process(reader);
        assertEquals("Hello World", result);
    }
}