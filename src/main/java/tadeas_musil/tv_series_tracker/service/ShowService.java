package tadeas_musil.tv_series_tracker.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.api.TraktTvApi;
import tadeas_musil.tv_series_tracker.api.TvMazeApi;
import tadeas_musil.tv_series_tracker.model.Episode;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.ShowsPage;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.util.DateUtils;

@Service
public class ShowService {
    
    @Value("${app.shows_per_page}")
    private int showsPerPage;
    
    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private TraktTvApi traktTvApi;

    @Autowired
    private TvMazeApi tvMazeApi;

    public ShowsPage searchShows(String query, Integer page) {
        ShowsPage shows = traktTvApi.searchShows(query, page);
        addImages(shows.getShows());

        return shows;
    }

    public Show findShow(String traktId){
        return traktTvApi.findShow(traktId);
    }

    public List<Episode> getAiringEpisodes(){
        return traktTvApi.getAiringEpisodes();
    }

    public List<Show> getPremieringShows(){
        List<Show> premieringShows = traktTvApi.getPremieringShows();
        premieringShows.removeIf(show -> show.getImdbId() == null || !show.getImdbId().startsWith("tt")); 
        premieringShows.forEach(show -> show.setReleaseDate(DateUtils.getCurrentDate()));
        
        return premieringShows;
    }

    public String getImageUrl(String tvdbId) {
        return tvMazeApi.getImage(tvdbId);
              
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
    
    // Adding missing images here to preserve memory, because heroku 
    // quotas were getting exceeded during recommended shows notification
    public Page<Show> getRecommendedShows(int pageNumber){
        List<Show> showsWithoutImage = showRepository.findByIsRecommendedAndImageUrl(true, null);
        showsWithoutImage.forEach(show -> showRepository.setImageUrl(getImageUrl(show.getTvdbId()), show.getTraktId()));
        
        Pageable pageable = PageRequest.of(pageNumber, showsPerPage);
        Page<Show> page = showRepository.findByIsRecommendedOrderByReleaseDateDesc(true, pageable);
        return page;
    }

    public void setReleaseDateForExistingShow(LocalDate date, String showId){
        if(showRepository.existsById(showId)){
            showRepository.setReleaseDate(date, showId);
        }
    }

}