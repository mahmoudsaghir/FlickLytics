package services;

import org.junit.Test;
import play.api.mvc.Handler;
import play.api.mvc.RequestHeader;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import play.test.Helpers;
import router.Routes;
import scala.PartialFunction;
import scala.Tuple3;
import scala.collection.Seq;
import scala.runtime.AbstractFunction1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class RouterTest extends WithApplication {

    private void assertRouteTwice(String method, String uri, int expectedStatus) {
        Result first = route(app, Helpers.fakeRequest().method(method).uri(uri));
        assertEquals(expectedStatus, first.status());

        // Hit the exact same route twice in one app lifecycle to exercise lazy-val branches.
        Result second = route(app, Helpers.fakeRequest().method(method).uri(uri));
        assertEquals(expectedStatus, second.status());
    }

    private void assertRouteNotFoundTwice(String method, String uri) {
        Result first = route(app, Helpers.fakeRequest().method(method).uri(uri));
        assertEquals(NOT_FOUND, first.status());

        Result second = route(app, Helpers.fakeRequest().method(method).uri(uri));
        assertEquals(NOT_FOUND, second.status());
    }

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

    @Test
    public void testRemainingRoutesExist() {
        assertRouteTwice(GET, "/", SEE_OTHER);

        Result globalDiversity = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/global-diversity/movie/550"));
        assertNotEquals(NOT_FOUND, globalDiversity.status());
        Result globalDiversitySecond = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/global-diversity/movie/550"));
        assertNotEquals(NOT_FOUND, globalDiversitySecond.status());

        Result financialPerformance = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/financial-performance/550"));
        assertNotEquals(NOT_FOUND, financialPerformance.status());
        Result financialPerformanceSecond = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/financial-performance/550"));
        assertNotEquals(NOT_FOUND, financialPerformanceSecond.status());

        Result reviews = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/reviews/movie/550"));
        assertNotEquals(NOT_FOUND, reviews.status());
        Result reviewsSecond = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/reviews/movie/550"));
        assertNotEquals(NOT_FOUND, reviewsSecond.status());

        Result asset = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/assets/images/favicon.png"));
        assertNotEquals(NOT_FOUND, asset.status());
        Result assetSecond = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/assets/images/favicon.png"));
        assertNotEquals(NOT_FOUND, assetSecond.status());
    }

    @Test
    public void testRouterRejectsWrongMethodsAndInvalidParameters() {
        assertRouteNotFoundTwice(POST, "/flicklytics/movie/550");
        assertRouteNotFoundTwice(GET, "/flicklytics/does-not-exist");

        Result invalidMovieId = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/movie/not-a-number"));
        assertEquals(BAD_REQUEST, invalidMovieId.status());

        Result invalidTvId = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/tv/not-a-number"));
        assertEquals(BAD_REQUEST, invalidTvId.status());

        Result invalidFinancialId = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/financial-performance/not-a-number"));
        assertEquals(BAD_REQUEST, invalidFinancialId.status());

        Result invalidReviewId = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/reviews/movie/not-a-number"));
        assertEquals(BAD_REQUEST, invalidReviewId.status());

        Result invalidDiversityId = route(app, Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/global-diversity/movie/not-a-number"));
        assertEquals(BAD_REQUEST, invalidDiversityId.status());
    }

    @Test
    public void testGeneratedRouterDocumentationAndPrefixBranches() {
        Routes injectedRoutes = app.injector().instanceOf(Routes.class);
        Seq<Tuple3<String, String, String>> docs = injectedRoutes.documentation();
        assertFalse(docs.isEmpty());
        assertEquals("/", docs.apply(0)._2());
        assertEquals("/flicklytics", docs.apply(1)._2());

        Routes prefixedRoutes = injectedRoutes.withPrefix("/api");
        Seq<Tuple3<String, String, String>> prefixedDocs = prefixedRoutes.documentation();
        assertFalse(prefixedDocs.isEmpty());
        assertEquals("/api", prefixedDocs.apply(0)._2());
        assertEquals("/api/flicklytics", prefixedDocs.apply(1)._2());
    }

    @Test
    public void testGeneratedRouterIsDefinedAtBranches() {
        Routes injectedRoutes = app.injector().instanceOf(Routes.class);
        PartialFunction<RequestHeader, Handler> pf = injectedRoutes.routes();

        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/flicklytics").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(POST).uri("/flicklytics").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/global-diversity/movie/550").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/person/123/stats").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/assets/images/favicon.png").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/flicklytics/movie/550").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/flicklytics/tv/1396").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/financial-performance/550").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/reviews/movie/550").build().asScala()));

        assertFalse(pf.isDefinedAt(Helpers.fakeRequest().method(POST).uri("/").build().asScala()));
        assertFalse(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/flicklytics/unknown").build().asScala()));
        assertFalse(pf.isDefinedAt(Helpers.fakeRequest().method(DELETE).uri("/flicklytics/movie/550").build().asScala()));

        // Invoke the same checks again to exercise already-initialized router branches.
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/flicklytics").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(POST).uri("/flicklytics").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/global-diversity/movie/550").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/person/123/stats").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/assets/images/favicon.png").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/flicklytics/movie/550").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/flicklytics/tv/1396").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/financial-performance/550").build().asScala()));
        assertTrue(pf.isDefinedAt(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/reviews/movie/550").build().asScala()));

        assertFalse(pf.isDefinedAt(Helpers.fakeRequest().method(POST).uri("/").build().asScala()));
        assertFalse(pf.isDefinedAt(Helpers.fakeRequest().method(GET).uri("/flicklytics/unknown").build().asScala()));
        assertFalse(pf.isDefinedAt(Helpers.fakeRequest().method(DELETE).uri("/flicklytics/movie/550").build().asScala()));
    }

    @Test
    public void testGeneratedRouterApplyOrElseBranches() {
        Routes injectedRoutes = app.injector().instanceOf(Routes.class);
        PartialFunction<RequestHeader, Handler> pf = injectedRoutes.routes();
        PartialFunction<RequestHeader, Handler> pfSecondRef = injectedRoutes.routes();
        assertNotNull(pfSecondRef);

        AbstractFunction1<RequestHeader, Handler> fallback = new AbstractFunction1<RequestHeader, Handler>() {
            @Override
            public Handler apply(RequestHeader v1) {
                return null;
            }
        };

        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET).uri("/").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET).uri("/flicklytics").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(POST).uri("/flicklytics").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/global-diversity/movie/550").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET).uri("/person/123/stats").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/assets/images/favicon.png").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET).uri("/flicklytics/movie/550").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET).uri("/flicklytics/tv/1396").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/financial-performance/550").build().asScala(), fallback));
        assertNotNull(pf.applyOrElse(Helpers.fakeRequest().method(GET)
                .uri("/flicklytics/reviews/movie/550").build().asScala(), fallback));

        assertNull(pf.applyOrElse(Helpers.fakeRequest().method(POST).uri("/").build().asScala(), fallback));
        assertNull(pf.applyOrElse(Helpers.fakeRequest().method(GET).uri("/flicklytics/unknown").build().asScala(), fallback));
        assertNull(pf.applyOrElse(Helpers.fakeRequest().method(DELETE).uri("/flicklytics/movie/550").build().asScala(), fallback));
    }

    @Test
    public void testGeneratedRouterConcurrentInitializationPaths() throws InterruptedException {
        Routes seed = app.injector().instanceOf(Routes.class);

        String[] suffixes = new String[]{
                "/",
                "/flicklytics",
                "/flicklytics/global-diversity/movie/550",
                "/person/123/stats",
                "/flicklytics/assets/images/favicon.png",
                "/flicklytics/movie/550",
                "/flicklytics/tv/1396",
                "/flicklytics/financial-performance/550",
                "/flicklytics/reviews/movie/550"
        };

        AbstractFunction1<RequestHeader, Handler> fallback = new AbstractFunction1<RequestHeader, Handler>() {
            @Override
            public Handler apply(RequestHeader v1) {
                return null;
            }
        };

        // Race first-touch reads on fresh Routes instances to exercise lazy-val synchronization branches.
        for (int i = 0; i < 30; i++) {
            String prefix = "/race" + i;
            for (String suffix : suffixes) {
                Routes r = seed.withPrefix(prefix);
                PartialFunction<RequestHeader, Handler> pf = r.routes();

                RequestHeader req = Helpers.fakeRequest().method(GET).uri(prefix + suffix).build().asScala();
                CountDownLatch start = new CountDownLatch(1);
                CountDownLatch done = new CountDownLatch(2);

                Thread t1 = new Thread(() -> {
                    try {
                        start.await();
                        pf.isDefinedAt(req);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });

                Thread t2 = new Thread(() -> {
                    try {
                        start.await();
                        pf.applyOrElse(req, fallback);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });

                t1.start();
                t2.start();
                start.countDown();
                assertTrue(done.await(2, TimeUnit.SECONDS));
            }
        }
    }
}