package com.example.voicechat;

import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service
public class Transcriber {

  private final TranscriptionModel transcriptionModel;

  public Transcriber(TranscriptionModel transcriptionModel) {
    this.transcriptionModel = transcriptionModel;
  }

  public String transcribe(byte[] audio) {
    var audioResource = new ByteArrayResource(audio) {
      @Override
      public String getFilename() {
        return "audio.wav";
      }
    };

    return transcriptionModel.transcribe(audioResource);
  }

}
