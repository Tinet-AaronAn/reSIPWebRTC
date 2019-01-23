package org.webrtc;

public interface Account {
	
	    public void MakeRegister(AccountConfig accConfig);

	    public void MakeDeRegister();

	    public void RefreshRegistration();

	    public void RegisterRegistrationObserver(RegistrationEventListener observer);

	    public void DeRegisterRegistrationObserver();
	    
	    public void SetNetworkReachable(boolean yesno);
	    
	    public boolean ProfileIsRegistered();
	    
	    public int getAccountId();

}
