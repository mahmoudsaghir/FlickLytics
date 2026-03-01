package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.GlobalDiversityResult;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import services.GenreService;
import services.GlobalDiversityService;
import services.TmdbService;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;

public class HomeControllerTest {
    private HomeController controller;
    private TmdbService tmdbService;
    private GlobalDiversityService globalDiversityService;

    @Before
    public void setUp() {
        tmdbService = mock(TmdbService.class);
        globalDiversityService = mock(GlobalDiversityService.class);
        GenreService genreService = mock(GenreService.class);
        Config config = mock(Config.class);

        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");

        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);

        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        controller = new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                globalDiversityService,
                genreService,
                tmdbService
        );
    }

    @Test
    public void testGlobalDiversity() throws Exception {
        // Mock TMDb responses
        JsonNode detailsNode = Json.newObject().put("overview", "abc");
        JsonNode translationsNode = Json.newObject().set("translations", Json.newArray());

        when(tmdbService.getDetails(anyString(), anyString(), eq("movie"), eq(1L)))
                .thenReturn(detailsNode);

        when(tmdbService.getTranslations(anyString(), anyString(), eq("movie"), eq(1L)))
                .thenReturn(translationsNode);

        // Mock computed result
        GlobalDiversityResult mockResult = new GlobalDiversityResult(0.5, 0.8, "Test Movie");

        when(globalDiversityService.compute(eq("movie"), any(), any(), eq(10)))
                .thenReturn(mockResult);

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.globalDiversity(request, "movie", 1);

        Result result = resultStage.toCompletableFuture().join();
        String html = contentAsString(result);

        assertEquals(OK, result.status());
        assertTrue(html.contains("Test Movie"));
        assertTrue(html.contains("Translation Density"));
        assertTrue(html.contains("0.5"));
        assertTrue(html.contains("Localization Index"));
        assertTrue(html.contains("0.8"));
    }
}