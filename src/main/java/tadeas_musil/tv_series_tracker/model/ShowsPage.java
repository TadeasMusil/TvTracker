package tadeas_musil.tv_series_tracker.model;

import java.util.List;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ShowsPage {

  private List<Show> shows;

  private int totalNumberOfPages;

  

}