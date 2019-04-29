import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.strava.api.v3.api.ActivitiesApi;
import com.strava.api.v3.model.SummaryActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.threeten.bp.OffsetDateTime;

@RunWith(MockitoJUnitRunner.class)
public class AppTest {

    @Test
    public void resultsOrderedByStartDateIfMoreResultsThanPageSize() throws Exception {
        App app = new App(0, 0);
        app.apiInstance = mock(ActivitiesApi.class);

        SummaryActivity oldestActivity = new SummaryActivity();
        SummaryActivity middleActivity = new SummaryActivity();
        SummaryActivity newestActivity = new SummaryActivity();
        oldestActivity.setStartDateLocal(OffsetDateTime.now().minusDays(3));
        middleActivity.setStartDateLocal(OffsetDateTime.now().minusDays(2));
        newestActivity.setStartDateLocal(OffsetDateTime.now().minusDays(1));
        List<SummaryActivity> queryResult1 = new ArrayList<>();
        queryResult1.add(newestActivity); // Strava returns newest activities first
        queryResult1.add(middleActivity);
        List<SummaryActivity> queryResult2 = new ArrayList<>();
        queryResult2.add(oldestActivity);

        when(app.apiInstance.getLoggedInAthleteActivities(any(), any(), any(), any()))
                .thenReturn(queryResult1, queryResult2, Collections.emptyList());
        List<SummaryActivity> result = app.getActivities(0, 0);

        List<SummaryActivity> expectedResult = new ArrayList<>();
        expectedResult.add(oldestActivity);
        expectedResult.add(middleActivity);
        expectedResult.add(newestActivity);
        assertEquals(expectedResult, result);
    }
}
