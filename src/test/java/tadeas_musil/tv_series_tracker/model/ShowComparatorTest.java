package tadeas_musil.tv_series_tracker.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import tadeas_musil.tv_series_tracker.model.comparator.ShowComparator;

public class ShowComparatorTest {

    private static ShowComparator showComparator;

    @BeforeClass
    public static void setUp() {
        showComparator = new ShowComparator();
    }

    @Test
    public void compare_shouldReturn1_givenDifferentImdbIds() {
        Show planetEarth = new Show();
        planetEarth.setTraktId("planetEarthId");
        planetEarth.setImdbId("11111");

        Show cosmos = new Show();
        cosmos.setTraktId("cosmosId");
        planetEarth.setImdbId("77777");

        int comparingResult = showComparator.compare(planetEarth, cosmos);

        assertThat(comparingResult).isEqualTo(1);
    }

    @Test
    public void compare_shouldReturn0_givenSameImdbIds() {
        Show planetEarth = new Show();
        planetEarth.setTraktId("planetEarthId");
        planetEarth.setImdbId("11111");

        Show cosmos = new Show();
        cosmos.setTraktId("cosmosId");
        cosmos.setImdbId("11111");

        int comparingResult = showComparator.compare(planetEarth, cosmos);

        assertThat(comparingResult).isEqualTo(0);
    }

    @Test
    public void compare_shouldReturn0_givenSameTraktIds() {
        Show planetEarth = new Show();
        planetEarth.setTraktId("planetEarthId");
        planetEarth.setImdbId("11111");

        Show cosmos = new Show();
        cosmos.setTraktId("planetEarthId");
        cosmos.setImdbId("77777");

        int comparingResult = showComparator.compare(planetEarth, cosmos);

        assertThat(comparingResult).isEqualTo(0);
    }

    @Test
    public void compare_shouldReturn1_givenFirstImdbIdIsNull() {
        Show planetEarth = new Show();
        planetEarth.setTraktId("planetEarthId");

        Show cosmos = new Show();
        cosmos.setTraktId("cosmos");
        cosmos.setImdbId("77777");

        int comparingResult = showComparator.compare(planetEarth, cosmos);

        assertThat(comparingResult).isEqualTo(1);
    }

    @Test
    public void compare_shouldReturn1_givenBothImdbIdsAreNull() {
        Show planetEarth = new Show();
        planetEarth.setTraktId("planetEarthId");

        Show cosmos = new Show();
        cosmos.setTraktId("cosmos");
        
        int comparingResult = showComparator.compare(planetEarth, cosmos);

        assertThat(comparingResult).isEqualTo(1);
    }
}
