package actors;

import models.MovieOrTVShow;
import models.PersonStats;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;

import java.util.ArrayList;
import java.util.List;

/**
 * Actor that handles reactive updates and reads for PersonStats.
 *
 * @author Syed Shahab Shah
 */
public class PersonStatsActor extends AbstractActor {

    private final PersonStats personStats;

    /**
     * Command to refresh known-for items and recompute statistics.
     *
     * @author Syed Shahab Shah
     */
    public static final class SetItems {
        public final List<MovieOrTVShow> items;

        public SetItems(List<MovieOrTVShow> items) {
            this.items = items;
        }
    }

    /**
     * Command to update person profile details.
     *
     * @author Syed Shahab Shah
     */
    public static final class SetPersonDetails {
        public final String name;
        public final String profilePath;
        public final String knownForDepartment;
        public final int genderCode;
        public final String birthday;
        public final String placeOfBirth;

        public SetPersonDetails(String name, String profilePath, String knownForDepartment,
                                int genderCode, String birthday, String placeOfBirth) {
            this.name = name;
            this.profilePath = profilePath;
            this.knownForDepartment = knownForDepartment;
            this.genderCode = genderCode;
            this.birthday = birthday;
            this.placeOfBirth = placeOfBirth;
        }
    }

    /**
     * Command to request a read-only snapshot of current values.
     *
     * @author Syed Shahab Shah
     */
    public static final class GetSnapshot {
    }

    /**
     * Command to compute a complete PersonStats model in one message.
     *
     * @author Syed Shahab Shah
     */
    public static final class ComputePersonStats {
        public final List<MovieOrTVShow> items;
        public final String name;
        public final String profilePath;
        public final String knownForDepartment;
        public final int genderCode;
        public final String birthday;
        public final String placeOfBirth;

        public ComputePersonStats(List<MovieOrTVShow> items,
                                  String name,
                                  String profilePath,
                                  String knownForDepartment,
                                  int genderCode,
                                  String birthday,
                                  String placeOfBirth) {
            this.items = items;
            this.name = name;
            this.profilePath = profilePath;
            this.knownForDepartment = knownForDepartment;
            this.genderCode = genderCode;
            this.birthday = birthday;
            this.placeOfBirth = placeOfBirth;
        }
    }

    /**
     * Immutable snapshot payload returned by GetSnapshot.
     *
     * @author Syed Shahab Shah
     */
    public static final class PersonStatsSnapshot {
        public final List<MovieOrTVShow> latestItems;
        public final double popAvg;
        public final double popMin;
        public final double popMax;
        public final double voteAvg;
        public final double voteMin;
        public final double voteMax;
        public final double countAvg;
        public final int countMin;
        public final int countMax;
        public final String personName;
        public final String profilePhotoUrl;
        public final String knownFor;
        public final String gender;
        public final String birthday;
        public final int age;
        public final String placeOfBirth;

        public PersonStatsSnapshot(List<MovieOrTVShow> latestItems,
                                   double popAvg,
                                   double popMin,
                                   double popMax,
                                   double voteAvg,
                                   double voteMin,
                                   double voteMax,
                                   double countAvg,
                                   int countMin,
                                   int countMax,
                                   String personName,
                                   String profilePhotoUrl,
                                   String knownFor,
                                   String gender,
                                   String birthday,
                                   int age,
                                   String placeOfBirth) {
            this.latestItems = latestItems;
            this.popAvg = popAvg;
            this.popMin = popMin;
            this.popMax = popMax;
            this.voteAvg = voteAvg;
            this.voteMin = voteMin;
            this.voteMax = voteMax;
            this.countAvg = countAvg;
            this.countMin = countMin;
            this.countMax = countMax;
            this.personName = personName;
            this.profilePhotoUrl = profilePhotoUrl;
            this.knownFor = knownFor;
            this.gender = gender;
            this.birthday = birthday;
            this.age = age;
            this.placeOfBirth = placeOfBirth;
        }
    }

    /**
     * Creates props for PersonStatsActor.
     *
     * @param initialItems initial known-for items
     * @return props for actor creation
     * @author Syed Shahab Shah
     */
    public static Props props(List<MovieOrTVShow> initialItems) {
        return Props.create(PersonStatsActor.class, () -> new PersonStatsActor(initialItems));
    }

    /**
     * Constructor.
     *
     * @param initialItems initial known-for items
     * @author Syed Shahab Shah
     */
    public PersonStatsActor(List<MovieOrTVShow> initialItems) {
        this.personStats = new PersonStats(initialItems);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SetItems.class, this::onSetItems)
                .match(SetPersonDetails.class, this::onSetPersonDetails)
                .match(ComputePersonStats.class, this::onComputePersonStats)
                .match(GetSnapshot.class, ignored -> getSender().tell(createSnapshot(), getSelf()))
                .build();
    }

    private void onSetItems(SetItems message) {
        personStats.setItems(message.items);
    }

    private void onSetPersonDetails(SetPersonDetails message) {
        personStats.setPersonDetails(
                message.name,
                message.profilePath,
                message.knownForDepartment,
                message.genderCode,
                message.birthday,
                message.placeOfBirth
        );
    }

    private void onComputePersonStats(ComputePersonStats message) {
        PersonStats stats = new PersonStats(message.items);
        stats.setPersonDetails(
                message.name,
                message.profilePath,
                message.knownForDepartment,
                message.genderCode,
                message.birthday,
                message.placeOfBirth
        );
        getSender().tell(stats, getSelf());
    }

    private PersonStatsSnapshot createSnapshot() {
        return new PersonStatsSnapshot(
                new ArrayList<>(personStats.getLatestItems()),
                personStats.getPopAvg(),
                personStats.getPopMin(),
                personStats.getPopMax(),
                personStats.getVoteAvg(),
                personStats.getVoteMin(),
                personStats.getVoteMax(),
                personStats.getCountAvg(),
                personStats.getCountMin(),
                personStats.getCountMax(),
                personStats.getPersonName(),
                personStats.getProfilePhotoUrl(),
                personStats.getKnownFor(),
                personStats.getGender(),
                personStats.getBirthday(),
                personStats.getAge(),
                personStats.getPlaceOfBirth()
        );
    }
}



