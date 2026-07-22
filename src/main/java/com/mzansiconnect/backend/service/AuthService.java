package com.mzansiconnect.backend.service;

import com.mzansiconnect.backend.dto.auth.RegisterRequest;
import com.mzansiconnect.backend.dto.auth.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);
}
