package org.webrtc;

public interface CallManager {

    public Call CreateCall(int accId);

    public void MakeCall(int accId, int callId, String peerNumber, String localSdp);
    
	public void Accept(int callId, String localSdp);
	
	//public void Accept(boolean send_audio,boolean send_video);
	
	public void Reject(int code, String reason);

    public int Hangup(int callId);

    public void RegisterIncomingCallObserver(IncomingCallObserver observer);
    
    public void RegisterCallStateObserver(CallStateEventListener observer);

    public void DeRegisterCallStateObserver();

    public void registerCall(Call call);
	
   	public void unregisterCall(Call call);
   	
   	public PeerConnectionFactory getPeerConnectionFactory();
   	
   	public AudioSource createlocalAudioSource();
   	
   	public VideoSource createlocalVideoSource(int width, int height, int framerate);
   	
   	public void stoplocalMediaSource();
   	
   	public void setPeerConnectionParameters(PeerConnectionParameters peerConnectionParameters);
   	
   	public PeerConnectionParameters getPeerConnectionParameters();

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
