package tadeas_musil.tv_series_tracker.service;

import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Image;
import tadeas_musil.tv_series_tracker.model.ImageWrapper;
import tadeas_musil.tv_series_tracker.model.SearchResult;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowWrapper;

@Service
public class ShowService {
    
    @Value("${trakt_tv.api.key}")
    private String traktApiKey;
    
    @Value("${trakt_tv.api.version}")
    private String traktApiVersion;
    
    @Autowired
    private Environment env;
    
    @Autowired
    private WebClient webClient;
    
    @Value("${app.timezone}")
    private String timezone;

    public SearchResult searchShows(String query, Integer page) throws Exception {
        SearchResult searchResult = new SearchResult();
        List<Show> shows = webClient
                .get()
                .uri(env.getProperty("trakt_tv.api.uri.search_shows_by_query"),query,page)
                .header("trakt-api-version", "2")
                .header("trakt-api-key", traktApiKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .doOnSuccess(response -> searchResult.setTotalNumberOfPages(
                                                            Integer.parseInt(response
                                                                                    .headers()
                                                                                    .header("X-Pagination-Page-Count")
                                                                                    .get(0))))
                .flatMap(response -> response
                    .bodyToFlux(ShowWrapper.class)
                    .map(showWrapper -> showWrapper.getShow())
                    .collectList())
                .block();

         addImages(shows);
         searchResult.setShows(shows);
         return searchResult;
    }

    public Show findShow(String traktId){
        Show show = webClient
        .get()
        .uri(env.getProperty("trakt_tv.api.uri.search_show_by_id"),traktId)
        .header("trakt-api-version",traktApiVersion)
        .header("trakt-api-key", traktApiKey)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(ShowWrapper[].class)
        .map(wrapper -> wrapper[0].getShow())
        .block();
        
        return show;
    }

    public List<Episode> getAiringEpisodes(){
        String date = java.time.LocalDate.now(ZoneId.of(timezone)).toString();
            
        List<Episode> episodes = webClient
        .get()
        .uri(env.getProperty("trakt_tv.api.uri.search_get_schedule"),date)
        .header("trakt-api-version", "${trakt_tv.api.version}")
        .header("trakt-api-key", traktApiKey)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToFlux(Episode.class)
        .collectList()
        .block();

        return episodes;
    }

    public String getImageUrl(String tvdbId) throws Exception {
        ImageWrapper wrapper = webClient
                .get()
                .uri(env.getProperty("tvmaze.api.uri.search_show_by_tvdbid"), tvdbId)
                .retrieve()
                .bodyToMono(ImageWrapper.class)
                .onErrorReturn(new ImageWrapper(new Image("")))
                .block();
                
      if(wrapper.getImage() != null ){
            return wrapper.getImage().getUrl();
      }
        return "";
    }

    public void addImages(List<Show> shows) throws Exception {
        for (Show show : shows) {
            String tvdbId = show.getTvdbId();
            if (tvdbId != null) {
                String url = getImageUrl(tvdbId);
                show.setImageUrl(url);
            }
        }
    }

}