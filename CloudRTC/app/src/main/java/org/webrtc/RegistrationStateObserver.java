package org.webrtc;

public interface RegistrationStateObserver {
	
	public void OnRegistrationProgress(int acc_id);

	public void OnRegistrationSuccess(int acc_id);

	public void OnRegistrationCleared(int acc_id);

	public void OnRegistrationFailed(int acc_id);
}
