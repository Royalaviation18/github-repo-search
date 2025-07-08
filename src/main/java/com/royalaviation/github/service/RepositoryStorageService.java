package com.royalaviation.github.service;

import com.royalaviation.github.model.GitHubRepository;

import java.util.List;

public interface RepositoryStorageService {
    void saveOrUpdateAll(List<GitHubRepository> repositories);
    List<GitHubRepository> getFiltered(String language, Integer minStars, String sort);
}