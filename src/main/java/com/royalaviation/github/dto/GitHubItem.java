package com.royalaviation.github.dto;

import lombok.Data;

@Data
public class GitHubItem {
    private Long id;
    private String name;
    private String description;
    private GitHubOwner owner;
    private String language;
    private int stargazers_count;
    private int forks_count;
    private String updated_at;
}