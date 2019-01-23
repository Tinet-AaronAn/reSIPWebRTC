package org.webrtc;

public class AccountImpl implements Account {

	//private long nativePtr = 0;
    private RegistrationManager mRegistrationManager;
    private int accId = -1;
    private RegistrationEventListener mRegistrationEventListener;
	//private native void MakeRegister(long nativePtr);
	//private native void MakeDeRegister(long nativePtr);
	//private native void RefreshRegistration(long nativePtr);
	//private native void RegisterRegistrationObserver(long nativePtr, RegistrationEventListener observer);
	//private native void DeRegisterRegistrationObserver(long nativePtr);
	// native void SetNetworkReachable(long nativePtr, boolean yesno);
	//private native boolean ProfileIsRegistered(long nativePtr);
	
	public AccountImpl(RegistrationManager registrationManager){
		mRegistrationManager = registrationManager;
	}
	
	@Override
	public int getAccountId()
	{
		return accId;
	}
	
	@Override
	public void MakeRegister(AccountConfig accConfig) {
		accId = mRegistrationManager.MakeRegister(accConfig);
		mRegistrationManager.registerAccount(this);
	}

	@Override
	public void MakeDeRegister() {
		mRegistrationManager.MakeDeRegister();
		mRegistrationManager.unregisterAccount(this);
	}

	@Override
	public void RefreshRegistration() {
		mRegistrationManager.RefreshRegistration();
	}

	@Override
	public void RegisterRegistrationObserver(RegistrationEventListener observer) {
		mRegistrationEventListener = observer;
	}

	@Override
	public void DeRegisterRegistrationObserver() {
		//DeRegisterRegistrationObserver(nativePtr);
	}

	@Override
	public void SetNetworkReachable(boolean yesno) {
		mRegistrationManager.SetNetworkReachable(yesno);
	}
	
	@Override
	public boolean ProfileIsRegistered()
	{
	
		return mRegistrationManager.ProfileIsRegistered();
	}
}
