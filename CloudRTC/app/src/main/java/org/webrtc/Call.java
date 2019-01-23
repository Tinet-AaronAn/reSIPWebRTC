package org.webrtc;

import org.webrtc.VideoRenderer.Callbacks;

import com.cloudrtc.sdk.PeerConnectionStatsReport;

public interface Call
{
	public void MakeCall(String peerNumber);
	public void Accept();

	//public void Accept(boolean send_audio,boolean send_video);
	
	public void Reject(int code, String reason);
    public int GetCallId();
    public int Hangup();
	public void OnCallOffer(String offerSdp);
	public void OnCallAnswer(String answerSdp);

    public void RegisterCallStateObserver(CallStateEventListener observer);
    public void RegisterCallStreamObserver(CallStreamObserver Observer);
    public void OnCallStateChange(int call_id, int state);
    //public void DeRegisterCallStateObserver();
	public void StartVideoRender(Callbacks remoteRender);
	public void close();
	public void setPeerConnectionStatsReport(PeerConnectionStatsReport mListener);
	public void enableStatsEvents(boolean enable, int periodMs);
	public void setAudioEnabled(final boolean enable);
	public void setVideoEnabled(final boolean enable);
	 
   // public int UpdateCall(boolean  enable_video);

   // public int Hold();

   // public int UnHold();
    
   // public int SendDtmf(DtmfMethod dtmf_method, String tone, boolean play_dtmf_tone);

    //public String GetCallerId();
	
	//public Direction GetDirection() ;
	
	//public CallState GetCallState();
	
	//public boolean GetSupportVideo();
	
	//public boolean GetSupportData();
	
	//public SipProfile GetProfile();
	
	//public MediaStream GetMediaStream();
	
   // public int GetErrorCode();

    //public String GetErrorReason();
    
   // public  CallReport GetCallReport();
    
   // public String CallStateName(CallState state);
    
   // public String GetUniqueId();
}
