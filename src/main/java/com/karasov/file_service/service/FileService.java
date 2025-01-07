package com.karasov.file_service.service;

import com.karasov.file_service.dto.FileResponseDto;
import org.springframework.core.io.InputStreamResource;

import java.util.List;

public interface FileService {
    boolean saveFile(String fileName, byte[] bytes, long size);

    List<FileResponseDto> getFileList(int limit);

    boolean deleteFile(String fileName);

    boolean updateFileName(String oldFileName, String newFileName);

    InputStreamResource getFile(String fileName);
}
