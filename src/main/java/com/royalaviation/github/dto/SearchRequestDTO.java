package com.royalaviation.github.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchRequestDTO {
    @NotBlank
    private String query;

    private String language;
    private String sort = "stars"; // default
}