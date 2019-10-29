package tadeas_musil.tv_series_tracker.model.comparator;

import java.util.Comparator;

import tadeas_musil.tv_series_tracker.model.Show;

public class ShowComparator implements Comparator<Show>{

	@Override
	public int compare(Show first, Show second) {
        if ( first.getTraktId().equals(second.getTraktId()) || 
            (first.getImdbId() != null && first.getImdbId().equals(second.getImdbId()))) {
                return 0;
            }
            return 1;
	}
  

}