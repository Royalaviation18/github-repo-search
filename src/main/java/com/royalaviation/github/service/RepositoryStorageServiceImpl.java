package com.royalaviation.github.service;

import com.royalaviation.github.model.GitHubRepository;
import com.royalaviation.github.repository.GitHubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryStorageServiceImpl implements RepositoryStorageService {

    private final GitHubRepositoryRepository repository;

    @Override
    public void saveOrUpdateAll(List<GitHubRepository> repositories) {
        for (GitHubRepository repo : repositories) {
            repository.save(repo); // Will upsert due to ID being PK
        }
    }

    @Override
    public List<GitHubRepository> getFiltered(String language, Integer minStars, String sort) {
        return repository.findFiltered(language, minStars, sort);
    }
}