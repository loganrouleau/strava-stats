import com.strava.api.v3.model.SummaryActivity;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public final class CsvWriter {
    private static final char SEPARATOR = ',';

    public static void write(List<SummaryActivity> activities, String fileName) throws IOException {
        File csvFile = new File(fileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile))) {
            bw.write("url," +
                    "name," +
                    "distance (m)," +
                    "moving_time (s)," +
                    "elapsed_time (s)," +
                    "moving_time (h:mm:ss)," +
                    "elapsed_time (h:mm:ss)," +
                    "total_elevation_gain (m)," +
                    "type," +
                    "start_date_local," +
                    "start_time_local," +
                    "average_speed (m/s)" +
                    "average_pace (min/km)");
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
                        UnitConverter.durationToHoursMinutesSeconds(activity.getMovingTime()) + SEPARATOR +
                        UnitConverter.durationToHoursMinutesSeconds(activity.getElapsedTime()) + SEPARATOR +
                        activity.getTotalElevationGain() + SEPARATOR +
                        activity.getType() + SEPARATOR +
                        startDateLocal + SEPARATOR +
                        startTimeLocal + SEPARATOR +
                        UnitConverter.speedToPace(activity.getAverageSpeed()));
                bw.newLine();
            }
            System.out.println("\nResults written to file " + csvFile.getAbsolutePath());
        }
    }

    private CsvWriter() {
        throw new UnsupportedOperationException();
    }
}
