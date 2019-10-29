package tadeas_musil.tv_series_tracker.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tadeas_musil.tv_series_tracker.model.Show;

@Repository
public interface ShowRepository extends JpaRepository<Show, String> {

	List<Show> findAllByShouldGetRatingChecked(boolean shouldGetRatingChecked);

	Page<Show> findByIsRecommendedOrderByReleaseDateDesc(boolean isRecommended, Pageable pageable);
	
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Show s SET s.releaseDate = ?1 WHERE s.traktId = ?2")
	void setReleaseDate(LocalDate date, String traktId);
	
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Show s SET s.shouldGetRatingChecked = ?1 WHERE s.traktId = ?2")
	void setShouldGetRatingChecked(boolean shouldGetRatingChecked, String traktId);

	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Show s SET s.isRecommended = ?1 WHERE s.traktId = ?2")
	void setIsRecommended(boolean isRecommended, String traktId);
}
