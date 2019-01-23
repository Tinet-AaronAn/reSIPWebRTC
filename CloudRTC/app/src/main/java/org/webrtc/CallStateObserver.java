package org.webrtc;

public interface CallStateObserver {
	public void OnIncomingCall(final int acc_id, final int call_id);
	public void OnCallStateChange(final int call_id, final int state);
	public void OnCallOffer(int callId, String offerSdp);
    public void OnCallAnswer(int callId, String  answerSdp);
}
