package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Readability.ReadabilityScores;
import org.junit.Before;
import org.junit.Test;
import org.webjars.play.WebJarsUtil;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.test.Helpers;
import play.test.WithApplication;

import static org.junit.Assert.*;

public class DetailsMovieTvTest extends WithApplication {

    private ObjectMapper mapper;
    private Http.Request  request;
    private Messages      messages;
    private WebJarsUtil webJarsUtil;

    // ── Scores helper ─────────────────────────────────────────────────────
    // Uses clean doubles to avoid floating-point rendering surprisesjaco

    private ReadabilityScores scores() {
        return new ReadabilityScores(65.0, 8.0, 10, 200, 310);
    }

    private ReadabilityScores scores(double flesch, double grade,
                                     int sentences, int words, int syllables) {
        return new ReadabilityScores(flesch, grade, sentences, words, syllables);
    }

    // ── Setup ─────────────────────────────────────────────────────────────

    @Before
    public void setUp() {
        mapper   = new ObjectMapper();
        request  = Helpers.fakeRequest().build();
        messages = app.injector().instanceOf(MessagesApi.class).preferred(request);
        webJarsUtil = app.injector().instanceOf(org.webjars.play.WebJarsUtil.class);
    }

    // ── Render helper ─────────────────────────────────────────────────────

    private String render(String itemType, ObjectNode details,
                          String overview, ReadabilityScores scores) {
        return views.html.details
                .render(itemType, details, overview, scores, request, messages, webJarsUtil)
                .body();
    }

    // ── JSON node builders ────────────────────────────────────────────────

    private ObjectNode movieNode() {
        ObjectNode n = mapper.createObjectNode();
        n.put("release_date",       "2023-06-15");
        n.put("runtime",            120);
        n.put("popularity",         87.4);
        n.put("status",             "Released");
        n.put("tagline",            "A great movie");
        n.put("original_language",  "en");
        n.put("vote_average",       7.8);
        n.put("vote_count",         5000);
        n.put("homepage",           "https://example.com");
        n.put("poster_path",        "/poster.jpg");
        ArrayNode genres = n.putArray("genres");
        genres.addObject().put("name", "Action");
        genres.addObject().put("name", "Drama");
        ArrayNode companies = n.putArray("production_companies");
        companies.addObject().put("name", "Studio A");
        companies.addObject().put("name", "Studio B");
        return n;
    }

    private ObjectNode tvNode() {
        ObjectNode n = mapper.createObjectNode();
        n.put("first_air_date",      "2020-01-10");
        n.put("last_air_date",       "2023-05-14");
        n.put("popularity",          120.5);
        n.put("status",              "Ended");
        n.put("tagline",             "A great show");
        n.put("type",                "Scripted");
        n.put("vote_average",        8.5);
        n.put("vote_count",          12000);
        n.put("number_of_seasons",   4);
        n.put("number_of_episodes",  48);
        n.put("homepage",            "https://tvshow.example.com");
        n.put("poster_path",         "/tvposter.jpg");
        ArrayNode genres = n.putArray("genres");
        genres.addObject().put("name", "Drama");
        genres.addObject().put("name", "Thriller");
        ArrayNode networks = n.putArray("networks");
        networks.addObject().put("name", "HBO");
        networks.addObject().put("name", "Sky");
        return n;
    }

    // ══════════════════════════════════════════════════════════════════════
    // 1. PAGE STRUCTURE
    // ══════════════════════════════════════════════════════════════════════

    @Test
    public void testContainerDivPresent() {
        assertTrue(render("movie", movieNode(), "Overview.", scores())
                .contains("container mt-4"));
    }

    @Test
    public void testBackButtonPresent() {
        String html = render("movie", movieNode(), "Overview.", scores());
        assertTrue(html.contains("Back to Search"));
        assertTrue(html.contains("window.history.back()"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // 2. HEADING BRANCHING
    // Covers YELLOW if/else on itemType — both sides must be exercised.
    // ══════════════════════════════════════════════════════════════════════

    @Test
    public void testMovieHeading() {
        String html = render("movie", movieNode(), "Overview.", scores());
        assertTrue(html.contains("Movie Details"));
        assertFalse(html.contains("TV Show Details"));
    }

    @Test
    public void testTvHeading() {
        // RED line: itemType == "tv" branch was never taken
        String html = render("tv", tvNode(), "Overview.", scores());
        assertTrue(html.contains("TV Show Details"));
        assertFalse(html.contains("Movie Details"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // 3. MOVIE FIELDS
    // ══════════════════════════════════════════════════════════════════════

    @Test
    public void testMovieReleaseDate() {
        assertTrue(render("movie", movieNode(), "Overview.", scores())
                .contains("2023-06-15"));
    }

    @Test
    public void testMovieRuntime() {
        assertTrue(render("movie", movieNode(), "Overview.", scores())
                .contains("120"));
    }

    @Test
    public void testMovieStatus() {
        assertTrue(render("movie", movieNode(), "Overview.", scores())
                .contains("Released"));
    }

    @Test
    public void testMovieTagline() {
        assertTrue(render("movie", movieNode(), "Overview.", scores())
                .contains("A great movie"));
    }

    @Test
    public void testMovieOriginalLanguage() {
        assertTrue(render("movie", movieNode(), "Overview.", scores())
                .contains("en"));
    }

    @Test
    public void testMovieVoteAverageAndCount() {
        String html = render("movie", movieNode(), "Overview.", scores());
        assertTrue(html.contains("7.8"));
        assertTrue(html.contains("5000"));
    }

    @Test
    public void testMovieGenres() {
        // YELLOW: gs.isArray TRUE side
        String html = render("movie", movieNode(), "Overview.", scores());
        assertTrue(html.contains("Action"));
        assertTrue(html.contains("Drama"));
    }

    @Test
    public void testMovieProductionCompanies() {
        // YELLOW: pcs.isArray TRUE side
        String html = render("movie", movieNode(), "Overview.", scores());
        assertTrue(html.contains("Studio A"));
        assertTrue(html.contains("Studio B"));
    }

    @Test
    public void testMoviePosterRendered() {
        // YELLOW: poster_path != "" TRUE side (movie)
        String html = render("movie", movieNode(), "Overview.", scores());
        assertTrue(html.contains("image.tmdb.org/t/p/w500/poster.jpg"));
        assertTrue(html.contains("alt=\"poster\""));
    }

    @Test
    public void testMoviePosterNotRenderedWhenEmpty() {
        // RED: poster_path == "" → else {null} (movie)
        ObjectNode node = movieNode();
        node.put("poster_path", "");
        assertFalse(render("movie", node, "Overview.", scores())
                .contains("image.tmdb.org"));
    }

    @Test
    public void testMovieHomepageLink() {
        // YELLOW: hp != "" TRUE side (movie)
        String html = render("movie", movieNode(), "Overview.", scores());
        assertTrue(html.contains("https://example.com"));
        assertTrue(html.contains("target=\"_blank\""));
    }

    @Test
    public void testMovieHomepageShowsNAWhenEmpty() {
        // RED: hp == "" → N/A (movie)
        ObjectNode node = movieNode();
        node.put("homepage", "");
        String html = render("movie", node, "Overview.", scores());
        assertTrue(html.contains("N/A"));
        assertFalse(html.contains("target=\"_blank\""));
    }

    @Test
    public void testMovieDoesNotShowTvFields() {
        String html = render("movie", movieNode(), "Overview.", scores());
        assertFalse(html.contains("First air date"));
        assertFalse(html.contains("Number of seasons"));
        assertFalse(html.contains("Networks"));
    }

    @Test
    public void testMovieGenresNotArray_NA() {
        // RED: gs.isArray == false → N/A (movie)
        ObjectNode node = movieNode();
        node.put("genres", "");
        assertTrue(render("movie", node, "Overview.", scores())
                .contains("N/A"));
    }

    @Test
    public void testMovieProductionCompaniesNotArray_NA() {
        // RED: pcs.isArray == false → N/A
        ObjectNode node = movieNode();
        node.put("production_companies", "");
        assertTrue(render("movie", node, "Overview.", scores())
                .contains("N/A"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // 4. TV SHOW FIELDS
    // ══════════════════════════════════════════════════════════════════════

    @Test
    public void testTvInformationBlock() {
        // RED: TV info block (first/last air date, seasons, episodes)
        String html = render("tv", tvNode(), "Overview.", scores());
        assertTrue(html.contains("TV Show Information"));
        assertTrue(html.contains("First air date"));
        assertTrue(html.contains("Last air date"));
        assertTrue(html.contains("Number of seasons"));
        assertTrue(html.contains("Number of episodes"));
    }

    @Test
    public void testTvFirstAndLastAirDate() {
        String html = render("tv", tvNode(), "Overview.", scores());
        assertTrue(html.contains("2020-01-10"));
        assertTrue(html.contains("2023-05-14"));
    }

    @Test
    public void testTvType() {
        assertTrue(render("tv", tvNode(), "Overview.", scores())
                .contains("Scripted"));
    }

    @Test
    public void testTvSeasonsAndEpisodes() {
        String html = render("tv", tvNode(), "Overview.", scores());
        assertTrue(html.contains("4"));
        assertTrue(html.contains("48"));
    }

    @Test
    public void testTvNetworks() {
        // YELLOW: ns.isArray TRUE side
        String html = render("tv", tvNode(), "Overview.", scores());
        assertTrue(html.contains("HBO"));
        assertTrue(html.contains("Sky"));
    }

    @Test
    public void testTvPosterRendered() {
        // YELLOW: poster_path != "" TRUE side (tv)
        assertTrue(render("tv", tvNode(), "Overview.", scores())
                .contains("image.tmdb.org/t/p/w500/tvposter.jpg"));
    }

    @Test
    public void testTvPosterNotRenderedWhenEmpty() {
        // RED: poster_path == "" → else {null} (tv)
        ObjectNode node = tvNode();
        node.put("poster_path", "");
        assertFalse(render("tv", node, "Overview.", scores())
                .contains("image.tmdb.org"));
    }

    @Test
    public void testTvHomepageLink() {
        // YELLOW: hp != "" TRUE side (tv)
        String html = render("tv", tvNode(), "Overview.", scores());
        assertTrue(html.contains("https://tvshow.example.com"));
        assertTrue(html.contains("target=\"_blank\""));
    }

    @Test
    public void testTvHomepageShowsNAWhenEmpty() {
        // RED: hp == "" → N/A (tv)
        ObjectNode node = tvNode();
        node.put("homepage", "");
        String html = render("tv", node, "Overview.", scores());
        assertTrue(html.contains("N/A"));
        assertFalse(html.contains("target=\"_blank\""));
    }

    @Test
    public void testTvDoesNotShowMovieFields() {
        String html = render("tv", tvNode(), "Overview.", scores());
        assertFalse(html.contains("Release date"));
        assertFalse(html.contains("Runtime"));
        assertFalse(html.contains("Original language"));
    }

    @Test
    public void testTvGenresNotArray_NA() {
        // RED: gs.isArray == false → N/A (tv)
        ObjectNode node = tvNode();
        node.put("genres", "");
        assertTrue(render("tv", node, "Overview.", scores())
                .contains("N/A"));
    }

    @Test
    public void testTvNetworksNotArray_NA() {
        // RED: ns.isArray == false → N/A (tv)
        ObjectNode node = tvNode();
        node.put("networks", "");
        assertTrue(render("tv", node, "Overview.", scores())
                .contains("N/A"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // 5. OVERVIEW SECTION
    // ══════════════════════════════════════════════════════════════════════

    @Test
    public void testOverviewHeading() {
        assertTrue(render("movie", movieNode(), "Some text.", scores())
                .contains("Overview"));
    }

    @Test
    public void testOverviewTextRendered() {
        assertTrue(render("movie", movieNode(), "An epic tale.", scores())
                .contains("An epic tale."));
    }

    @Test
    public void testEmptyOverview() {
        String html = render("movie", movieNode(), "", scores());
        assertNotNull(html);
        assertTrue(html.contains("<p"));   // <p> tag present even when empty
    }

    // ══════════════════════════════════════════════════════════════════════
    // 6. READABILITY SECTION
    // ══════════════════════════════════════════════════════════════════════

    @Test
    public void testReadabilityHeading() {
        assertTrue(render("movie", movieNode(), "Overview.", scores())
                .contains("Readability"));
    }

    @Test
    public void testFleschReadingEase() {
        // Use a clean value — avoids floating-point rendering like 72.30000000000001
        String html = render("movie", movieNode(), "Overview.", scores(70.0, 6.0, 10, 200, 300));
        assertTrue(html.contains("70.0") || html.contains(">70<"));
    }

    @Test
    public void testFleschKincaidGrade() {
        String html = render("movie", movieNode(), "Overview.", scores(70.0, 6.0, 10, 200, 300));
        assertTrue(html.contains("6.0") || html.contains(">6<"));
    }

    @Test
    public void testSentencesWordsAndSyllables() {
        String html = render("movie", movieNode(), "Overview.", scores(65.0, 8.0, 42, 388, 512));
        assertTrue(html.contains("42"));
        assertTrue(html.contains("388"));
        assertTrue(html.contains("512"));
    }

    // ══════════════════════════════════════════════════════════════════════
    // 7. EDGE CASES
    // ══════════════════════════════════════════════════════════════════════

    @Test
    public void testEmptyJsonNodeDoesNotThrow() {
        assertNotNull(render("movie", mapper.createObjectNode(), "Overview.", scores()));
    }

    @Test
    public void testZeroRuntimeAndVotes() {
        ObjectNode node = movieNode();
        node.put("runtime",    0);
        node.put("vote_count", 0);
        assertNotNull(render("movie", node, "Overview.", scores()));
    }

    @Test
    public void testZeroSeasonsAndEpisodes() {
        ObjectNode node = tvNode();
        node.put("number_of_seasons",  0);
        node.put("number_of_episodes", 0);
        assertNotNull(render("tv", node, "Overview.", scores()));
    }

    @Test
    public void testUnknownItemTypeRendersNeitherBranch() {
        String html = render("unknown", movieNode(), "Overview.", scores());
        assertFalse(html.contains("<h3>Movie Details</h3>"));
        assertFalse(html.contains("<h3>TV Show Details</h3>"));
        assertTrue(html.contains("Overview"));
        assertTrue(html.contains("Readability"));
    }

    @Test
    public void testZeroReadabilityScores() {
        assertNotNull(render("movie", movieNode(), "Overview.",
                scores(0.0, 0.0, 0, 0, 0)));
    }

}