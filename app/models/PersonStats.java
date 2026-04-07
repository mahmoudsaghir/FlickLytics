package models;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates and provides statistical analysis of a person's "known for" media items.
 * This class retrieves up to 50 of the latest items a person is known for from TMDb,
 * then calculates comprehensive statistics including popularity, vote average, and vote count
 * using Java 8+ Streams API.
 *
 * @author Syed Shahab Shah
 */
public class PersonStats {
    private List<MovieOrTVShow> latestItems;
    private DoubleSummaryStatistics popularityStats;
    private DoubleSummaryStatistics voteAverageStats;
    private IntSummaryStatistics voteCountStats;

    // Person profile details
    private String personName;
    private String profilePhotoUrl;
    private String knownFor;
    private String gender;
    private String birthday;
    private int age;
    private String placeOfBirth;

    /**
     * Constructs a PersonStats object from a list of all available items.
     * Automatically limits the items to the 50 most recent and calculates statistics.
     * Handles null input gracefully by using an empty list.
     *
     * @param allItems The list of all items a person is known for (can be null)
     * @author Syed Shahab Shah
     */
    public PersonStats(List<MovieOrTVShow> allItems) {
        setItems(allItems);
    }

    /**
     * Replaces known-for items and recomputes summary statistics.
     *
     * @param allItems latest TMDb known-for items
     * @author Syed Shahab Shah
     */
    public void setItems(List<MovieOrTVShow> allItems) {
        if (allItems == null) {
            this.latestItems = java.util.Collections.emptyList();
        } else {
            this.latestItems = allItems.stream()
                    .sorted(Comparator.comparing(MovieOrTVShow::getYear, Comparator.reverseOrder()))
                    .limit(50)
                    .collect(Collectors.toList());
        }
        calculateStatistics();
    }

    /**
     * Calculates summary statistics for popularity, vote average, and vote count
     * using Java 8+ Streams API's summaryStatistics() method.
     * This method is called during construction and handles empty lists gracefully.
     *
     * @author Syed Shahab Shah
     */
    private void calculateStatistics() {
        // If the list is empty, summaryStatistics() will return 0s naturally
        this.popularityStats = latestItems.stream()
                .mapToDouble(MovieOrTVShow::getPopularity)
                .summaryStatistics();

        this.voteAverageStats = latestItems.stream()
                .mapToDouble(MovieOrTVShow::getVoteAverage)
                .summaryStatistics();

        this.voteCountStats = latestItems.stream()
                .mapToInt(MovieOrTVShow::getVoteCount)
                .summaryStatistics();
    }

    /**
     * Gets the list of up to 50 latest items this person is known for.
     *
     * @return An unmodifiable list of MovieOrTVShow items
     * @author Syed Shahab Shah
     */
    public List<MovieOrTVShow> getLatestItems() {
        return latestItems;
    }

    /**
     * Gets the average popularity score across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The average popularity score
     * @author Syed Shahab Shah
     */
    public double getPopAvg() {
        return popularityStats.getAverage();
    }

    /**
     * Gets the minimum popularity score across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The minimum popularity score
     * @author Syed Shahab Shah
     */
    public double getPopMin() {
        return latestItems.isEmpty() ? 0.0 : popularityStats.getMin();
    }

    /**
     * Gets the maximum popularity score across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The maximum popularity score
     * @author Syed Shahab Shah
     */
    public double getPopMax() {
        return latestItems.isEmpty() ? 0.0 : popularityStats.getMax();
    }

    /**
     * Gets the average vote average rating across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The average of vote average ratings
     * @author Syed Shahab Shah
     */
    public double getVoteAvg() {
        return voteAverageStats.getAverage();
    }

    /**
     * Gets the minimum vote average rating across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The minimum vote average rating
     * @author Syed Shahab Shah
     */
    public double getVoteMin() {
        return latestItems.isEmpty() ? 0.0 : voteAverageStats.getMin();
    }

    /**
     * Gets the maximum vote average rating across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The maximum vote average rating
     * @author Syed Shahab Shah
     */
    public double getVoteMax() {
        return latestItems.isEmpty() ? 0.0 : voteAverageStats.getMax();
    }

    /**
     * Gets the average vote count across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The average number of votes as a double
     * @author Syed Shahab Shah
     */
    public double getCountAvg() {
        return voteCountStats.getAverage();
    }

    /**
     * Gets the minimum vote count across all items.
     * Returns 0 for an empty list.
     *
     * @return The minimum vote count
     * @author Syed Shahab Shah
     */
    public int getCountMin() {
        return latestItems.isEmpty() ? 0 : voteCountStats.getMin();
    }

    /**
     * Gets the maximum vote count across all items.
     * Returns 0 for an empty list.
     *
     * @return The maximum vote count
     * @author Syed Shahab Shah
     */
    public int getCountMax() {
        return latestItems.isEmpty() ? 0 : voteCountStats.getMax();
    }

    /**
     * Sets the person's profile details from TMDb person API response.
     *
     * @param name The person's full name
     * @param profilePath The profile photo path from TMDb (e.g. "/abc.jpg")
     * @param knownForDepartment The department the person is known for (e.g. "Acting")
     * @param genderCode The gender code from TMDb (1=Female, 2=Male, 0=Not specified)
     * @param birthday The birthday string in "YYYY-MM-DD" format
     * @param placeOfBirth The place of birth
     * @author Syed Shahab Shah
     */
    public void setPersonDetails(String name, String profilePath, String knownForDepartment,
                                 int genderCode, String birthday, String placeOfBirth) {
        this.personName = (name != null) ? name : "Unknown";
        this.profilePhotoUrl = (profilePath != null && !profilePath.isEmpty())
                ? "https://image.tmdb.org/t/p/w300" + profilePath : "";
        this.knownFor = (knownForDepartment != null) ? knownForDepartment : "N/A";
        this.gender = switch (genderCode) {
            case 1 -> "Female";
            case 2 -> "Male";
            default -> "Not specified";
        };
        this.birthday = (birthday != null && !birthday.isEmpty()) ? birthday : "N/A";
        this.placeOfBirth = (placeOfBirth != null && !placeOfBirth.isEmpty()) ? placeOfBirth : "N/A";

        // Calculate age from birthday
        if (birthday != null && birthday.length() >= 10) {
            try {
                java.time.LocalDate birthDate = java.time.LocalDate.parse(birthday);
                this.age = java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
            } catch (Exception e) {
                this.age = -1;
            }
        } else {
            this.age = -1;
        }
    }

    /** @return The person's full name @author Syed Shahab Shah */
    public String getPersonName() { return personName; }

    /** @return The full URL to the person's profile photo @author Syed Shahab Shah */
    public String getProfilePhotoUrl() { return profilePhotoUrl; }

    /** @return The department the person is known for @author Syed Shahab Shah */
    public String getKnownFor() { return knownFor; }

    /** @return The person's gender as a string @author Syed Shahab Shah */
    public String getGender() { return gender; }

    /** @return The person's birthday in YYYY-MM-DD format @author Syed Shahab Shah */
    public String getBirthday() { return birthday; }

    /** @return The person's age, or -1 if unknown @author Syed Shahab Shah */
    public int getAge() { return age; }

    /** @return The person's place of birth @author Syed Shahab Shah */
    public String getPlaceOfBirth() { return placeOfBirth; }
}