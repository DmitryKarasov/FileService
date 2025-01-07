package com.karasov.file_service.repository;

import com.karasov.file_service.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, String> {
    @Query(value = "SELECT * FROM files",
            nativeQuery = true)
    List<FileEntity> findAllFiles();

    @Modifying
    @Query(value = "DELETE FROM files WHERE name = :name",
            nativeQuery = true)
    void deleteByName(@Param("name") String name);

    @Query(value = "SELECT * FROM files WHERE name = :name",
            nativeQuery = true)
    Optional<FileEntity> getFileEntityByName(@Param("name") String name);

    @Modifying
    @Query(value = "UPDATE files SET name = :new_name WHERE name = :old_name",
            nativeQuery = true)
    void updateByName(@Param("old_name") String oldFileName,
                      @Param("new_name") String newFileName);
}
