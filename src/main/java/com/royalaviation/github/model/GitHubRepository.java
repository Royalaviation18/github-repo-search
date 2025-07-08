package com.royalaviation.github.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubRepository {

    @Id
    private Long id; // GitHub repository ID (unique)

    @Column(length = 512)
    private String name;

    @Lob // Allows storing large text
    @Column
    private String description;

    @Column(length = 512)
    private String owner;

    @Column(length = 100)
    private String language;

    private Integer stars;
    private Integer forks;

    private Instant lastUpdated;
}