package com.ai.mentor_assistant;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.ai.mentor_assistant.Api.ApiClient;
import com.ai.mentor_assistant.Api.OpenaiAPI;
import com.ai.mentor_assistant.Models.ChatRequest;
import com.ai.mentor_assistant.Models.ChatResponse;
import com.ai.mentor_assistant.Models.GeminiContent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_CODE = 1;
    private Button start_btn;
    private TextView result_text;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewID();

        OpenaiAPI api = ApiClient.getInstance().create(OpenaiAPI.class);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.INTERNET}, RECORD_AUDIO_PERMISSION_CODE);
        }

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startListening();
            }
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(getApplicationContext(), "Ready for speech", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {
                Toast.makeText(getApplicationContext(), "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                Log.d("MainActivity", "Voice input volume changed: " + rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                Toast.makeText(getApplicationContext(), "Speech ended", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error) {
                String errorMessage = "Error: " + getErrorText(error);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    result_text.setText(matches.get(0));
                }
                String promptUser = result_text.getText().toString();
                GeminiAPI(promptUser);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");
        speechRecognizer.startListening(intent);
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client-side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No match found";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER: return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No speech input";
            default: return "Unknown error";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (tts != null) {
            tts.shutdown();
        }
    }

    private void GeminiAPI(String prompt) {
        ChatRequest chatRequest = new ChatRequest("gemini-1.5-flash", new GeminiContent(prompt));

        OpenaiAPI geminiAPI = ApiClient.getInstance().create(OpenaiAPI.class);
        String apiKey = BuildConfig.GEMINI_API_KEY;

        Call<ChatResponse> call = geminiAPI.getChatResponse(chatRequest, apiKey);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    ChatResponse chatResponse = gson.fromJson(gson.toJson(response.body()), ChatResponse.class);

                    if (chatResponse.getCandidates() != null && !chatResponse.getCandidates().isEmpty()) {
                        for (ChatResponse.Candidate candidate : chatResponse.getCandidates()) {
                            ChatResponse.Content content = candidate.getContent();
                            if (content != null && content.getParts() != null) {
                                for (ChatResponse.Part part : content.getParts()) {
                                    Log.d("MainActivity", "Text: " + part.getText());
                                    tts = new TextToSpeech(getApplicationContext(), status -> {
                                        if (status == TextToSpeech.SUCCESS) {
                                            tts.setLanguage(Locale.US);
                                            tts.setPitch(1.0f);
                                            tts.setSpeechRate(1.0f);

                                            String textToSpeak = part.getText();
                                            result_text.setText(textToSpeak);
                                            if (!textToSpeak.isEmpty()) {
                                                tts.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null, null);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        Log.e("MainActivity", "No candidates found in the response.");
                    };
                } else {
                    try {
                        String errorMessage = response.message();
                        Log.e("MainActivity", "Gemini API Error: " + errorMessage);
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error parsing Gemini API response", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e("MainActivity", "API request failed", t);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void findViewID () {
        start_btn = findViewById(R.id.start_btn);
        result_text = findViewById(R.id.result_text);
    }
}