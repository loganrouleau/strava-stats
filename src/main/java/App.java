import com.strava.api.v3.ApiClient;
import com.strava.api.v3.ApiException;
import com.strava.api.v3.Configuration;
import com.strava.api.v3.api.ActivitiesApi;
import com.strava.api.v3.auth.OAuth;
import com.strava.api.v3.model.SummaryActivity;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedList;
import java.util.List;

public class App {
    private static final String ACCESS_TOKEN = System.getenv("ACCESS_TOKEN");
    private static final int PAGE_SIZE = 200;
    private static final String FILE_NAME = "strava-data.csv";
    private static final String TIME_ZONE = "America/Los_Angeles";
    private static final char SEPARATOR = ',';
    private final ActivitiesApi apiInstance = new ActivitiesApi();
    private final int beforeTime;
    private final int afterTime;

    private App(int beforeTime, int afterTime) {
        this.beforeTime = beforeTime;
        this.afterTime = afterTime;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }
        int beforeTime = -1;
        int afterTime = -1;
        try {
            beforeTime = (int) LocalDate.parse(args[1]).plusDays(1).atStartOfDay(ZoneId.of(TIME_ZONE)).toEpochSecond();
            afterTime = (int) LocalDate.parse(args[0]).atStartOfDay(ZoneId.of(TIME_ZONE)).toEpochSecond();
        } catch (DateTimeParseException e) {
            System.out.println("Error: " + e.getMessage());
            printUsage();
            System.exit(1);
        }

        new App(beforeTime, afterTime).run();
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar *.jar startDate endDate");
        System.out.println("startDate: Query for activities starting at the beginning of this day (format \"yyyy-MM-dd\")");
        System.out.println("endDate: Query for activities up until the end of this day (format \"yyyy-MM-dd\")");
    }

    private void run() {
        authenticate();

        List<SummaryActivity> activities = getActivities(beforeTime, afterTime);
        writeToCsvFile(activities);
    }

    private void authenticate() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        OAuth stravaOauth = (OAuth) defaultClient.getAuthentication("strava_oauth");
        stravaOauth.setAccessToken(ACCESS_TOKEN);
    }

    private List<SummaryActivity> getActivities(int beforeTime, int afterTime) {
        LinkedList<SummaryActivity> recentActivities = new LinkedList<>();
        try {
            System.out.println("\nFetching activities from Strava...\n");
            List<SummaryActivity> activities = apiInstance.getLoggedInAthleteActivities(beforeTime, afterTime, 1, PAGE_SIZE);
            for (SummaryActivity activity : activities) {
                recentActivities.add(activity);
                System.out.println(activity.getStartDateLocal() + " - " + activity.getName());
            }
            if (recentActivities.size() == PAGE_SIZE) {
                int newBeforeTime = (int) recentActivities.getLast().getStartDateLocal().toEpochSecond();
                recentActivities.addAll(getActivities(newBeforeTime, afterTime));
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return recentActivities;
    }

    private void writeToCsvFile(List<SummaryActivity> activities) {
        File csvFile = new File(FILE_NAME);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            bw.write("url," +
                    "name," +
                    "distance (m)," +
                    "movingTime (s)," +
                    "elapsedTime (s)," +
                    "totalElevationGain (m)," +
                    "type," +
                    "startDateLocal," +
                    "startTimeLocal," +
                    "averageSpeed (m/s)");
            bw.newLine();

            for (SummaryActivity activity : activities) {
                String url = "https://www.strava.com/activities/" + activity.getId();
                String name = activity.getName();
                if (name.contains(String.valueOf(SEPARATOR))) {
                    name = '"' + name + '"';
                }
                String fullDateString = activity.getStartDateLocal().toString();
                String startDateLocal = fullDateString.substring(0, fullDateString.indexOf('T'));
                String startTimeLocal = fullDateString.substring(fullDateString.indexOf('T') + 1, fullDateString.indexOf('Z'));

                bw.write(url + SEPARATOR +
                        name + SEPARATOR +
                        activity.getDistance() + SEPARATOR +
                        activity.getMovingTime() + SEPARATOR +
                        activity.getElapsedTime() + SEPARATOR +
                        activity.getTotalElevationGain() + SEPARATOR +
                        activity.getType() + SEPARATOR +
                        startDateLocal + SEPARATOR +
                        startTimeLocal + SEPARATOR +
                        activity.getAverageSpeed());
                bw.newLine();
            }
            System.out.println("\nResults written to file " + csvFile.getAbsolutePath());

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

}
