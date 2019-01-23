package org.webrtc;

public interface CallStateEventListener {
		public void OnCallStateChange(int call_id, int state_code);
		//public void OnDtmf(long callPtr, String tone);
		//public void OnMediaStreamReady(long callPtr, int stream_type);
}
