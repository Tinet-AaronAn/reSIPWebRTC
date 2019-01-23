package org.webrtc;

public interface RegistrationManager {

    public Account CreateAccount();
    
    public int MakeRegister(AccountConfig accConfig);

    public void MakeDeRegister();

    public void RefreshRegistration();

    public void RegisterRegistrationObserver(RegistrationStateObserver observer);

    void DeRegisterRegistrationObserver();
    
    public void SetNetworkReachable(boolean yesno);
    
    public boolean ProfileIsRegistered();
    
    public void registerAccount(Account account);
	
	public void unregisterAccount(Account account);
	
}