public class UnitConverter {

    public static String speedToPace(double speed) {
        int secondsPerKm = (int) Math.round(1000 / speed);
        return (secondsPerKm / 60) + ":" + String.format("%02d", secondsPerKm % 60);
    }

    public static String durationToHoursMinutesSeconds(int seconds) {
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }

}
