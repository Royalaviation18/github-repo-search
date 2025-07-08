package com.royalaviation.github.dto;

import lombok.Data;

import java.util.List;

@Data
public class GitHubApiResponse {
    private List<GitHubItem> items;
}