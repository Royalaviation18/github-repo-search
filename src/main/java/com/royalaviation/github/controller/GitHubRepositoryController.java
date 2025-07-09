package com.royalaviation.github.controller;

import com.royalaviation.github.dto.SearchRequestDTO;
import com.royalaviation.github.model.GitHubRepository;
import com.royalaviation.github.service.GitHubService;
import com.royalaviation.github.service.RepositoryStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubRepositoryController {

    private final GitHubService gitHubService;
    private final RepositoryStorageService repositoryStorageService;

    /**
     * POST /api/github/search
     * Calls GitHub API with query params, stores result in DB
     */
    @PostMapping("/search")
    public Map<String, Object> searchAndStore(@Valid @RequestBody SearchRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<GitHubRepository> fetchedRepos = gitHubService.fetchRepositoriesFromGitHub(request);
            repositoryStorageService.saveOrUpdateAll(fetchedRepos);

            response.put("message", "Repositories fetched and saved successfully");
            response.put("repositories", fetchedRepos);
        } catch (Exception ex) {
            response.put("error", "Failed to fetch or save repositories");
            response.put("details", ex.getMessage());
            throw new RuntimeException("GitHubRepositoryController: searchAndStore failed - " + ex.getMessage(), ex);
        }

        return response;
    }

    /**
     * GET /api/github/repositories
     * Returns stored repos from DB, filtered
     */
    @GetMapping("/repositories")
    public Map<String, Object> getStoredRepositories(@RequestParam(required = false) String language, @RequestParam(required = false) Integer minStars, @RequestParam(defaultValue = "stars") String sort) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<GitHubRepository> result = repositoryStorageService.getFiltered(language, minStars, sort);
            response.put("repositories", result);
        } catch (Exception ex) {
            response.put("error", "Failed to fetch repositories from database");
            response.put("details", ex.getMessage());
            throw new RuntimeException("GitHubRepositoryController: getStoredRepositories failed - " + ex.getMessage(), ex);
        }

        return response;
    }
}
