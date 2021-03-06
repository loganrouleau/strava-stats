import com.strava.api.v3.ApiClient;
import com.strava.api.v3.ApiException;
import com.strava.api.v3.Configuration;
import com.strava.api.v3.api.ActivitiesApi;
import com.strava.api.v3.auth.OAuth;
import com.strava.api.v3.model.ActivityType;
import com.strava.api.v3.model.SummaryActivity;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    private static final String ACCESS_TOKEN = System.getenv("ACCESS_TOKEN");
    private static final int PAGE_SIZE = 200; // max results Strava returns for a single request
    private static final String OUTPUT_FILE_NAME = "strava-data.csv";
    private static final String TIME_ZONE = "America/Los_Angeles";

    public ActivitiesApi apiInstance = new ActivitiesApi();
    private final int beforeTime;
    private final int afterTime;

    public App(int beforeTime, int afterTime) {
        this.beforeTime = beforeTime;
        this.afterTime = afterTime;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }

        try {
            int beforeTime = (int) LocalDate.parse(args[1]).plusDays(1).atStartOfDay(ZoneId.of(TIME_ZONE)).toEpochSecond();
            int afterTime = (int) LocalDate.parse(args[0]).atStartOfDay(ZoneId.of(TIME_ZONE)).toEpochSecond();
            new App(beforeTime, afterTime).run();
        } catch (DateTimeParseException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            printUsage();
            System.exit(1);
        }

    }

    private static void printUsage() {
        System.out.println("Usage: java -jar *.jar startDate endDate");
        System.out.println("startDate: Query for activities starting at the beginning of this day (format \"yyyy-MM-dd\")");
        System.out.println("endDate: Query for activities up until the end of this day (format \"yyyy-MM-dd\")");
    }

    private void run() throws IOException {
        authenticate();
        List<SummaryActivity> activities = getActivities(beforeTime, afterTime)
                .stream()
                .filter(a -> a.getType().equals(ActivityType.RUN))
                .collect(Collectors.toList());
        CsvWriter.write(activities, OUTPUT_FILE_NAME);
    }

    private void authenticate() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        OAuth stravaOauth = (OAuth) defaultClient.getAuthentication("strava_oauth");
        stravaOauth.setAccessToken(ACCESS_TOKEN);
    }

    /**
     * Get a list of activities ordered from oldest to most recent
     */
    public List<SummaryActivity> getActivities(int beforeTime, int afterTime) {
        LinkedList<SummaryActivity> result = new LinkedList<>();
        try {
            System.out.println("\nFetching activities from Strava...\n");
            List<SummaryActivity> activities = apiInstance.getLoggedInAthleteActivities(beforeTime, afterTime, 1, PAGE_SIZE);
            for (SummaryActivity activity : activities) {
                result.addFirst(activity);
                System.out.println(activity.getStartDateLocal() + " - " + activity.getName());
            }
            if (result.size() > 0) { // recurse if we need more results
                int newBeforeTime = (int) result.getFirst().getStartDateLocal().toEpochSecond();
                LinkedList<SummaryActivity> newResult = new LinkedList<>(getActivities(newBeforeTime, afterTime));
                newResult.addAll(result);
                result = newResult;
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return result;
    }

}
