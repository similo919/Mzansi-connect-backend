package com.mzansiconnect.backend.response;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void successShouldCreateSuccessfulResponseWithData() {
        Map<String, String> data = Map.of(
                "status",
                "UP"
        );

        ApiResponse<Map<String, String>> response =
                ApiResponse.success(
                        "Request completed successfully",
                        data
                );

        assertTrue(response.isSuccess());
        assertEquals(
                "Request completed successfully",
                response.getMessage()
        );
        assertEquals(data, response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void failureShouldCreateFailedResponseWithoutData() {
        ApiResponse<Void> response =
                ApiResponse.failure(
                        "Resource not found"
                );

        assertFalse(response.isSuccess());
        assertEquals(
                "Resource not found",
                response.getMessage()
        );
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }
}
