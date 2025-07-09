package com.royalaviation.github.service;

import com.royalaviation.github.model.GitHubRepository;
import com.royalaviation.github.repository.GitHubRepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryStorageServiceImplTest {

    private GitHubRepositoryRepository mockRepository;
    private RepositoryStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        mockRepository = mock(GitHubRepositoryRepository.class);
        service = new RepositoryStorageServiceImpl(mockRepository);
    }

    @Test
    void testSaveOrUpdateAll_shouldCallRepositorySaveAll() {
        List<GitHubRepository> repos = List.of(
                GitHubRepository.builder().id(1L).name("Repo1").build()
        );

        service.saveOrUpdateAll(repos);

        ArgumentCaptor<List<GitHubRepository>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockRepository, times(1)).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals("Repo1", captor.getValue().get(0).getName());
    }

    @Test
    void testGetFiltered_byLanguageAndStars() {
        GitHubRepository repo1 = GitHubRepository.builder().id(1L).name("A").language("Java").stars(50).build();
        GitHubRepository repo2 = GitHubRepository.builder().id(2L).name("B").language("Python").stars(200).build();
        GitHubRepository repo3 = GitHubRepository.builder().id(3L).name("C").language("Java").stars(10).build();

        when(mockRepository.findAll()).thenReturn(List.of(repo1, repo2, repo3));

        List<GitHubRepository> result = service.getFiltered("Java", 30, "stars");

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getName());
    }

    @Test
    void testGetFiltered_sortedByForks() {
        GitHubRepository r1 = GitHubRepository.builder().id(1L).name("A").forks(5).build();
        GitHubRepository r2 = GitHubRepository.builder().id(2L).name("B").forks(15).build();
        GitHubRepository r3 = GitHubRepository.builder().id(3L).name("C").forks(10).build();

        when(mockRepository.findAll()).thenReturn(List.of(r1, r2, r3));

        List<GitHubRepository> result = service.getFiltered(null, null, "forks");

        assertEquals(List.of("B", "C", "A"), result.stream().map(GitHubRepository::getName).toList());
    }

    @Test
    void testGetFiltered_sortedByLastUpdated() {
        GitHubRepository r1 = GitHubRepository.builder().id(1L).name("X").lastUpdated(Instant.parse("2023-01-01T00:00:00Z")).build();
        GitHubRepository r2 = GitHubRepository.builder().id(2L).name("Y").lastUpdated(Instant.parse("2023-06-01T00:00:00Z")).build();

        when(mockRepository.findAll()).thenReturn(List.of(r1, r2));

        List<GitHubRepository> result = service.getFiltered(null, null, "updated");

        assertEquals(List.of("Y", "X"), result.stream().map(GitHubRepository::getName).toList());
    }

    @Test
    void testGetFiltered_handlesNullsGracefully() {
        GitHubRepository r1 = GitHubRepository.builder().id(1L).name("X").stars(null).build();
        GitHubRepository r2 = GitHubRepository.builder().id(2L).name("Y").stars(5).build();

        when(mockRepository.findAll()).thenReturn(List.of(r1, r2));

        List<GitHubRepository> result = service.getFiltered(null, null, "stars");

        // Debug output
        System.out.println("Sorted result (by stars): " + result.stream().map(GitHubRepository::getName).toList());

        assertEquals(2, result.size());
        assertEquals("Y", result.get(0).getName()); // Expect Y (5 stars) before X (null)
    }
}
