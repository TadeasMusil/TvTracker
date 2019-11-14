package tadeas_musil.tv_series_tracker.service;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.config_properties.ImdbRatingsProperties;
import tadeas_musil.tv_series_tracker.model.ShowRating;

@Service
public class ShowRatingService {

    @Autowired
    private ImdbRatingsProperties imdbProperties;

    @Autowired
    private FileService fileService;

    public ShowRating getShowRating(List<ShowRating> ratings, String imdbId) {
        int index = Collections.binarySearch(ratings, new ShowRating(imdbId));
        if (index < 0) {
            return new ShowRating(imdbId, 0.0, 0);
        }
        return ratings.get(index);
    }

    public boolean meetsRequiredRatingAndVotes(ShowRating rating) {
        return meetsRequiredVotes(rating) && 
                rating.getRating() >= imdbProperties.getRequiredRating();
    }

    public boolean meetsRequiredVotes(ShowRating rating) {
        return rating.getNumberOfVotes() >= imdbProperties.getRequiredVotes();
    }

    public List<ShowRating> getRatings() {
        fileService.downloadFile(imdbProperties.getRatingsUrl(), imdbProperties.getDownloadLocation());
        fileService.decompressGzip(imdbProperties.getDownloadLocation(), imdbProperties.getDecompressionLocation());
        List<ShowRating> ratings = fileService.parseTsv(imdbProperties.getDecompressionLocation());
        deleteFiles(imdbProperties.getDownloadLocation(), imdbProperties.getDecompressionLocation());
        return ratings;
    }

    private void deleteFiles(String... locations) {
        for (String location : locations) {
            new File(location).delete();
        }
    }
}