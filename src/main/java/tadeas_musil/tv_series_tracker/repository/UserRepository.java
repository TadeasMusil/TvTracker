package tadeas_musil.tv_series_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tadeas_musil.tv_series_tracker.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  
  User findByUsername(String username);

  @Query("SELECT u FROM User u left JOIN FETCH u.followedShows WHERE u.username = ?1" )
  User findByUsernameFetchShows(String username);

  @Query("SELECT u FROM User u JOIN FETCH u.followedShows s WHERE u.dailyScheduleNotification = true AND s.traktId IN :showIds ")
  List<User> findAllTrackingAtLeastOneShowFetchShows(List<String> showIds);
}
