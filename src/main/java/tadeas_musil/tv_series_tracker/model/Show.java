package tadeas_musil.tv_series_tracker.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Show {
    @Id
    private String traktId;

    private String imdbId;

    private String tvdbId;

    private String title;

    private String imageUrl;

    private int year;

    @Getter(value = AccessLevel.NONE)
    @Column(name = "rating_check")
    private boolean shouldGetRatingChecked = false;

    private boolean isRecommended = false;

    private LocalDate releaseDate;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "followedShows")
    private Set<User> users = new HashSet<>();

    @JsonProperty("ids")
    private void unpackIds(Map<String, String> ids) {
        this.imdbId = ids.get("imdb");
        this.tvdbId = ids.get("tvdb");
        this.traktId = ids.get("trakt");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((traktId == null) ? 0 : traktId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Show other = (Show) obj;
        if (traktId == null) {
            if (other.traktId != null)
                return false;
        } else if (!traktId.equals(other.traktId))
            return false;
        return true;
    }

    public boolean shouldGetRatingChecked() {
        return this.shouldGetRatingChecked;
    }

}