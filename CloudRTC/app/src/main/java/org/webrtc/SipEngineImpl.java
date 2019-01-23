package org.webrtc;

import org.webrtc.CallManager;
import org.webrtc.Config;
import org.webrtc.RegistrationManager;
import org.webrtc.SipEngine;

import android.content.Context;

public class SipEngineImpl implements SipEngine {
	private Context mContext=null;
	private long nativePtr = 0;
	private native long Initialize();
	private native boolean Terminate(long nativePtr);
	//private native boolean RunEventLoop(long nativePtr);
	//private native long GetSipProfileManager(long nativePtr);
	private native long GetCallManager(long nativePtr);
	private native long GetRegistrationManager(long nativePtr);
	//private native long GetMediaEngine(long nativePtr);
	//private native long GetDefaultConfig(long nativePtr);
	
	public SipEngineImpl(Context context) {
		mContext = context;
	}
	@Override
	public boolean SipEngineInitialize() {
		if(nativePtr == 0)
		{
			nativePtr = Initialize();
			return (nativePtr != 0);
		}
		return true;
	}
	
	@Override
	public boolean Terminate() {
		return Terminate(nativePtr);
	}
	
	//@Override
	//public boolean RunEventLoop() {
		//return RunEventLoop(nativePtr);
	//}
	//@Override
	//public SipProfileManager GetSipProfileManager() {
	//	long sipProfileManagerPtr = GetSipProfileManager(nativePtr);
		//return new SipProfileManagerImpl(sipProfileManagerPtr);
	//}
	@Override
	public CallManager GetCallManager() {
		long callManagerPtr = GetCallManager(nativePtr);
		return new CallManagerImpl(callManagerPtr, mContext);
	}
	@Override
	public RegistrationManager GetRegistrationManager() {
		long registrationManagerPtr = GetRegistrationManager(nativePtr);
		return new RegistrationManagerImpl(registrationManagerPtr);
	}
	//@Override
	//public MediaEngine GetMediaEngine() {
	//	long mediaEnginePtr = GetMediaEngine(nativePtr);
	//	return new MediaEngineImpl(mediaEnginePtr);
	//}
	//@Override
	//public Config GetDefaultConfig() {
	//	long configPtr = GetDefaultConfig(nativePtr);
	//	if(configPtr == 0)
	//		return null;
	//	return new ConfigImpl(configPtr);
	//}
}
