package services;

import controllers.Assets;
import controllers.ReverseAssets;
import controllers.ReverseHomeController;
import controllers.routes;
import org.junit.Test;
import play.api.routing.JavaScriptReverseRoute;
import play.mvc.Call;
import scala.runtime.AbstractFunction0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests generated reverse route wrappers for coverage.
 *
 * @author Syed Shahab Shah
 */
public class GeneratedRoutesTest {

    @Test
    public void testControllersRoutesConstructorsAndStatics() {
        assertNotNull(new controllers.routes());
        assertNotNull(new routes.javascript());
        assertNotNull(routes.javascript.HomeController);
        assertNotNull(routes.javascript.Assets);
    }

    @Test
    public void testReverseHomeControllerMethodsAndDefaultPrefixBranches() {
        ReverseHomeController withSlash = new ReverseHomeController(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/";
            }
        });

        ReverseHomeController withoutSlash = new ReverseHomeController(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/api";
            }
        });

        assertTrue(withSlash.index().url().contains("flicklytics"));
        assertTrue(withoutSlash.index().url().contains("/api/flicklytics"));

        Call movie = withSlash.movie(1L);
        Call tv = withSlash.tv(2L);
        Call redirect = withSlash.redirectToFlicklytics();
        assertTrue(movie.url().contains("/movie/1"));
        assertTrue(tv.url().contains("/tv/2"));
        assertEquals("/", redirect.url());

        // FIX: search() replaced by ws() — route is now GET /ws/search
        assertTrue(withSlash.ws().url().contains("ws/search"));
        assertTrue(withSlash.personStats("12").url().contains("/person/12/stats"));
        assertTrue(withSlash.reviews("movie", 10L).url().contains("/reviews/movie/10"));
        assertTrue(withSlash.globalDiversity("movie", 5).url().contains("global-diversity/movie/5"));
        assertTrue(withSlash.financialPerformance(9).url().contains("financial-performance/9"));
    }

    @Test
    public void testReverseAssetsDefaultPrefixBranches() {
        ReverseAssets withSlash = new ReverseAssets(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/";
            }
        });

        ReverseAssets withoutSlash = new ReverseAssets(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/api";
            }
        });

        Call c1 = withSlash.versioned(new Assets.Asset("stylesheets/main.css"));
        Call c2 = withoutSlash.versioned(new Assets.Asset("stylesheets/main.css"));
        assertTrue(c1.url().contains("/flicklytics/assets/"));
        assertTrue(c2.url().contains("/api/flicklytics/assets/"));
    }

    @Test
    public void testJavaScriptReverseControllersCoverage() {
        controllers.javascript.ReverseHomeController withSlash =
                new controllers.javascript.ReverseHomeController(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/";
                    }
                });

        controllers.javascript.ReverseHomeController withoutSlash =
                new controllers.javascript.ReverseHomeController(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/api";
                    }
                });

        JavaScriptReverseRoute r1 = withSlash.index();
        JavaScriptReverseRoute r2 = withSlash.movie();
        JavaScriptReverseRoute r3 = withSlash.tv();
        // FIX: search() replaced by ws()
        JavaScriptReverseRoute r4 = withSlash.ws();
        JavaScriptReverseRoute r5 = withSlash.redirectToFlicklytics();
        JavaScriptReverseRoute r6 = withSlash.personStats();
        JavaScriptReverseRoute r7 = withSlash.reviews();
        JavaScriptReverseRoute r8 = withSlash.globalDiversity();
        JavaScriptReverseRoute r9 = withSlash.financialPerformance();
        JavaScriptReverseRoute r10 = withoutSlash.index();

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        assertNotNull(r4);
        assertNotNull(r5);
        assertNotNull(r6);
        assertNotNull(r7);
        assertNotNull(r8);
        assertNotNull(r9);
        assertNotNull(r10);

        controllers.javascript.ReverseAssets jsAssetsWithSlash =
                new controllers.javascript.ReverseAssets(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/";
                    }
                });

        controllers.javascript.ReverseAssets jsAssetsWithoutSlash =
                new controllers.javascript.ReverseAssets(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/api";
                    }
                });

        assertNotNull(jsAssetsWithSlash.versioned());
        assertNotNull(jsAssetsWithoutSlash.versioned());
    }
}
