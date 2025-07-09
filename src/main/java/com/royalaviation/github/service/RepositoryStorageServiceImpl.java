package com.royalaviation.github.service;

import com.royalaviation.github.model.GitHubRepository;
import com.royalaviation.github.repository.GitHubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryStorageServiceImpl implements RepositoryStorageService {

    private final GitHubRepositoryRepository repository;

    @Override
    public void saveOrUpdateAll(List<GitHubRepository> repos) {
        repository.saveAll(repos);
    }

    @Override
    public List<GitHubRepository> getFiltered(String language, Integer minStars, String sort) {
        List<GitHubRepository> filtered = repository.findAll().stream()
                .filter(repo -> language == null || language.equalsIgnoreCase(repo.getLanguage()))
                .filter(repo -> minStars == null || repo.getStars() >= minStars)
                .sorted(getComparator(sort))
                .toList();

        return filtered;
    }

    private Comparator<GitHubRepository> getComparator(String sort) {
        return switch (sort.toLowerCase()) {
            case "forks" -> Comparator.comparing(GitHubRepository::getForks, Comparator.nullsLast(Integer::compareTo)).reversed();
            case "updated" -> Comparator.comparing(GitHubRepository::getLastUpdated, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
            default -> Comparator.comparing(GitHubRepository::getStars, Comparator.nullsLast(Integer::compareTo)).reversed();
        };
    }
}
