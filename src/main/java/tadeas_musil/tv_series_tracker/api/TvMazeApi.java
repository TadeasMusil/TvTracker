package tadeas_musil.tv_series_tracker.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import tadeas_musil.tv_series_tracker.model.Image;
import tadeas_musil.tv_series_tracker.model.ImageWrapper;

@Component
public class TvMazeApi {

    @Autowired
    private WebClient webClient;

    @Autowired
    private Environment env;

    public String getImageUrl(String tvdbId) {
        ImageWrapper wrapper = webClient
                .get()
                .uri(env.getProperty("tvmaze.api.uri.search_show_by_tvdbid"), tvdbId)
                .retrieve()
                .bodyToMono(ImageWrapper.class)
                .onErrorReturn(new ImageWrapper(new Image("")))
                .block();

        if (wrapper.getImage() != null) {
            return wrapper.getImage().getUrl();
        } else {
            return "";
        }
    }
}