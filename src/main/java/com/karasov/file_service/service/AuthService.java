package com.karasov.file_service.service;

public interface AuthService {
    String authenticateAndIssueToken(String login, String password);
}
