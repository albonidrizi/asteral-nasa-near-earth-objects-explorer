package com.nasa.asteral.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nasa.asteral.model.response.dto.AsteroidDetailResponse;
import com.nasa.asteral.service.FavoriteAsteroidService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/favorite")
@RequiredArgsConstructor
public class UserFavoriteController extends BaseController {
	
	private final FavoriteAsteroidService favoriteService;
	
	@PostMapping("/add/{referenceId}")
	public String addAsteroidToFavorite(@PathVariable String referenceId, Model model) {
		favoriteService.addAsteroidToFavorite(referenceId, extractUsername());
		return ok("redirect:/", model);
	}
	
	@PostMapping("/remove/{referenceId}")
	public String removeAsteroidFromFavorite(@PathVariable String referenceId, Model model) {
		favoriteService.removeAsteroidFromFavorite(referenceId, extractUsername());
		return ok("redirect:/", model);
	}

	@PostMapping("/remove/{referenceId}/from-list")
	public String removeAsteroidFromFavoriteList(@PathVariable String referenceId, Model model) {
		favoriteService.removeAsteroidFromFavorite(referenceId, extractUsername());
		return ok("redirect:/user/favorite/all", model);
	}

	@GetMapping("/all")
	public String getFavoriteAsteroidsView(Model model) {
		List<AsteroidDetailResponse> favoriteAsteroids = favoriteService.getFavoriteAsteroids(extractUsername());
		model.addAttribute("favoriteAsteroids", favoriteAsteroids);
		
		return ok("myFavorites", model);
	}
	
	
}
