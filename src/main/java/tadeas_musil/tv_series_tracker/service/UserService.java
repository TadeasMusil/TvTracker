package tadeas_musil.tv_series_tracker.service;

import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.model.User;

@Service
public interface UserService {
	
	User createUser(User user);

	User getByUsername(String username);

	void updateUser(User updatedUser);

	void followShow(String username, String traktId);

	void unfollowShow(String username, String traktId);
/**
 * Returns user and his followed shows
 */
	User getByUsernameWithShows(String username);

	

}
