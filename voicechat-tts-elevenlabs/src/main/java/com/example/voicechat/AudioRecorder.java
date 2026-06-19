package com.example.voicechat;

import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Component
public class AudioRecorder {

  private final AudioFormat format = new AudioFormat(
      16000.0f, 16, 1, true, false);

  private TargetDataLine microphone;
  private ByteArrayOutputStream pcmOutput;
  private Thread recordingThread;

  public void start() throws Exception {
    microphone = AudioSystem.getTargetDataLine(format);
    microphone.open(format);
    microphone.start();

    pcmOutput = new ByteArrayOutputStream();

    recordingThread = Thread.startVirtualThread(() -> {
      byte[] buffer = new byte[4096];

      while (microphone.isOpen()) {
        int bytesRead = microphone.read(buffer, 0, buffer.length);
        if (bytesRead > 0) {
          pcmOutput.write(buffer, 0, bytesRead);
        }
      }
    });
  }

  public byte[] stop() throws Exception {
    microphone.stop();
    microphone.close();
    recordingThread.join();

    byte[] pcmBytes = pcmOutput.toByteArray();

    long frameLength = pcmBytes.length / format.getFrameSize();

    try (
        var pcmInput = new ByteArrayInputStream(pcmBytes);
        var audioStream = new AudioInputStream(
            pcmInput, format, frameLength);
        var wavOutput = new ByteArrayOutputStream()
    ) {
      AudioSystem.write(
          audioStream,
          AudioFileFormat.Type.WAVE,
          wavOutput);

      return wavOutput.toByteArray();
    }
  }

}
