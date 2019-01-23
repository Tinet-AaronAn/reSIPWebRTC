package org.webrtc;

public interface CallSdpObserver {
	
    public void OnCallOffer(int callId, String offerSdp);
    public void OnCallAnswer(int callId, String  answerSdp);

}
