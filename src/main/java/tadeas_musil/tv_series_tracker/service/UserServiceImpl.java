package tadeas_musil.tv_series_tracker.service;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.repository.ShowRepository;
import tadeas_musil.tv_series_tracker.repository.UserRepository;

@Service("userServiceImpl")
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ShowRepository showRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private ShowService showService;
	
	@Override
	public User saveUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}

	public void updateUser(User updatedInfo) {
		User user = getByUsername(updatedInfo.getUsername());
		if (StringUtils.isNotBlank(updatedInfo.getPassword())) {
			user.setPassword(passwordEncoder.encode(updatedInfo.getPassword()));
		}
		user.setGettingScheduleNotification(updatedInfo.isGettingScheduleNotification());
		user.setGettingRecommendedShowsNotification(updatedInfo.isGettingRecommendedShowsNotification());
		userRepository.save(user);
	}

	@Override
	public User getByUsername(String username) {
		return userRepository.findByUsername(username);
	}
	@Override
	public User getByUsernameWithShows(String username) {
		return userRepository.findByUsernameFetchShows(username);
	}
	
	public void followShow(String username, String traktId) {
		Show show = showRepository.findById(traktId)
					.orElseGet(() -> showService.findShow(traktId));
					
		User user = userRepository.findByUsernameFetchShows(username);
		user.getFollowedShows().add(show);
		userRepository.save(user);
	}

	public void unfollowShow(String username, String traktId){
		User user = userRepository.findByUsernameFetchShows(username);
		Show show = showRepository.findById(traktId).get();
		user.getFollowedShows().remove(show);
		userRepository.save(user);
	}

}
