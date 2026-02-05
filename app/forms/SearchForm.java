package forms;

import play.data.validation.Constraints;

public class SearchForm {
    @Constraints.Required
    public String query;

    @Constraints.Required
    public String category;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
