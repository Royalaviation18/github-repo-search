package com.royalaviation.github.service;

import com.royalaviation.github.dto.GitHubApiResponse;
import com.royalaviation.github.dto.SearchRequestDTO;


import com.royalaviation.github.model.GitHubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubServiceImpl implements GitHubService {

    private final WebClient webClient;

    @Override
    public List<GitHubRepository> fetchRepositoriesFromGitHub(SearchRequestDTO request) {
        String query = request.getQuery();
        String language = request.getLanguage();
        String sort = request.getSort();

        try {
            log.info("Fetching GitHub repos with query='{}', language='{}', sort='{}'", query, language, sort);

            GitHubApiResponse response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/repositories")
                            .queryParam("q", query + " language:" + language)
                            .queryParam("sort", sort)
                            .build())
                    .retrieve()
                    .bodyToMono(GitHubApiResponse.class)
                    .block();

            if (response == null || response.getItems() == null) {
                log.warn("GitHub API returned empty response for query='{}'", query);
                return Collections.emptyList();
            }

            return response.getItems().stream()
                    .map(item -> GitHubRepository.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(safeTruncate(item.getDescription(), 1000))
                            .owner(item.getOwner() != null ? item.getOwner().getLogin() : null)
                            .language(item.getLanguage())
                            .stars(item.getStargazers_count())
                            .forks(item.getForks_count())
                            .lastUpdated(parseInstantSafely(item.getUpdated_at()))
                            .build())
                    .toList();

        } catch (Exception e) {
            log.error("Error fetching GitHub repositories: {}", e.getMessage(), e);
            throw new RuntimeException("GitHub API call failed", e);
        }
    }

    private Instant parseInstantSafely(String timestamp) {
        try {
            return timestamp != null ? Instant.parse(timestamp) : null;
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}", timestamp);
            return null;
        }
    }

    private String safeTruncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
