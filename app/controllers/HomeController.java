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

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final FormFactory formFactory;
    private final MessagesApi messagesApi;

    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index(Http.Request request) {
        // Pass an empty form to the template for display
        Form<SearchForm> form = formFactory.form(SearchForm.class);
        Messages messages = messagesApi.preferred(request);
        return ok(views.html.index.render(form, request, messages));
    }

    public Result search(Http.Request request) {
        Form<SearchForm> form = formFactory.form(SearchForm.class).bindFromRequest(request);
        Messages messages = messagesApi.preferred(request);

        if (form.hasErrors()) {
            return badRequest(views.html.index.render(form, request, messages));
        }

        return ok("Search works");
    }
}
