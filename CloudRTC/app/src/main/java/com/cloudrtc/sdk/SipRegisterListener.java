package com.cloudrtc.sdk;

public interface SipRegisterListener {
	    //虚方法回调
		//注册/注销通知
	public void OnRegistrationProgress(int acc_id);
	public void OnRegistrationSuccess(int acc_id);
	public void OnRegisterationFailed(int acc_id, int code, String reason);
	public void OnRegistrationCleared(int acc_id);	
}
