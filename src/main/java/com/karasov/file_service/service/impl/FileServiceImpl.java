package com.karasov.file_service.service.impl;

import com.karasov.file_service.dto.FileResponseDto;
import com.karasov.file_service.mapper.FileEntityMapper;
import com.karasov.file_service.model.FileEntity;
import com.karasov.file_service.repository.FileRepository;
import com.karasov.file_service.service.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.karasov.file_service.mapper.FileEntityMapper.mapToFileEntity;

/**
 * Сервисный слой с логикой работы с файлами
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    /**
     * Сохраняет файл в базу данных.
     *
     * @param fileName имя файла
     * @param bytes    содержимое файла в виде массива байтов
     * @param size     размер файла
     * @return true, если файл успешно сохранен, false, если файл с таким именем уже существует
     */
    @Override
    public boolean saveFile(String fileName, byte[] bytes, long size) {
        if (fileRepository.getFileEntityByName(fileName).isPresent()) {
            return false;
        }
        fileRepository.save(mapToFileEntity(fileName, bytes, size));
        return true;
    }

    /**
     * Получает список файлов, ограниченный заданным количеством.
     *
     * @param limit максимальное количество файлов в списке
     * @return список объектов FileResponseDto, содержащий данные о файлах
     */
    @Override
    public List<FileResponseDto> getFileList(int limit) {
        return fileRepository.findAllFiles().stream()
                .limit(limit)
                .map(FileEntityMapper::mapFileEntityToFileResponseDto)
                .toList();
    }

    /**
     * Удаляет файл по имени.
     *
     * @param fileName имя файла для удаления
     * @return true, если файл был успешно удален, false, если файл не найден
     */
    @Override
    public boolean deleteFile(String fileName) {
        if (fileRepository.getFileEntityByName(fileName).isPresent()) {
            fileRepository.deleteByName(fileName);
            return true;
        }
        return false;
    }

    /**
     * Обновляет имя файла.
     *
     * @param oldFileName старое имя файла
     * @param newFileName новое имя файла
     * @return true, если имя файла успешно обновлено, false, если файл с таким старым именем не найден
     */
    @Override
    public boolean updateFileName(String oldFileName, String newFileName) {
        if (fileRepository.getFileEntityByName(oldFileName).isPresent()) {
            fileRepository.updateByName(oldFileName, newFileName);
            return true;
        }
        return false;
    }

    /**
     * Получает файл по имени.
     *
     * @param fileName имя файла для загрузки
     * @return InputStreamResource, представляющий файл для скачивания
     * @throws RuntimeException если файл не найден или произошла ошибка при загрузке
     */
    @Override
    public InputStreamResource getFile(String fileName) {
        Optional<FileEntity> optionalFile = fileRepository.getFileEntityByName(fileName);
        if (optionalFile.isPresent()) {
            FileEntity file = optionalFile.get();
            try (ByteArrayInputStream fileStream = new ByteArrayInputStream(file.getBytes())) {
                return new InputStreamResource(fileStream);
            } catch (IOException e) {
                System.err.println("Ошибка записи файла: " + e.getMessage());
            }
        }
        throw new RuntimeException("Не удалось загрузить файл");
    }
}
