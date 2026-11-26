package com.example.qxoticvoicechat;

import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.audio.tts.TextToSpeechOptions;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

@Service
public class SpeechService {

  private final TextToSpeechModel speechModel;

  public SpeechService(TextToSpeechModel speechModel) {
    this.speechModel = speechModel;
  }

  @Async
  public void speak(String text) {
    TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, TextToSpeechOptions.builder()
        .speed(1.25)
        .build());
    TextToSpeechResponse response = speechModel.call(prompt);
    byte[] audioBytes = response.getResult().getOutput();

    try {
      play(audioBytes);
    } catch (Exception e) {
      System.err.println("Unable to speak: " + e);
      e.printStackTrace();
    }
  }

  /** Plays WAV or MP3 (the latter via the mp3spi service provider) by decoding to 16-bit PCM. */
  private void play(byte[] audioBytes) throws Exception {
    try (AudioInputStream source = AudioSystem.getAudioInputStream(
        new BufferedInputStream(new ByteArrayInputStream(audioBytes)))) {
      AudioFormat base = source.getFormat();
      AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
          base.getSampleRate(), 16, base.getChannels(),
          base.getChannels() * 2, base.getSampleRate(), false);

      try (AudioInputStream decoded = AudioSystem.getAudioInputStream(pcm, source);
           SourceDataLine line = AudioSystem.getSourceDataLine(pcm)) {
        line.open(pcm);
        line.start();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = decoded.read(buffer, 0, buffer.length)) != -1) {
          line.write(buffer, 0, read);
        }
        line.drain();
      }
    }
  }

}
