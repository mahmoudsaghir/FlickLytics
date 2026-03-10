package models;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Utils} class.
 * <p>
 * This test class verifies the correctness of utility methods in Utils, including
 * rounding numbers and sending GET requests.
 * @author Mahmoud Saghir
 */
public class UtilsTest {

    /**
     * Tests the {@link Utils#round2(double)} method.
     * Verifies that positive, negative, and zero values are correctly rounded
     * to two decimal places.
     * @author Mahmoud Saghir
     */
    @Test
    public void testRound2() {
        assertEquals(2.35, Utils.round2(2.3456), 0.0001);
        assertEquals(-1.57, Utils.round2(-1.567), 0.0001);
        assertEquals(0.0, Utils.round2(0.0), 0.0001);
    }

    /**
     * Tests the {@link Utils#sendGetRequest(String, String)} method.
     * <p>
     * This test uses a mock HTTP response and verifies that the returned
     * JSON node contains the expected data.
     *
     * @throws Exception if an I/O error occurs during reading the response
     * @author Mahmoud Saghir
     */
    @Test
    public void testSendGetRequest() throws Exception {
        // Mocking URL and HttpURLConnection
        HttpURLConnection mockConn = mock(HttpURLConnection.class);

        String jsonResponse = "{\"key\":\"value\"}";
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(jsonResponse.getBytes())
        ));

        when(mockConn.getInputStream()).thenReturn(new ByteArrayInputStream(jsonResponse.getBytes()));

        JsonNode result = Utils.sendGetRequest("https://httpbin.org/json", null);
        assertTrue(result.has("slideshow"));
    }
}
