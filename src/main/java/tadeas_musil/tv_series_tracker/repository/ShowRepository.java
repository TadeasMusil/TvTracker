package tadeas_musil.tv_series_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tadeas_musil.tv_series_tracker.model.Show;

@Repository
public interface ShowRepository extends JpaRepository<Show, String> {

}
