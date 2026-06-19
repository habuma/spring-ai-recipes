package com.example.voicechat;

import javazoom.jl.player.Player;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.elevenlabs.ElevenLabsTextToSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class SpeechService {

  private final TextToSpeechModel speechModel;

  public SpeechService(
      @Qualifier("elevenLabsSpeechModel") TextToSpeechModel speechModel) {
    this.speechModel = speechModel;
  }

  public void speak(String text) {
    var options = ElevenLabsTextToSpeechOptions.builder()
        .voice("mKoqwDP2laxTdq1gEgU6")
        .build();

    var prompt = new TextToSpeechPrompt(text, options);
    var response = speechModel.call(prompt);
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
