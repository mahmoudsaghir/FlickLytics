package models;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Utils} class.
 * <p>
 * This test class verifies utility methods including constructor coverage,
 * rounding behavior, and GET-request handling with mocked HTTP infrastructure.
 *
 * @author Mahmoud Saghir
 */
public class UtilsTest {

    /**
     * Tests default constructor for coverage.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testUtilsConstructor() {
        Utils utils = new Utils();
        assertNotNull(utils);
    }

    /**
     * Tests the {@link Utils#round2(double)} method.
     * Verifies that positive, negative, and zero values are correctly rounded
     * to two decimal places.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testRound2() {
        assertEquals(2.35, Utils.round2(2.3456), 0.0001);
        assertEquals(-1.57, Utils.round2(-1.567), 0.0001);
        assertEquals(0.0, Utils.round2(0.0), 0.0001);
    }

    /**
     * Tests sendGetRequest when token is null.
     * Covers short-circuit branch: token == null.
     *
     * @throws Exception if request processing fails
     * @author Mahmoud Saghir
     */
    @Test
    public void testSendGetRequestWithNullToken() throws Exception {
        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConn = mock(HttpURLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConn);
        when(mockConn.getInputStream()).thenReturn(new ByteArrayInputStream(
                "{\"ok\":true}".getBytes(StandardCharsets.UTF_8)
        ));

        List<Object> uriCtorArgs = new ArrayList<>();
        try (MockedConstruction<URI> mockedUri = mockConstruction(
                URI.class,
                (mock, context) -> {
                    uriCtorArgs.addAll(context.arguments());
                    when(mock.toURL()).thenReturn(mockUrl);
                }
        )) {
            JsonNode result = Utils.sendGetRequest("https://example.com/a b", null);

            assertTrue(result.path("ok").asBoolean());
            assertEquals("https://example.com/a%20b", uriCtorArgs.get(0));
            verify(mockConn).setRequestMethod("GET");
            verify(mockConn).setRequestProperty("Accept", "application/json");
            verify(mockConn, never()).setRequestProperty(eq("Authorization"), anyString());
        }
    }

    /**
     * Tests sendGetRequest when token is an empty string.
     * Covers short-circuit branch: token != null and token.isEmpty().
     *
     * @throws Exception if request processing fails
     * @author Mahmoud Saghir
     */
    @Test
    public void testSendGetRequestWithEmptyToken() throws Exception {
        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConn = mock(HttpURLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConn);
        when(mockConn.getInputStream()).thenReturn(new ByteArrayInputStream(
                "{\"x\":1}".getBytes(StandardCharsets.UTF_8)
        ));

        try (MockedConstruction<URI> mockedUri = mockConstruction(
                URI.class,
                (mock, context) -> when(mock.toURL()).thenReturn(mockUrl)
        )) {
            JsonNode result = Utils.sendGetRequest("https://example.com/test", "");

            assertEquals(1, result.path("x").asInt());
            verify(mockConn, never()).setRequestProperty(eq("Authorization"), anyString());
            verify(mockConn).setRequestProperty("Accept", "application/json");
        }
    }

    /**
     * Tests sendGetRequest when token is provided.
     * Covers true branch of authorization header assignment.
     *
     * @throws Exception if request processing fails
     * @author Mahmoud Saghir
     */
    @Test
    public void testSendGetRequestWithBearerToken() throws Exception {
        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConn = mock(HttpURLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConn);
        when(mockConn.getInputStream()).thenReturn(new ByteArrayInputStream(
                "{\"name\":\"value\"}".getBytes(StandardCharsets.UTF_8)
        ));

        try (MockedConstruction<URI> mockedUri = mockConstruction(
                URI.class,
                (mock, context) -> when(mock.toURL()).thenReturn(mockUrl)
        )) {
            JsonNode result = Utils.sendGetRequest("https://example.com/secure", "abc123");

            assertEquals("value", result.path("name").asText());
            verify(mockConn).setRequestProperty("Authorization", "Bearer abc123");
            verify(mockConn).setRequestProperty("Accept", "application/json");
        }
    }

    /**
     * Tests sendGetRequest when response body is empty.
     * Covers while-loop false branch (no lines to read).
     *
     * @throws Exception if request processing fails
     * @author Mahmoud Saghir
     */
    @Test
    public void testSendGetRequestWithEmptyResponse() throws Exception {
        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConn = mock(HttpURLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConn);
        when(mockConn.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        try (MockedConstruction<URI> mockedUri = mockConstruction(
                URI.class,
                (mock, context) -> when(mock.toURL()).thenReturn(mockUrl)
        )) {
            JsonNode result = Utils.sendGetRequest("https://example.com/empty", "token");
            assertNotNull(result);
        }
    }
}
