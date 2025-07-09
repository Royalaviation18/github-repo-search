package com.royalaviation.github.repository;

import com.royalaviation.github.model.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GitHubRepositoryRepository extends JpaRepository<GitHubRepository, Long> {
}