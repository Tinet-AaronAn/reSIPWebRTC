package org.webrtc;

public class PeerConnectionParameters {
    public final boolean videoCallEnabled;
    public final boolean tracing;
    public final boolean useCamera2;
    public final int videoWidth;
    public final int videoHeight;
    public final int videoFps;
    public final int videoStartBitrate;
    public final String videoCodec;
    public final boolean videoCodecHwAcceleration;
    public final boolean captureToTexture;
    public final int audioStartBitrate;
    public final String audioCodec;
    public final boolean noAudioProcessing;
    public final boolean aecDump;
    public final boolean useOpenSLES;
    public final boolean disableBuiltInAEC;
    public final boolean disableBuiltInAGC;
    public final boolean disableBuiltInNS;
    public final boolean enableLevelControl;

    public PeerConnectionParameters(
        boolean videoCallEnabled, boolean tracing, boolean useCamera2,
        int videoWidth, int videoHeight, int videoFps,
        int videoStartBitrate, String videoCodec, boolean videoCodecHwAcceleration,
        boolean captureToTexture, int audioStartBitrate, String audioCodec,
        boolean noAudioProcessing, boolean aecDump, boolean useOpenSLES,
        boolean disableBuiltInAEC, boolean disableBuiltInAGC, boolean disableBuiltInNS,
        boolean enableLevelControl) {
      this.videoCallEnabled = videoCallEnabled;
      this.useCamera2 = useCamera2;
      this.tracing = tracing;
      this.videoWidth = videoWidth;
      this.videoHeight = videoHeight;
      this.videoFps = videoFps;
      this.videoStartBitrate = videoStartBitrate;
      this.videoCodec = videoCodec;
      this.videoCodecHwAcceleration = videoCodecHwAcceleration;
      this.captureToTexture = captureToTexture;
      this.audioStartBitrate = audioStartBitrate;
      this.audioCodec = audioCodec;
      this.noAudioProcessing = noAudioProcessing;
      this.aecDump = aecDump;
      this.useOpenSLES = useOpenSLES;
      this.disableBuiltInAEC = disableBuiltInAEC;
      this.disableBuiltInAGC = disableBuiltInAGC;
      this.disableBuiltInNS = disableBuiltInNS;
      this.enableLevelControl = enableLevelControl;
    }
}
