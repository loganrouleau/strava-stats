import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnitConverterTest {

    @Test
    public void convertMetersPerSecondToMinutesPerKm() {
        assertEquals("4:00", UnitConverter.speedToPace(4.1666666));
        assertEquals("4:10", UnitConverter.speedToPace(4.000));
        assertEquals("4:16", UnitConverter.speedToPace(3.9));
        assertEquals("4:46", UnitConverter.speedToPace(3.5));
        assertEquals("8:33", UnitConverter.speedToPace(1.95));
    }

    @Test
    public void convertSecondsToHoursMinutesSeconds() {
        assertEquals("2:45:45", UnitConverter.durationToHoursMinutesSeconds(9945));
        assertEquals("0:59:59", UnitConverter.durationToHoursMinutesSeconds(3599));
        assertEquals("0:35:54", UnitConverter.durationToHoursMinutesSeconds(2154));
        assertEquals("0:02:00", UnitConverter.durationToHoursMinutesSeconds(120));
        assertEquals("0:00:59", UnitConverter.durationToHoursMinutesSeconds(59));
    }

}
