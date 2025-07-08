package com.royalaviation.github.service;

import com.royalaviation.github.dto.GitHubApiResponse;
import com.royalaviation.github.dto.SearchRequestDTO;
import com.royalaviation.github.model.GitHubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GitHubServiceImpl implements GitHubService {

    private final WebClient webClient;

    @Override
    public List<GitHubRepository> fetchRepositoriesFromGitHub(SearchRequestDTO request) {
        String query = request.getQuery();
        String language = request.getLanguage();
        String sort = request.getSort();

        String uri = "/search/repositories?q=" + query + "+language:" + language + "&sort=" + sort;

        GitHubApiResponse response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GitHubApiResponse.class)
                .block(); // We use block() since this is not a reactive controller

        if (response == null || response.getItems() == null) {
            throw new RuntimeException("Empty or invalid response from GitHub API");
        }

        return response.getItems().stream().map(item -> GitHubRepository.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .owner(item.getOwner().getLogin())
                .language(item.getLanguage())
                .stars(item.getStargazers_count())
                .forks(item.getForks_count())
                .lastUpdated(Instant.parse(item.getUpdated_at()))
                .build()).collect(Collectors.toList());
    }
}