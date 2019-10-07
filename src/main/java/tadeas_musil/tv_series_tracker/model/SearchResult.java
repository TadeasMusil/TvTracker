package tadeas_musil.tv_series_tracker.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {

  private List<Show> shows;

  private int totalNumberOfPages;

}