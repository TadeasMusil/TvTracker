package tadeas_musil.tv_series_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tadeas_musil.tv_series_tracker.model.ShowsPage;
import tadeas_musil.tv_series_tracker.model.Show;
import tadeas_musil.tv_series_tracker.service.ShowService;

@Controller
@RequestMapping("/shows")
public class ShowsController {

	@Autowired
	private ShowService showService;

	@RequestMapping("/search")
	public String showShows(@RequestParam String query, @RequestParam Integer page, Model model)
			throws Exception {
		ShowsPage shows = showService.searchShows(query, page);
		
		model.addAttribute("shows", shows.getShows());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalNumberOfPages", shows.getTotalNumberOfPages());
		model.addAttribute("nextPage", getPageUrl(page + 1));
		model.addAttribute("previousPage", getPageUrl(page - 1));
		model.addAttribute("showToTrack",new Show());
		
		return "shows-search";
	}

	private String getPageUrl(int page){
		return ServletUriComponentsBuilder.fromCurrentRequest()
											.replaceQueryParam("page", page)
											.toUriString();
	}

	@RequestMapping("/recommended")
	public String showRecommendedShows(@RequestParam Integer page, Model model)
			throws Exception {
		Page<Show> shows = showService.getRecommendedShows(page);
		
		model.addAttribute("shows", shows.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalNumberOfPages", shows.getTotalPages());
		model.addAttribute("nextPage", getPageUrl(page + 1));
		model.addAttribute("previousPage", getPageUrl(page - 1));
		model.addAttribute("showToTrack",new Show());
		
		return "recommended-shows";
	}
	
}
