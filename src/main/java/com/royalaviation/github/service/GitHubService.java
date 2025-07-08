package com.royalaviation.github.service;

import com.royalaviation.github.dto.SearchRequestDTO;
import com.royalaviation.github.model.GitHubRepository;

import java.util.List;

public interface GitHubService {
    List<GitHubRepository> fetchRepositoriesFromGitHub(SearchRequestDTO request);
}