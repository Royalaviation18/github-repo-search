package com.royalaviation.github;

import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.royalaviation.github.controller.GitHubRepositoryController;
import com.royalaviation.github.dto.SearchRequestDTO;
import com.royalaviation.github.exception.GlobalExceptionHandler;
import com.royalaviation.github.service.GitHubService;
import com.royalaviation.github.service.RepositoryStorageService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LambdaHttpApiV2InvocationTest {

    private static final GitHubService GITHUB_SERVICE = mock(GitHubService.class);
    private static final RepositoryStorageService STORAGE_SERVICE = mock(RepositoryStorageService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void translatesFunctionUrlPostAndGetEventsToExistingRoutes() throws Exception {
        reset(GITHUB_SERVICE, STORAGE_SERVICE);
        when(GITHUB_SERVICE.fetchRepositoriesFromGitHub(any())).thenReturn(List.of());
        when(STORAGE_SERVICE.getFiltered("Java", 50, "forks")).thenReturn(List.of());

        SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler =
                SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(LambdaTestApplication.class, "lambda");

        String postEvent = """
                {
                  "version": "2.0",
                  "routeKey": "$default",
                  "rawPath": "/api/github/search",
                  "rawQueryString": "",
                  "headers": {"content-type": "application/json"},
                  "requestContext": {"http": {"method": "POST", "path": "/api/github/search", "protocol": "HTTP/1.1", "sourceIp": "127.0.0.1", "userAgent": "test"}},
                  "body": "{\\"query\\":\\"spring boot\\",\\"language\\":\\"Java\\",\\"sort\\":\\"stars\\"}",
                  "isBase64Encoded": false
                }
                """;
        JsonNode postResponse = invoke(handler, postEvent);
        JsonNode getResponse = invoke(handler, """
                {
                  "version": "2.0",
                  "routeKey": "$default",
                  "rawPath": "/api/github/repositories",
                  "rawQueryString": "language=Java&minStars=50&sort=forks",
                  "queryStringParameters": {"language": "Java", "minStars": "50", "sort": "forks"},
                  "headers": {},
                  "requestContext": {"http": {"method": "GET", "path": "/api/github/repositories", "protocol": "HTTP/1.1", "sourceIp": "127.0.0.1", "userAgent": "test"}},
                  "isBase64Encoded": false
                }
                """);

        assertEquals(200, postResponse.path("statusCode").asInt());
        assertEquals("Repositories fetched and saved successfully",
                objectMapper.readTree(postResponse.path("body").asText()).path("message").asText());
        assertTrue(postResponse.path("headers").toString().toLowerCase().contains("application/json"));
        assertEquals(200, getResponse.path("statusCode").asInt());
        assertEquals("[]", objectMapper.readTree(getResponse.path("body").asText()).path("repositories").toString());
        ArgumentCaptor<SearchRequestDTO> requestCaptor = ArgumentCaptor.forClass(SearchRequestDTO.class);
        verify(GITHUB_SERVICE).fetchRepositoriesFromGitHub(requestCaptor.capture());
        assertEquals("spring boot", requestCaptor.getValue().getQuery());
        assertEquals("Java", requestCaptor.getValue().getLanguage());
        assertEquals("stars", requestCaptor.getValue().getSort());
        verify(STORAGE_SERVICE).getFiltered("Java", 50, "forks");

        HttpHeaders rateLimitHeaders = new HttpHeaders();
        rateLimitHeaders.set("X-RateLimit-Remaining", "0");
        when(GITHUB_SERVICE.fetchRepositoriesFromGitHub(any())).thenThrow(
                WebClientResponseException.create(403, "Forbidden", rateLimitHeaders, new byte[0],
                        StandardCharsets.UTF_8));
        JsonNode rateLimitResponse = invoke(handler, postEvent);
        assertEquals(503, rateLimitResponse.path("statusCode").asInt());
        String rateLimitBody = rateLimitResponse.path("body").asText();
        assertTrue(rateLimitBody.contains("GitHub API rate limit exceeded"));
        assertFalse(rateLimitBody.contains("Forbidden"));
    }

    private JsonNode invoke(
            SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler,
            String event) throws IOException {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        handler.proxyStream(new ByteArrayInputStream(event.getBytes(StandardCharsets.UTF_8)),
                response, mock(Context.class));
        return objectMapper.readTree(response.toByteArray());
    }

    @Configuration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    @Import({GitHubRepositoryController.class, GlobalExceptionHandler.class})
    static class LambdaTestApplication {

        @Bean
        GitHubService gitHubService() {
            return GITHUB_SERVICE;
        }

        @Bean
        RepositoryStorageService repositoryStorageService() {
            return STORAGE_SERVICE;
        }
    }
}
