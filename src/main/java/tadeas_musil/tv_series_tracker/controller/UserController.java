package tadeas_musil.tv_series_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.model.User.Settings;
import tadeas_musil.tv_series_tracker.service.UserService;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("user")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/updateUser")
	public String updateUser(@Validated(Settings.class) @ModelAttribute("user") User updatedUser,
			BindingResult bindingResult, Principal principal) {
		if (bindingResult.hasErrors()) {
			return "settings";
		}
		updatedUser.setUsername(principal.getName());
		userService.updateUser(updatedUser);
		return "settings";
	}

	@RequestMapping("/settings")
	public String showSettings(Model model, Principal principal) {
		User user = userService.getByUsername(principal.getName());
		model.addAttribute("user", user);
		return "settings";
	}

	@PostMapping("/followShow")
	public String followShow(@ModelAttribute Show showToTrack, HttpServletRequest req, Principal principal) {

		userService.followShow(principal.getName(), showToTrack.getTraktId());
		return "redirect:" + req.getHeader("Referer");
	}

	@RequestMapping("/shows")
	public String showShows(Model model, Principal principal) {
		User user = userService.getByUsernameWithShows(principal.getName());
		model.addAttribute("shows", user.getFollowedShows());
		model.addAttribute("showToRemove", new Show());
		return "user-shows";
	}

	@PostMapping("/unfollowShow")
	public String unfollowShow(@ModelAttribute Show showToRemove, HttpServletRequest req, Principal principal) {

		userService.unfollowShow(principal.getName(), showToRemove.getTraktId());
		return "redirect:" + req.getHeader("Referer");
	}
}
