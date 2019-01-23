package org.webrtc;
	  
public interface RegistrationEventListener {
	
	public void OnRegistrationProgress();

	public void OnRegistrationSuccess();

	public void OnRegistrationCleared();

	public void OnRegisterationFailed();
}
