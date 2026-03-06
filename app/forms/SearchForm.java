package forms;

import play.data.validation.Constraints;

/**
 * Form class for handling search input from users.
 * Binds HTTP request data to Java objects using Play Framework's data binding.
 * Includes validation constraints to ensure required fields are present.
 *
 * @author Syed Shahab Shah
 */
public class SearchForm {
    /**
     * The search query string entered by the user.
     * Must not be empty (validated with @Constraints.Required).
     */
    @Constraints.Required
    public String query;

    /**
     * The category to search in: "movie", "tv", or "person".
     * Must not be empty (validated with @Constraints.Required).
     */
    @Constraints.Required
    public String category;

    /**
     * Gets the search query string.
     *
     * @return The search query
     * @author Syed Shahab Shah
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the search query string.
     *
     * @param query The search query to set
     * @author Syed Shahab Shah
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Gets the search category.
     *
     * @return The category ("movie", "tv", or "person")
     * @author Syed Shahab Shah
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the search category.
     *
     * @param category The category to set
     * @author Syed Shahab Shah
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
