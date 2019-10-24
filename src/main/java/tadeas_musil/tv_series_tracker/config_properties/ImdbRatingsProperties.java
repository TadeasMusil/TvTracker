package tadeas_musil.tv_series_tracker.config_properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "imdb.ratings")
public class ImdbRatingsProperties {

    private String url;

    private String decompressionLocation;

    private String downloadLocation;

    private int requiredVotes;

    private float requiredRating;

    private int maxAge;

}