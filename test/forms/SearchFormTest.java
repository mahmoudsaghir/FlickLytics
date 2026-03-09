package forms;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the SearchForm class.
 * Tests getter/setter methods and field assignments.
 *
 * Testing strategy:
 * - Query getter/setter with normal, empty, null values
 * - Category getter/setter for all valid categories
 * - Direct field access
 * - Multi-word and special character queries
 *
 * @author Tasmia Naomi
 */
public class SearchFormTest {

    /**
     * Tests setQuery and getQuery with a normal search term.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSetQuery() {
        SearchForm form = new SearchForm();
        form.setQuery("Batman");
        assertEquals("Batman", form.getQuery());
    }

    /**
     * Tests setCategory and getCategory with "movie".
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSetCategoryMovie() {
        SearchForm form = new SearchForm();
        form.setCategory("movie");
        assertEquals("movie", form.getCategory());
    }

    /**
     * Tests setCategory and getCategory with "tv".
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSetCategoryTv() {
        SearchForm form = new SearchForm();
        form.setCategory("tv");
        assertEquals("tv", form.getCategory());
    }

    /**
     * Tests setCategory and getCategory with "person".
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSetCategoryPerson() {
        SearchForm form = new SearchForm();
        form.setCategory("person");
        assertEquals("person", form.getCategory());
    }

    /**
     * Tests query with empty string.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testEmptyQuery() {
        SearchForm form = new SearchForm();
        form.setQuery("");
        assertEquals("", form.getQuery());
    }

    /**
     * Tests category with empty string.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testEmptyCategory() {
        SearchForm form = new SearchForm();
        form.setCategory("");
        assertEquals("", form.getCategory());
    }

    /**
     * Tests query with null value.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testNullQuery() {
        SearchForm form = new SearchForm();
        form.setQuery(null);
        assertNull(form.getQuery());
    }

    /**
     * Tests category with null value.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testNullCategory() {
        SearchForm form = new SearchForm();
        form.setCategory(null);
        assertNull(form.getCategory());
    }

    /**
     * Tests that query and category are independent fields.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testQueryAndCategoryIndependence() {
        SearchForm form = new SearchForm();
        form.setQuery("Inception");
        form.setCategory("movie");
        assertEquals("Inception", form.getQuery());
        assertEquals("movie", form.getCategory());
    }

    /**
     * Tests direct public field access for query.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testDirectFieldAccessQuery() {
        SearchForm form = new SearchForm();
        form.query = "Interstellar";
        assertEquals("Interstellar", form.query);
        assertEquals("Interstellar", form.getQuery());
    }

    /**
     * Tests direct public field access for category.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testDirectFieldAccessCategory() {
        SearchForm form = new SearchForm();
        form.category = "tv";
        assertEquals("tv", form.category);
        assertEquals("tv", form.getCategory());
    }

    /**
     * Tests query with multiple search terms.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testMultiWordQuery() {
        SearchForm form = new SearchForm();
        form.setQuery("The Dark Knight");
        assertEquals("The Dark Knight", form.getQuery());
    }

    /**
     * Tests query with special characters.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testSpecialCharacterQuery() {
        SearchForm form = new SearchForm();
        form.setQuery("Spider-Man: No Way Home");
        assertEquals("Spider-Man: No Way Home", form.getQuery());
    }
}
