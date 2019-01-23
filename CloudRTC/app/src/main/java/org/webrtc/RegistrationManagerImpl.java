package org.webrtc;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;

public class RegistrationManagerImpl implements RegistrationManager {
	//implement for RegistrationManager_JNI.cpp
	private long nativePtr = 0;
	@SuppressLint("UseSparseArrays")
	private Map<Integer, Account> AccountMap = new HashMap<Integer, Account>();
	//private native long CreateAccount(long aNativePtr);
	private native int MakeRegister(long nativePtr, AccountConfig accConfig);
	private native void MakeDeRegister(long nativePtr);
	private native void RefreshRegistration(long nativePtr);
	private native void RegisterRegistrationObserver(long nativePtr, RegistrationStateObserver observer);
	private native void DeRegisterRegistrationObserver(long nativePtr);
	private native void SetNetworkReachable(long nativePtr, boolean yesno);
	
	public Account CreateAccount()
	{
	   //long accountPtr = CreateAccount(nativePtr);
	   return new AccountImpl(this);
	}

	public RegistrationManagerImpl(long aNativePtr)
	{
		nativePtr = aNativePtr;
		//RegisterRegistrationObserver(this);
	}
	
	@Override
	public int MakeRegister(AccountConfig accConfig) {
		return MakeRegister(nativePtr, accConfig);
	}

	@Override
	public void MakeDeRegister() {
		//SipProfileImpl sip_profile_impl = (SipProfileImpl)sip_profile;
		MakeDeRegister(nativePtr);
	}

	@Override
	public void RefreshRegistration() {
		//SipProfileImpl sip_profile_impl = (SipProfileImpl)sip_profile;
		RefreshRegistration(nativePtr);
	}

	public void RegisterRegistrationObserver(RegistrationStateObserver observer) {
		RegisterRegistrationObserver(nativePtr, observer);
	}
	
	public void DeRegisterRegistrationObserver() {
		DeRegisterRegistrationObserver(nativePtr);
	}
	
	@Override
	public void SetNetworkReachable(boolean yesno) {
		SetNetworkReachable(nativePtr,yesno);
	}
	
	@Override
	public boolean ProfileIsRegistered()
	{
	
		return true;
	}
	
	
	public void registerAccount(Account account)
	{
		AccountMap.put(account.getAccountId(), account);
	}
	
	public void unregisterAccount(Account account)
	{
		AccountMap.remove(account.getAccountId());
	}
	
	/*@Override
	public void OnRegistrationProgress(int acc_id) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnRegistrationSuccess(int acc_id) {
		// TODO Auto-generated method stub
		System.out.println("=====OnRegistrationSuccess======:" +acc_id);
	}
	
	@Override
	public void OnRegistrationCleared(int acc_id) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void OnRegisterationFailed(int acc_id) {
		// TODO Auto-generated method stub
		
	}*/
}
