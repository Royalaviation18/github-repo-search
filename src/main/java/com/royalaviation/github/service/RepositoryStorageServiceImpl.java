package com.royalaviation.github.service;

import com.royalaviation.github.model.GitHubRepository;
import com.royalaviation.github.repository.GitHubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        log.info("Upserting {} GitHub repositories", repos.size());

        // Fetch existing repos from DB by their IDs
        List<Long> ids = repos.stream().map(GitHubRepository::getId).toList();
        Map<Long, GitHubRepository> existingMap = repository.findAllById(ids).stream()
                .collect(Collectors.toMap(GitHubRepository::getId, Function.identity()));

        List<GitHubRepository> finalListToSave = new ArrayList<>();

        for (GitHubRepository incomingRepo : repos) {
            GitHubRepository updatedRepo = existingMap.getOrDefault(incomingRepo.getId(), new GitHubRepository());

            // Either update existing or create new
            updatedRepo.setId(incomingRepo.getId());
            updatedRepo.setName(incomingRepo.getName());
            updatedRepo.setDescription(incomingRepo.getDescription());
            updatedRepo.setOwner(incomingRepo.getOwner());
            updatedRepo.setLanguage(incomingRepo.getLanguage());
            updatedRepo.setStars(incomingRepo.getStars());
            updatedRepo.setForks(incomingRepo.getForks());
            updatedRepo.setLastUpdated(incomingRepo.getLastUpdated());

            finalListToSave.add(updatedRepo);
        }

        repository.saveAll(finalListToSave);
        log.info("Saved {} repositories to the database", finalListToSave.size());
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
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
            case "updated" -> Comparator.comparing(
                    GitHubRepository::getLastUpdated,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
            default -> Comparator.comparing(
                    GitHubRepository::getStars,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
        };
    }
}
