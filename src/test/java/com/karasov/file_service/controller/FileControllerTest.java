package com.karasov.file_service.controller;

import com.karasov.file_service.config.TestSecurityConfig;
import com.karasov.file_service.dto.FileNameEditDto;
import com.karasov.file_service.dto.FileResponseDto;
import com.karasov.file_service.service.FileService;
import com.karasov.file_service.service.impl.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@WebMvcTest(FileController.class)
@Import(TestSecurityConfig.class)
class FileControllerTest {
    @MockBean
    private FileService fileService;
    @MockBean
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;
    private final static String FILE_NAME = "example.txt";
    private final static String TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ1c2VyQG1haWwucnUiLCJyYW5kb20iOiJmOGU5M2RmMy1lMDFjLTQ0NGItYWFjYy0yMjRmZWVkMzdkODEiLCJpYXQiOjE3MzYxODU2OTcsImV4cCI6MTczNjE4OTI5N30.vpYBhyPOuchUO-Gt5AY5ri9S4qHp7gdpQQk7ikPqOAatHcV4bJ1xDn4IdiE0FABz";

    @Nested
    @DisplayName("Тесты загрузки файла")
    class DownloadFileTests {

        @DisplayName("Тест успешной выгрузки файла (возвращаемый статус 200)")
        @Test
        void downloadFileSuccessTest() throws Exception {
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream("file content".getBytes()));

            Mockito.when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn("user");
            Mockito.when(fileService.getFile(FILE_NAME)).thenReturn(resource);

            mockMvc.perform(get("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", FILE_NAME))
                    .andExpect(status().isOk())
                    .andExpect(header().string(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + FILE_NAME + "\""))
                    .andExpect(content().contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(content().string("file content"));
        }

        @DisplayName("Тест неудачной выгрузки при отсутствии файла (возвращаемый статус 400)")
        @Test
        void downloadFileWhenReturnedNullTEst() throws Exception {
            Mockito.when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn("user");
            Mockito.when(fileService.getFile(FILE_NAME)).thenReturn(null);

            mockMvc.perform(get("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", FILE_NAME))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Error input data"))
                    .andExpect(jsonPath("$.id").value(400));
        }

        @DisplayName("Тест неудачной выгрузки с ошибкой на сервере (возвращаемый статус 500)")
        @Test
        void downloadFileWhenServerError() throws Exception {
            Mockito.when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn("user");
            Mockito.when(fileService.getFile(FILE_NAME)).thenThrow(new RuntimeException("Server error"));

            mockMvc.perform(get("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", FILE_NAME))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Error download file"))
                    .andExpect(jsonPath("$.id").value(500));
        }

        @DisplayName("Тест неудачной выгрузки при отсутствии или неправильности токена (возвращаемый статус 401)")
        @Test
        void downloadFileWhenTokenIsMissingOrInvalid() throws Exception {
            String invalidToken = "";

            mockMvc.perform(get("/file")
                            .header("auth-token", invalidToken)
                            .param("filename", FILE_NAME))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Unauthorized error"))
                    .andExpect(jsonPath("$.id").value(401));
        }
    }

    @Nested
    @DisplayName("Тесты выгрузки файла")
    class UploadFileTests {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "example.txt",
                "text/plain",
                "file content".getBytes()
        );

        @DisplayName("Тест успешной выгрузки файла (возвращаемый статус 200)")
        @Test
        void uploadFileSuccessfully() throws Exception {
            Mockito.when(fileService.saveFile(FILE_NAME, file.getBytes(), file.getSize())).thenReturn(true);

            mockMvc.perform(multipart("/file")
                            .file(file)
                            .param("filename", FILE_NAME)
                            .header("auth-token", TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success upload"));
        }

        @DisplayName("Тест неудачной выгрузки при отсутствии файла (возвращаемый статус 400)")
        @Test
        void uploadFileWhenInputDataIsInvalid() throws Exception {
            Mockito.when(fileService.saveFile(FILE_NAME, file.getBytes(), file.getSize())).thenReturn(false);

            mockMvc.perform(multipart("/file")
                            .file(file)
                            .param("filename", FILE_NAME)
                            .header("auth-token", TOKEN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error input data"))
                    .andExpect(jsonPath("$.id").value(400));
        }

        @DisplayName("Тест неудачной выгрузки файла при отсутствии или неправильности токена (возвращаемый статус 401)")
        @Test
        void uploadFileWhenTokenIsMissingOrInvalid() throws Exception {
            String invalidToken = "";

            mockMvc.perform(multipart("/file")
                            .file(file)
                            .param("filename", FILE_NAME)
                            .header("auth-token", invalidToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Unauthorized error"))
                    .andExpect(jsonPath("$.id").value(401));
        }

        @DisplayName("Тест неудачной выгрузки файла при внутренней ошибке сервиса (возвращаемый статус 500)")
        @Test
        void uploadFileWhenInternalServerErrorOccurs() throws Exception {
              Mockito.when(fileService.saveFile(FILE_NAME, file.getBytes(), file.getSize())).thenThrow(new RuntimeException("Internal error"));

            mockMvc.perform(multipart("/file")
                            .file(file)
                            .param("filename", FILE_NAME)
                            .header("auth-token", TOKEN))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Error upload file"))
                    .andExpect(jsonPath("$.id").value(500));
        }
    }

    @Nested
    @DisplayName("Тесты удаления файла")
    class DeleteFileTests {

        @DisplayName("Тест успешного удаления файла (возвращаемый статус 200)")
        @Test
        void deleteFileSuccessfully() throws Exception {
            Mockito.when(fileService.deleteFile(FILE_NAME)).thenReturn(true);

            mockMvc.perform(delete("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", FILE_NAME))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success deleted"));
        }

        @DisplayName("Тест неудачного удаления файла при ошибочных входных данных (возвращаемый статус 400)")
        @Test
        void deleteFileWhenInputDataIsInvalid() throws Exception {
            Mockito.when(fileService.deleteFile(FILE_NAME)).thenReturn(false);

            mockMvc.perform(delete("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", FILE_NAME))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Error input data"))
                    .andExpect(jsonPath("$.id").value(400));
        }

        @DisplayName("Тест неудачного удаления файла при отсутствии или неправильности токена (возвращаемый статус 401)")
        @Test
        void deleteFileWhenTokenIsMissingOrInvalid() throws Exception {
            String invalidToken = "";

            mockMvc.perform(delete("/file")
                            .header("auth-token", invalidToken)
                            .param("filename", FILE_NAME))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Unauthorized error"))
                    .andExpect(jsonPath("$.id").value(401));
        }

        @DisplayName("Тест неудачного удаления файла при внутренней ошибке сервера (возвращаемый статус 500)")
        @Test
        void deleteFileWhenInternalServerErrorOccurs() throws Exception {
            Mockito.when(fileService.deleteFile(FILE_NAME)).thenThrow(new RuntimeException("Internal error"));

            mockMvc.perform(delete("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", FILE_NAME))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Error delete file"))
                    .andExpect(jsonPath("$.id").value(500));
        }
    }

    @Nested
    @DisplayName("Тесты редактирования имени файла")
    class EditFileNameTests {
        String oldFileName = "old.txt";
        FileNameEditDto fileNameEditDto = new FileNameEditDto("new.txt");

        @DisplayName("Тест успешного редактирования имени файла (возвращаемый статус 200)")
        @Test
        void editFileNameSuccessTest() throws Exception {
            Mockito.when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn("user");
            Mockito.when(fileService.updateFileName(oldFileName, fileNameEditDto.filename())).thenReturn(true);

            mockMvc.perform(put("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", oldFileName)
                            .content("{\"filename\":\"new.txt\"}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Success edited"));
        }

        @DisplayName("Тест неудачного редактирования имени файла (возвращаемый статус 400)")
        @Test
        void editFileNameWhenInputDataIsInvalid() throws Exception {
            Mockito.when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn("user");
            Mockito.when(fileService.updateFileName(oldFileName, fileNameEditDto.filename())).thenReturn(false);

            mockMvc.perform(put("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", oldFileName)
                            .content("{\"filename\":\"new.txt\"}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error input data"))
                    .andExpect(jsonPath("$.id").value(400));
        }

        @DisplayName("Тест редактирования имени файла при отсутствии или неправильности токена (возвращаемый статус 401)")
        @Test
        void editFileNameWhenTokenIsMissingOrInvalid() throws Exception {
            String invalidToken = "";

            mockMvc.perform(put("/file")
                            .header("auth-token", invalidToken)
                            .param("filename", oldFileName)
                            .content("{\"filename\":\"new.txt\"}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Unauthorized error"))
                    .andExpect(jsonPath("$.id").value(401));
        }

        @DisplayName("Тест редактирования имени файла с ошибкой на сервере (возвращаемый статус 500)")
        @Test
        void editFileNameWhenServerErrorOccurs() throws Exception {
            Mockito.when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn("user");
            Mockito.when(fileService.updateFileName(oldFileName, fileNameEditDto.filename())).thenThrow(new RuntimeException("Internal error"));

            mockMvc.perform(put("/file")
                            .header("auth-token", TOKEN)
                            .param("filename", oldFileName)
                            .content("{\"filename\":\"new.txt\"}")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Error edit file"))
                    .andExpect(jsonPath("$.id").value(500));
        }
    }

    @Nested
    @DisplayName("Тесты получения списка файлов")
    class GetListOfFilesTests {
        int limit = 3;

        @DisplayName("Тест успешного получения списка файлов (возвращаемый статус 200)")
        @Test
        void getListOfFilesSuccessTest() throws Exception {
            List<FileResponseDto> fileList = List.of(
                    new FileResponseDto("file1.txt", 1024),
                    new FileResponseDto("file2.txt", 2048),
                    new FileResponseDto("file3.txt", 4096)
            );

            Mockito.when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn("user");
            Mockito.when(fileService.getFileList(limit)).thenReturn(fileList);

            mockMvc.perform(get("/list")
                            .header("auth-token", TOKEN)
                            .param("limit", String.valueOf(limit)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(fileList.size()))
                    .andExpect(jsonPath("$[0].filename").value("file1.txt"))
                    .andExpect(jsonPath("$[0].size").value(1024))
                    .andExpect(jsonPath("$[1].filename").value("file2.txt"))
                    .andExpect(jsonPath("$[1].size").value(2048))
                    .andExpect(jsonPath("$[2].filename").value("file3.txt"))
                    .andExpect(jsonPath("$[2].size").value(4096));
        }

        @DisplayName("Тест ошибки при отсутствии или неправильности токена (возвращаемый статус 401)")
        @Test
        void getListOfFilesWhenTokenIsMissingOrInvalid() throws Exception {
            String invalidToken = "";

            mockMvc.perform(get("/list")
                            .header("auth-token", invalidToken)
                            .param("limit", String.valueOf(limit)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Unauthorized error"))
                    .andExpect(jsonPath("$.id").value(401));
        }

        @DisplayName("Тест ошибки при внутренней ошибке сервиса (возвращаемый статус 500)")
        @Test
        void getListOfFilesWhenInternalServerErrorOccurs() throws Exception {
            Mockito.when(jwtService.validateAndExtractUsername(TOKEN)).thenReturn("user");
            Mockito.when(fileService.getFileList(limit)).thenThrow(new RuntimeException("Internal error"));

            mockMvc.perform(get("/list")
                            .header("auth-token", TOKEN)
                            .param("limit", String.valueOf(limit)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Error getting file list"))
                    .andExpect(jsonPath("$.id").value(500));
        }
    }
}


