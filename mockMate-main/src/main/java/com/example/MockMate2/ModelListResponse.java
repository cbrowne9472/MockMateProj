package com.example.MockMate2;

import java.util.List;

public record ModelListResponse(String object, List<GeminiModel> data) {
}
