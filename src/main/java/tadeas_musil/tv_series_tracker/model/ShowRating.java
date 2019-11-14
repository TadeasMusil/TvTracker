package tadeas_musil.tv_series_tracker.model;

import com.univocity.parsers.annotations.Parsed;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShowRating implements Comparable<ShowRating> {

    @Parsed(field = "tconst")
    private String imdbId;

    @Parsed(field = "averageRating")
    private double rating;

    @Parsed(field = "numVotes")
    private int numberOfVotes;

    public ShowRating(String id) {
        this.imdbId = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((imdbId == null) ? 0 : imdbId.hashCode());
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
        ShowRating other = (ShowRating) obj;
        if (imdbId == null) {
            if (other.imdbId != null)
                return false;
        } else if (!imdbId.equals(other.imdbId))
            return false;
        return true;
    }

    // Every imdb id starts with 'tt'
    @Override
    public int compareTo(ShowRating movie) {
        return Integer.parseInt(this.imdbId.substring(2)) - Integer.parseInt(movie.getImdbId().substring(2));

    }

    @Override
    public String toString() {
        return "ImdbMovieInfo [id=" + imdbId + ", numberOfVotes=" + numberOfVotes + ", rating=" + rating + "]";
    }

}