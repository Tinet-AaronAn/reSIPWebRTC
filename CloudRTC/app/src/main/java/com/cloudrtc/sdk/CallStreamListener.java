package com.cloudrtc.sdk;

import org.webrtc.MediaStream;

public interface CallStreamListener {
	public void onAddStream(final int call_id, final MediaStream stream);
	public void onRemoveStream(final MediaStream stream);
   // public void OnMediaStreamReady(long callPtr, int stream_type);

}
