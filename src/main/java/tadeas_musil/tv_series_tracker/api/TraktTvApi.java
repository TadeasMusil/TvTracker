package tadeas_musil.tv_series_tracker.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowWrapper;
import tadeas_musil.tv_series_tracker.model.ShowsPage;
import tadeas_musil.tv_series_tracker.util.DateUtils;

@Component
public class TraktTvApi {

    @Value("${trakt_tv.api.key}")
    private String traktApiKey;

    @Value("${trakt_tv.api.version}")
    private String traktApiVersion;

    @Value("${app.shows_per_page}")
    private int showsPerPage;

    @Autowired
    private Environment env;

    @Autowired
    private WebClient webClient;

    public ShowsPage searchShowsByQuery(String query, Integer page) {
        ShowsPage pageOfShows = new ShowsPage();

        List<Show> foundShows = webClient
                .get()
                .uri(env.getProperty("trakt_tv.api.uri.search_shows_by_query"), query, page)
                .header("trakt-api-version", traktApiVersion)
                .header("trakt-api-key", traktApiKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .doOnSuccess(response -> pageOfShows.setTotalNumberOfPages(Integer.valueOf(response
                                                                                                .headers()
                                                                                                .header("X-Pagination-Page-Count")
                                                                                                .get(0))))
                .flatMap(response -> response
                        .bodyToFlux(ShowWrapper.class)
                        .map(showWrapper -> showWrapper.getShow())
                        .collectList())
                .block();

        pageOfShows.setShows(foundShows);
        return pageOfShows;
    }

    public Show findShowById(String traktId) {
        Show show = webClient
                .get()
                .uri(env.getProperty("trakt_tv.api.uri.search_show_by_id"), traktId)
                .header("trakt-api-version", traktApiVersion)
                .header("trakt-api-key", traktApiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ShowWrapper[].class)
                .map(wrapper -> wrapper[0].getShow())
                .block();

        return show;
    }

    public List<Episode> getAiringEpisodes() {
        String date = DateUtils.getCurrentDate().toString();

        List<Episode> episodes = webClient
                .get()
                .uri(env.getProperty("trakt_tv.api.uri.search_get_schedule"), date)
                .header("trakt-api-version", traktApiVersion)
                .header("trakt-api-key", traktApiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Episode.class)
                .collectList()
                .block();

        return episodes;
    }

    public List<Show> getPremieringShows() {
        LocalDate date = DateUtils.getCurrentDate();

        List<Show> shows = webClient
                .get()
                .uri(env.getProperty("trakt_tv.api.uri.search_premiering_shows"), date)
                .header("trakt-api-version", traktApiVersion)
                .header("trakt-api-key", traktApiKey)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Episode.class)
                .map(episode -> episode.getShow())
                .collectList()
                .block();

        return shows;
    }
}