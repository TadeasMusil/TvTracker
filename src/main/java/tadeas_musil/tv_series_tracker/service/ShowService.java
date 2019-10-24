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
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.util.DateUtils;

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
    
    @Autowired
    private ShowRepository showRepository;

    public SearchResult searchShows(String query, Integer page) {
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
        String date = DateUtils.getCurrentDate().toString();
            
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

    public List<Show> getPremieringShows(){
        String date = DateUtils.getCurrentDate().toString();
            
        List<Show> shows = webClient
        .get()
        .uri(env.getProperty("trakt_tv.api.uri.search_premiering_shows"),date)
        .header("trakt-api-version", "${trakt_tv.api.version}")
        .header("trakt-api-key", traktApiKey)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToFlux(Episode.class)
        .map(episode -> episode.getShow())
        .filter(show -> show.getImdbId() != null)
        .collectList()
        .block();

        return shows;
    }

    public String getImageUrl(String tvdbId) {
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

    public void addImages(List<Show> shows) {
        for (Show show : shows) {
            String tvdbId = show.getTvdbId();
            if (tvdbId != null) {
                String url = getImageUrl(tvdbId);
                show.setImageUrl(url);
            }
        }
    }

    public void saveNewShow(Show show) {
        if(!showRepository.existsById(show.getTraktId())){
            showRepository.save(show);
        }
    }

}