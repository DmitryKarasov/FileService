package com.karasov.file_service.service;

import com.karasov.file_service.dto.FileResponseDto;
import com.karasov.file_service.model.FileEntity;
import com.karasov.file_service.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@SpringBootTest
public class FileServiceTest {
    @MockBean
    private FileRepository fileRepository;
    @Autowired
    private FileService fileService;

    @BeforeEach
    void setUp() {
        reset(fileRepository);
    }

    @Test
    void testSaveFile_WhenFileDoesNotExist() {
        String fileName = "file1";
        byte[] fileBytes = new byte[] {1, 2, 3};
        long size = 3L;

        when(fileRepository.getFileEntityByName(fileName)).thenReturn(Optional.empty());

        boolean result = fileService.saveFile(fileName, fileBytes, size);

        assertTrue(result);
        verify(fileRepository).save(any(FileEntity.class));
    }

    @Test
    void testSaveFile_WhenFileExists() {
        String fileName = "file1";
        byte[] fileBytes = new byte[] {1, 2, 3};
        long size = 3L;

        when(fileRepository.getFileEntityByName(fileName)).thenReturn(Optional.of(new FileEntity()));

        boolean result = fileService.saveFile(fileName, fileBytes, size);

        assertFalse(result);
        verify(fileRepository, never()).save(any(FileEntity.class));
    }

    @Test
    void testGetFileList() {
        when(fileRepository.findAllFiles()).thenReturn(List.of(
                new FileEntity("file1", new byte[] {1, 2, 3}, 3L),
                new FileEntity("file2", new byte[] {4, 5, 6}, 3L)
        ));

        List<FileResponseDto> fileList = fileService.getFileList(1);

        assertEquals(1, fileList.size());
        assertEquals("file1", fileList.get(0).filename());
    }

    @Test
    void testDeleteFile_WhenFileExists() {
        String fileName = "file1";

        when(fileRepository.getFileEntityByName(fileName)).thenReturn(Optional.of(new FileEntity()));

        boolean result = fileService.deleteFile(fileName);

        assertTrue(result);
        verify(fileRepository).deleteByName(fileName);
    }

    @Test
    void testDeleteFile_WhenFileDoesNotExist() {
        String fileName = "file1";

        when(fileRepository.getFileEntityByName(fileName)).thenReturn(Optional.empty());

        boolean result = fileService.deleteFile(fileName);

        assertFalse(result);
        verify(fileRepository, never()).deleteByName(fileName);
    }

    @Test
    void testUpdateFileName_WhenFileExists() {
        String oldFileName = "file1";
        String newFileName = "file2";

        when(fileRepository.getFileEntityByName(oldFileName)).thenReturn(Optional.of(new FileEntity()));

        boolean result = fileService.updateFileName(oldFileName, newFileName);

        assertTrue(result);
        verify(fileRepository).updateByName(oldFileName, newFileName);
    }

    @Test
    void testUpdateFileName_WhenFileDoesNotExist() {
        String oldFileName = "file1";
        String newFileName = "file2";

        when(fileRepository.getFileEntityByName(oldFileName)).thenReturn(Optional.empty());

        boolean result = fileService.updateFileName(oldFileName, newFileName);

        assertFalse(result);
        verify(fileRepository, never()).updateByName(oldFileName, newFileName);
    }

    @Test
    void testGetFile_WhenFileExists() {
        String fileName = "file1";

        FileEntity fileEntity = new FileEntity(fileName, new byte[] {1, 2, 3}, 3L);
        when(fileRepository.getFileEntityByName(fileName)).thenReturn(Optional.of(fileEntity));

        InputStreamResource fileResource = fileService.getFile(fileName);

        assertNotNull(fileResource);
    }

    @Test
    void testGetFile_WhenFileDoesNotExist() {
        String fileName = "file1";

        when(fileRepository.getFileEntityByName(fileName)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> fileService.getFile(fileName));
    }
}
