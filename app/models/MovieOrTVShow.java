package models;

/**
 * Represents a movie or TV show from the TMDb API.
 * This model stores essential information about a media item including
 * its unique identifier, title, and aggregated user statistics.
 *
 * @author Syed Shahab Shah
 */
public class MovieOrTVShow {
    private String id;
    private String title;
    private double popularity;
    private double voteAverage;
    private int voteCount;
    private String year;

    /**
     * Constructs a MovieOrTVShow with basic information.
     * Used for simple use cases where the ID is not required.
     *
     * @param title The title or name of the movie/TV show
     * @param popularity The popularity score from TMDb
     * @param voteAverage The average vote rating from TMDb
     * @param voteCount The total number of votes from TMDb
     * @author Syed Shahab Shah
     */
    public MovieOrTVShow(String title, double popularity, double voteAverage, int voteCount) {
        this.title = title;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.year = "";
    }

    /**
     * Constructs a MovieOrTVShow with complete information including ID.
     * Used primarily by the TMDbService when retrieving data from the API.
     *
     * @param id The unique identifier from TMDb
     * @param title The title or name of the movie/TV show
     * @param popularity The popularity score from TMDb
     * @param voteAverage The average vote rating from TMDb
     * @param voteCount The total number of votes from TMDb
     * @author Syed Shahab Shah
     */
    public MovieOrTVShow(String id, String title, double popularity, double voteAverage, int voteCount) {
        this.id = id;
        this.title = title;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.year = "";
    }

    /**
     * Constructs a MovieOrTVShow with complete information including ID and year.
     *
     * @param id The unique identifier from TMDb
     * @param title The title or name of the movie/TV show
     * @param popularity The popularity score from TMDb
     * @param voteAverage The average vote rating from TMDb
     * @param voteCount The total number of votes from TMDb
     * @param year The release year of the movie/TV show
     * @author Syed Shahab Shah
     */
    public MovieOrTVShow(String id, String title, double popularity, double voteAverage, int voteCount, String year) {
        this.id = id;
        this.title = title;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.year = (year != null) ? year : "";
    }

    /**
     * Gets the unique identifier of this media item.
     *
     * @return The TMDb ID
     * @author Syed Shahab Shah
     */
    public String getId() { return id; }

    /**
     * Gets the title or name of this media item.
     *
     * @return The title/name
     * @author Syed Shahab Shah
     */
    public String getTitle() { return title; }

    /**
     * Gets the popularity score of this media item.
     * Higher values indicate greater popularity on TMDb.
     *
     * @return The popularity score as a double
     * @author Syed Shahab Shah
     */
    public double getPopularity() { return popularity; }

    /**
     * Gets the average vote rating of this media item.
     * Typically ranges from 0 to 10.
     *
     * @return The average vote rating
     * @author Syed Shahab Shah
     */
    public double getVoteAverage() { return voteAverage; }

    /**
     * Gets the total number of votes for this media item.
     * Indicates the sample size for the vote average.
     *
     * @return The number of votes
     * @author Syed Shahab Shah
     */
    public int getVoteCount() { return voteCount; }

    /**
     * Gets the release year of this media item.
     *
     * @return The release year, or empty string if unknown
     * @author Syed Shahab Shah
     */
    public String getYear() { return year; }
}