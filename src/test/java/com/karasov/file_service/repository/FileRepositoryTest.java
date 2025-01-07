package com.karasov.file_service.repository;

import com.karasov.file_service.model.FileEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
public class FileRepositoryTest {
    @Container
    private static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                    .withDatabaseName("postgres")
                    .withUsername("user")
                    .withPassword("user");

    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM files");
        fileRepository.save(new FileEntity("file1", new byte[] {1, 2, 3}, 3L));
    }

    @Test
    @Transactional
    void testFindAllFiles() {
        List<FileEntity> files = fileRepository.findAllFiles();
        assertNotNull(files);
        assertEquals(1, files.size());
    }

    @Test
    @Transactional
    void testDeleteByName() {
        fileRepository.deleteByName("file1");
        Optional<FileEntity> file = fileRepository.getFileEntityByName("file1");
        assertTrue(file.isEmpty());
    }

    @Test
    @Transactional
    void testUpdateByName() {
        fileRepository.updateByName("file1", "file2");
        Optional<FileEntity> updatedFile = fileRepository.getFileEntityByName("file2");
        assertTrue(updatedFile.isPresent());
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM files");
    }
}

