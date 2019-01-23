package org.webrtc;

public interface CallStreamObserver {
	public void onAddStream(final int call_id, final MediaStream stream);
	public void onRemoveStream(final MediaStream stream);
}
