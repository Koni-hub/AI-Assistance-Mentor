package com.ai.mentor_assistant.Api;

import com.ai.mentor_assistant.BuildConfig;
import com.ai.mentor_assistant.Models.ChatRequest;
import com.ai.mentor_assistant.Models.ChatResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OpenaiAPI {

    default String getAuthorizationHeader() {
        return "Bearer " + BuildConfig.GEMINI_API_KEY;
    }

    String URL = "v1beta/models/gemini-1.5-flash-latest:generateContent";

    @POST(URL)
    Call<ChatResponse> getChatResponse(
            @Body ChatRequest request,
            @Query("key") String apiKey
    );
}