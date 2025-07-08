package com.royalaviation.github.repository;

import com.royalaviation.github.model.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, Long> {

    @Query("SELECT r FROM GitHubRepository r WHERE " +
            "(:language IS NULL OR r.language = :language) AND " +
            "(:minStars IS NULL OR r.stars >= :minStars) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'stars' THEN r.stars " +
            "     WHEN :sort = 'forks' THEN r.forks " +
            "     WHEN :sort = 'updated' THEN r.lastUpdated END DESC")
    List<GitHubRepository> findFiltered(@Param("language") String language,
                                        @Param("minStars") Integer minStars,
                                        @Param("sort") String sort);
}
