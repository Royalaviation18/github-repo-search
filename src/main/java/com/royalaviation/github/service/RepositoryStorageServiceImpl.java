package com.royalaviation.github.service;

import com.royalaviation.github.model.GitHubRepository;
import com.royalaviation.github.repository.GitHubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryStorageServiceImpl implements RepositoryStorageService {

    private final GitHubRepositoryRepository repository;

    /**
     * Saves or updates the list of GitHub repositories in the database.
     */
    @Override
    public void saveOrUpdateAll(List<GitHubRepository> repos) {
        log.info("Saving {} GitHub repositories to the database", repos.size());
        repository.saveAll(repos);
    }

    /**
     * Returns filtered and sorted GitHub repositories from the DB.
     *
     * @param language Filter by language (nullable)
     * @param minStars Filter by minimum stars (nullable)
     * @param sort     Sort by "stars", "forks", or "updated" (defaults to stars if null/unknown)
     */
    @Override
    public List<GitHubRepository> getFiltered(String language, Integer minStars, String sort) {
        List<GitHubRepository> filtered = repository.findAll().stream()
                .filter(repo -> language == null || language.equalsIgnoreCase(repo.getLanguage()))
                .filter(repo -> minStars == null || (repo.getStars() != null && repo.getStars() >= minStars))
                .sorted(getComparator(sort))
                .toList();

        log.info("Returning {} repositories after filtering", filtered.size());
        return filtered;
    }

    /**
     * Determines the sorting comparator based on the given sort type.
     *
     * @param sort Sorting criteria: "stars", "forks", or "updated"
     * @return A comparator for sorting the list
     */
    private Comparator<GitHubRepository> getComparator(String sort) {
        if (sort == null) sort = "stars";
        return switch (sort.toLowerCase()) {
            case "forks" -> Comparator.comparing(
                    GitHubRepository::getForks,
                    Comparator.nullsLast(Integer::compareTo)
            ).reversed();
            case "updated" -> Comparator.comparing(
                    GitHubRepository::getLastUpdated,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed();
            default -> Comparator.comparing(
                    GitHubRepository::getStars,
                    Comparator.nullsLast(Integer::compareTo)
            ).reversed();
        };
    }
}
