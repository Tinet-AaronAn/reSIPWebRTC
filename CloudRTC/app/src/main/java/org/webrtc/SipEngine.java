package org.webrtc;

public interface SipEngine {
		
    public boolean SipEngineInitialize();

    public boolean Terminate();

   // public boolean RunEventLoop();

   // public SipProfileManager GetSipProfileManager();

    public CallManager GetCallManager();

    public RegistrationManager GetRegistrationManager();

   // public MediaEngine GetMediaEngine();
    
    //public Config GetDefaultConfig();
}
