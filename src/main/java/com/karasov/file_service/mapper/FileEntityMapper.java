package com.karasov.file_service.mapper;

import com.karasov.file_service.dto.FileResponseDto;
import com.karasov.file_service.model.FileEntity;

public class FileEntityMapper {
    public static FileEntity mapToFileEntity(String fileName, byte[] bytes, long size) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(fileName);
        fileEntity.setBytes(bytes);
        fileEntity.setSize(size);
        return fileEntity;
    }

    public static FileResponseDto mapFileEntityToFileResponseDto(FileEntity fileEntity) {
        return new FileResponseDto(fileEntity.getName(), fileEntity.getSize());
    }
}
