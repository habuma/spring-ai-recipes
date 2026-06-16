package com.example.voicechat;

import javazoom.jl.player.Player;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class SpeechService {

  private final TextToSpeechModel speechModel;

  public SpeechService(TextToSpeechModel speechModel) {
    this.speechModel = speechModel;
  }

  public void speak(String text) {
    OpenAiAudioSpeechOptions.builder()
        .build();

    TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, TextToSpeechOptions.builder()
        .voice("marin")
        .build());
    TextToSpeechResponse response = speechModel.call(prompt);
    byte[] audioBytes = response.getResult().getOutput();
//    byte[] audioBytes = speechModel.call(text);

    try (var in = new ByteArrayInputStream(audioBytes)) {
      // Use this line if you'd rather write the response to an MP3 file:
      // Files.write(Path.of("/Users/habuma/audio-response.mp3"), audioBytes);
      new Player(in).play();
    } catch(Exception e) {
      System.err.println("Unable to speak: " + e.getMessage());
    }
  }

}
