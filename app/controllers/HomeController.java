package controllers;

import forms.SearchForm;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.List;
import java.util.ArrayList;

import models.PersonStats;
import models.MovieOrTVShow;
import services.TMDbService;

/**
 * Main controller for the FlickLytics web application.
 * Handles HTTP requests and responses for the search functionality and person statistics pages.
 * All controller actions return CompletionStage for asynchronous, non-blocking processing.
 *
 * This is the single controller for the entire application as per requirements,
 * with business logic delegated to service and model classes.
 *
 * @author Syed Shahab Shah
 */
public class HomeController extends Controller {

    private final FormFactory formFactory;
    private final MessagesApi messagesApi;
    private final TMDbService tmdbService;

    /**
     * Constructs the HomeController with all required dependencies.
     * Dependencies are injected by Play Framework's Guice injector.
     *
     * @param formFactory Play's form factory for handling form data
     * @param messagesApi Play's messages API for internationalization
     * @param tmdbService The TMDb service for API communication
     * @author Syed Shahab Shah
     */
    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi, TMDbService tmdbService) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
        this.tmdbService = tmdbService;
    }

    /**
     * Handles GET requests to the home page (/).
     * Displays the search form with an empty results list.
     * This action is synchronous as it only renders a static form.
     *
     * @param request The HTTP request object (injected by Play)
     * @return An HTTP result rendering the index page with empty search results
     * @author Syed Shahab Shah
     */
    public Result index(Http.Request request) {
        Form<SearchForm> form = formFactory.form(SearchForm.class);
        Messages messages = messagesApi.preferred(request);
        return ok(views.html.index.render(form, new ArrayList<>(), request, messages));
    }

    /**
     * Handles GET requests to display a person's "known for" items and statistics.
     * Retrieves data asynchronously from the TMDb API and displays comprehensive statistics.
     *
     * This action is asynchronous and non-blocking, returning a CompletionStage.
     * Error handling is included to gracefully report any API failures.
     *
     * @param id The TMDb person ID (passed from the URL)
     * @param request The HTTP request object (injected by Play)
     * @return A CompletionStage containing an HTTP result rendering the person stats page
     * @author Syed Shahab Shah
     */
    public CompletionStage<Result> personStats(String id, Http.Request request) {
        return tmdbService.getPersonStats(id)
                .thenApply(stats -> {
                    Messages messages = messagesApi.preferred(request);
                    return ok(views.html.personStats.render(stats, request, messages));
                })
                .exceptionally(t -> {
                    t.printStackTrace();
                    return internalServerError("Error: " + t.getMessage());
                });
    }

    /**
     * Handles POST requests from the search form.
     * Validates the form data, then retrieves search results from the TMDb API.
     * Displays results on the same index page.
     *
     * This action is asynchronous and non-blocking, returning a CompletionStage.
     * Form validation is performed before API calls. Errors are handled gracefully.
     *
     * @param request The HTTP request object containing form data (injected by Play)
     * @return A CompletionStage containing an HTTP result rendering the index page with results
     * @author Syed Shahab Shah
     */
    public CompletionStage<Result> search(Http.Request request) {
        Form<SearchForm> form = formFactory.form(SearchForm.class).bindFromRequest(request);
        Messages messages = messagesApi.preferred(request);

        if (form.hasErrors()) {
            return CompletableFuture.completedFuture(
                    badRequest(views.html.index.render(form, new ArrayList<>(), request, messages))
            );
        }

        return tmdbService.searchAnything(form.get().getQuery(), form.get().getCategory())
                .thenApply(results -> {
                    return ok(views.html.index.render(form, results, request, messages));
                })
                .exceptionally(t -> {
                    t.printStackTrace();
                    return internalServerError("Search failed: " + t.getMessage());
                });
    }
}