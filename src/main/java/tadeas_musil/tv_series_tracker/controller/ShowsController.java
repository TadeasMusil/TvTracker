package tadeas_musil.tv_series_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tadeas_musil.tv_series_tracker.model.SearchResult;
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
		SearchResult searchResult = showService.searchShows(query, page);
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
		String nextPage = builder.replaceQueryParam("page", page + 1).toUriString();
		String previousPage = builder.replaceQueryParam("page", page - 1).toUriString();
		
		model.addAttribute("shows", searchResult.getShows());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalNumberOfPages", searchResult.getTotalNumberOfPages());
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("previousPage", previousPage);
		model.addAttribute("showToTrack",new Show());
		
		return "shows-search";
	}

	@RequestMapping("/recommended")
	public String showRecommendedShows(@RequestParam Integer pageNumber, Model model)
			throws Exception {
		Page<Show> page = showService.getRecommendedShows(pageNumber);
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
		String nextPage = builder.replaceQueryParam("page", pageNumber + 1).toUriString();
		String previousPage = builder.replaceQueryParam("page", pageNumber - 1).toUriString();
		
		model.addAttribute("shows", page.getContent());
		model.addAttribute("currentPage", pageNumber);
		model.addAttribute("totalNumberOfPages", page.getTotalPages());
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("previousPage", previousPage);
		model.addAttribute("showToTrack",new Show());
		
		return "recommended-shows";
	}
	
}
