package com.karasov.file_service.controller;

import com.karasov.file_service.dto.ErrorResponseDto;
import com.karasov.file_service.dto.FileNameEditDto;
import com.karasov.file_service.handler.exception.InvalidTokenException;
import com.karasov.file_service.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * Контроллер для управления файлами.
 * Обеспечивает функции загрузки, скачивания, удаления, изменения имени файлов, а также получения списка файлов.
 */
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * Скачивание файла с сервера.
     *
     * @param token    Токен авторизации.
     * @param fileName Имя файла, который требуется скачать.
     * @return Если файл найден, возвращается поток данных с содержимым файла,
     * иначе возвращается ошибка 400. В случае системной ошибки возвращается ошибка 500.
     * @throws InvalidTokenException если токен авторизации отсутствует или недействителен.
     */
    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String fileName
    ) {

        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException();
        }

        try {
            InputStreamResource resource = fileService.getFile(fileName);
            return resource != null ? ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(resource)
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto("Error input data", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Error download file", 500));
        }
    }

    /**
     * Загрузка нового файла на сервер.
     *
     * @param token    Токен авторизации.
     * @param fileName Имя загружаемого файла.
     * @param file     Содержимое загружаемого файла в формате {@link MultipartFile}.
     * @return Если файл успешно загружен, возвращается сообщение об успешной загрузке,
     * иначе ошибка 400. В случае системной ошибки возвращается ошибка 500.
     * @throws InvalidTokenException если токен авторизации отсутствует или недействителен.
     */
    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String fileName,
            @RequestPart("file") MultipartFile file
    ) {

        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException();
        }

        try {
            return fileService.saveFile(fileName, file.getBytes(), file.getSize()) ?
                    ResponseEntity.ok("Success upload")
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto("Error input data", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Error upload file", 500));
        }
    }

    /**
     * Удаление файла с сервера.
     *
     * @param token    Токен авторизации.
     * @param fileName Имя файла, который требуется удалить.
     * @return Если файл успешно удалён, возвращается сообщение об успешном удалении,
     * иначе ошибка 400. В случае системной ошибки возвращается ошибка 500.
     * @throws InvalidTokenException если токен авторизации отсутствует или недействителен.
     */
    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String fileName
    ) {

        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException();
        }

        try {
            return fileService.deleteFile(fileName) ?
                    ResponseEntity.ok("Success deleted")
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto("Error input data", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Error delete file", 500));
        }
    }

    /**
     * Изменение имени файла.
     *
     * @param token           Токен авторизации.
     * @param oldFileName     Старое имя файла.
     * @param fileNameEditDto DTO с новым именем файла.
     * @return Если имя файла успешно изменено, возвращается сообщение об успешном изменении,
     * иначе ошибка 400. В случае системной ошибки возвращается ошибка 500.
     * @throws InvalidTokenException если токен авторизации отсутствует или недействителен.
     */
    @PutMapping("/file")
    public ResponseEntity<?> editFileName(
            @RequestHeader("auth-token") String token,
            @RequestParam("filename") String oldFileName,
            @RequestBody FileNameEditDto fileNameEditDto

    ) {

        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException();
        }

        try {
            return fileService.updateFileName(oldFileName, fileNameEditDto.filename()) ?
                    ResponseEntity.ok("Success edited")
                    : ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto("Error input data", 400));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Error edit file", 500));
        }
    }

    /**
     * Получение списка файлов с сервера.
     *
     * @param token Токен авторизации.
     * @param limit Максимальное количество файлов в списке.
     * @return Список файлов в формате JSON. В случае системной ошибки возвращается ошибка 500.
     * @throws InvalidTokenException если токен авторизации отсутствует или недействителен.
     */
    @GetMapping("/list")
    public ResponseEntity<?> getListOfFiles(
            @RequestHeader("auth-token") String token,
            @RequestParam("limit") int limit
    ) {

        if (token == null || token.isEmpty()) {
            throw new InvalidTokenException();
        }

        try {
            return ResponseEntity.ok(fileService.getFileList(limit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto("Error getting file list", 500));
        }
    }
}