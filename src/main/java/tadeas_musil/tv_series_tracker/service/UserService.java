package tadeas_musil.tv_series_tracker.service;

import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.model.User;

@Service
public interface UserService {
	
	User saveUser(User user);

	User getByUsername(String username);

	void updateUser(User updatedUser);

	void followShow(String username, String traktId);

	User getByUsernameWithShows(String username);

	void unfollowShow(String username, String traktId);

}
