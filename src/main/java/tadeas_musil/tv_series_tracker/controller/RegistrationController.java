package tadeas_musil.tv_series_tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import tadeas_musil.tv_series_tracker.model.User;
import tadeas_musil.tv_series_tracker.model.User.Registration;
import tadeas_musil.tv_series_tracker.service.UserService;

@Controller
public class RegistrationController {
	
	@Autowired
	private UserService userService;

	@RequestMapping("/registration")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "registration-form";
	}

	@PostMapping("/processRegistration")
	public String processRegistration(@Validated(Registration.class) @ModelAttribute("user") User user,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "registration-form";
		}
		userService.createUser(user);
		return "index";
	}
}
