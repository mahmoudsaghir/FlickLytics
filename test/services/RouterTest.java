package services;

import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import play.test.Helpers;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class RouterTest extends WithApplication {

    @Test
    public void testHomeRouteExists() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/flicklytics"); // Updated from "/" to match your index
        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void testSearchRoute() {
        // Your routes file defines search as a POST request
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .uri("/flicklytics?query=batman");
        Result result = route(app, request);
        assertNotEquals(NOT_FOUND, result.status());
    }

    @Test
    public void testDetailsMovieRoute() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/flicklytics/movie/550"); // Path corrected
        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void testDetailsTvRoute() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/flicklytics/tv/1396"); // Path corrected
        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    @Test
    public void testPersonStatsRoute() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(GET)
                .uri("/person/123/stats"); // Path corrected to include /stats
        Result result = route(app, request);
        assertEquals(OK, result.status());
    }
}