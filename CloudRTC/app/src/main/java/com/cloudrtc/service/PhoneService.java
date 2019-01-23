package com.cloudrtc.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cloudrtc.R;
import com.cloudrtc.sdk.CallStreamListener;
import com.cloudrtc.sdk.PeerConnectionStatsReport;
import com.cloudrtc.sdk.SipCallConnectedListener;
import com.cloudrtc.sdk.SipCallDisConnectListener;
import com.cloudrtc.sdk.SipIncomingListener;
import com.cloudrtc.sdk.SipOutgoingListener;
import com.cloudrtc.sdk.SipRegisterListener;
import com.cloudrtc.util.Direction;
import com.cloudrtc.util.LooperExecutor;
import com.cloudrtc.util.RegistrationErrorCode;
import com.cloudrtc.util.RegistrationState;

import org.webrtc.Account;
import org.webrtc.AccountConfig;
import org.webrtc.Call;
import org.webrtc.CallManager;
import org.webrtc.CallState;
import org.webrtc.CallStateEventListener;
import org.webrtc.CallStreamObserver;
import org.webrtc.IncomingCallObserver;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionParameters;
import org.webrtc.RegistrationManager;
import org.webrtc.RegistrationStateObserver;
import org.webrtc.SipEngine;
import org.webrtc.SipEngineFactory;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.Timer;

public class PhoneService extends Service implements RegistrationStateObserver, 
                      CallStateEventListener,  CallStreamObserver,  IncomingCallObserver {
	private final String TAG = PhoneService.class.getSimpleName();
	private static PhoneService the_service_instance_ = null;
	private MediaPlayer mediaPlayer;
	private Account current_account = null;
    private Call current_call = null;
	private String transport_data = null;
    private Direction dir;
    private MediaStream mMediaStream;
    private VideoTrack remoteVideoTrack;
    private RegistrationManager mRegistrationManager = null;
    private CallManager mCallManager = null;
	private LooperExecutor executor;
    private static SipEngine the_sipengine_ = null;
    private PeerConnectionParameters peerConnectionParameters;

    private String keyprefVideoCallEnabled;
    private String keyprefCamera2;
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefCaptureQualitySlider;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefVideoCodec;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefAudioCodec;
    private String keyprefHwCodecAcceleration;
    private String keyprefCaptureToTexture;
    private String keyprefNoAudioProcessingPipeline;
    private String keyprefAecDump;
    private String keyprefOpenSLES;
    private String keyprefDisableBuiltInAec;
    private String keyprefDisableBuiltInAgc;
    private String keyprefDisableBuiltInNs;
    private String keyprefEnableLevelControl;
    private String keyprefDisplayHud;
    private String keyprefTracing;
    
    private VideoRenderer.Callbacks mRemoteRender;
    
    private int callId = -1;
    
	private int _maxVolume = 0; // Android max level (commonly 5)
	private int _volumeLevel = 200;
	private boolean headset_is_plugin_ = false;
	public Handler mHandler = new Handler();
	Timer mTimer = new Timer("SipEngine scheduler");

	public static boolean isready() {
		return (the_service_instance_ != null);
	}

	public static PhoneService instance() {
		if (the_service_instance_ == null) {
			throw new RuntimeException(
					"the_service_instance_ not instanciated yet");
		} else {
			return the_service_instance_;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	//add by david.xu
		private SipRegisterListener mSipRegisterListener;
		private SipCallConnectedListener mSipCallConnectedListener;
		private SipIncomingListener mSipIncomingListener;
		private SipOutgoingListener mSipOutgoingListener;
	    private SipCallDisConnectListener mSipCallDisconnectListener;
	    private PeerConnectionStatsReport mPeerConnectionStatsReport;
	    private CallStreamListener mCallStreamListener;

		public void setSipRegisterListener(SipRegisterListener mListener) {
			this.mSipRegisterListener = mListener;
		}
		
		public void setSipCallConnectedListener(SipCallConnectedListener mListener) {
			this.mSipCallConnectedListener = mListener;
		}
		
		public void setSipIncomingListener(SipIncomingListener mListener) {
			this.mSipIncomingListener = mListener;
		}
		
		public void setSipOutgoingListener(SipOutgoingListener mListener) {
			this.mSipOutgoingListener = mListener;
		}
		
		 public void setSipCallDisConnectListener(SipCallDisConnectListener mListener) {
			 this.mSipCallDisconnectListener = mListener;
	     }

		 public void setPeerConnectionStatsReport(PeerConnectionStatsReport mListener) {
			if(this.current_call != null)
				current_call.setPeerConnectionStatsReport(mListener);
		 }
		 
		 public void setCallStreamListener(CallStreamListener mListener) {
				this.mCallStreamListener = mListener;
		 }
		 
	public boolean MuteMic(boolean yesno)
	{
		return false;
	}

	public boolean HoldCall(boolean yesno)
	{
		return false;
	}
	
	public void SetHeadsetIsPlug(boolean yesno)
	{
		headset_is_plugin_ = yesno;
	}

	public boolean SetLoudspeakerStatus(boolean yesno) {
		boolean speaker_on = yesno;
		if(headset_is_plugin_){
			speaker_on = false;
		}
		//the_sipengine_.GetMediaEngine().GetVoiceEngine().SetLoudspeakerStatus(speaker_on);
		if(audioManager == null)
			audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(speaker_on);
		return true;
	}

	public boolean UpdateCall(boolean video_mode) {
		return false;
	}

	public boolean MakeUrlCall(String url, boolean video_mode) {
		return false;
	}

	public boolean SendDtmf(String tone)
	{
	    return false;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		//Debug.e(TAG, "PhoneService::onCreate");
		 
		the_service_instance_ = this;	
		executor = new LooperExecutor();
        // Looper thread is started once in private ctor and is used for all
        // peer connection API calls to ensure new peer connection factory is
        // created on the same thread as previously destroyed factory.
       executor.requestStart();
       
		mNetWorkBroadCast = new NetWorkBroadCast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mNetWorkBroadCast, filter);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		/*_maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
		if (_maxVolume <= 0) {
			//Debug.i(TAG, "Could not get max volume!");
		} else {
			int androidVolumeLevel = (_volumeLevel * _maxVolume) / 255;
			_volumeLevel = (androidVolumeLevel * 255) / _maxVolume;
			am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, _maxVolume, 0);
		}*/
		 
		am.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
			        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        am.setMicrophoneMute(true);
		am.setSpeakerphoneOn(false);
		
		 if (the_sipengine_ == null)
         {
                 the_sipengine_ = SipEngineFactory.instance().CreateSipEngine(this);

                // loadConfig();

                 the_sipengine_.SipEngineInitialize();
                 
                 mRegistrationManager = the_sipengine_.GetRegistrationManager();
                 mRegistrationManager.RegisterRegistrationObserver(this);
                 mCallManager = the_sipengine_.GetCallManager();
                 mCallManager.RegisterIncomingCallObserver(this);
                 loadConfig();
                 //mCallManager.RegisterCallStateObserver(this);
                // Debug.d("*SipEngine*", "Start CoreEventProgress timer")         
         }

	}

	private NetWorkBroadCast mNetWorkBroadCast;

	public static void startService(Context ctx)
    {
		//Debug.i("PhoneService", "Need start service ="
			//	+ (the_service_instance_ == null));
		Intent intent = new Intent(ctx, PhoneService.class);
		ctx.startService(intent);
	}

	/**
	 * 登陆
	 */
	public void RegisterSipAccount(String username, String password, String server, String transport_type)
	{
    	if(transport_type.equalsIgnoreCase("udp")) {
      	  this.transport_data = "";
        } else if(transport_type.equalsIgnoreCase("tcp")) {
      	  this.transport_data = ";transport=tcp";
        } else if(transport_type.equalsIgnoreCase("tls")) {
      	  this.transport_data = ";transport=tls";
        }
    	
    	String mServer =  server + transport_data;
    	
    	
		if(this.mRegistrationManager != null) {
			current_account = mRegistrationManager.CreateAccount();
			AccountConfig accConfig = new AccountConfig();
			accConfig.username = username;
			accConfig.password = password;
			accConfig.server = mServer;
			current_account.MakeRegister(accConfig);
		}
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//mSipEngine = new SipEngine(this, this, new LooperExecutor());
    }

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		the_service_instance_ = null;
        if (mNetWorkBroadCast != null)
                unregisterReceiver(mNetWorkBroadCast);
	}
	
	public void MakeCall(String number, boolean video_call) 
	{
		if(this.mCallManager != null) {
			current_call = mCallManager.CreateCall(this.current_account.getAccountId());
			current_call.RegisterCallStateObserver(this);
			current_call.MakeCall(number);
		    this.OnNewCall(current_call.GetCallId(), Direction.Outgoing);
		}
	}
	
	public boolean Hangup()
	{		
		if(this.current_call != null) {
			this.current_call.Hangup();
			current_call = null;
		}
		return true;
	}

	public boolean AnswerCall(boolean video_mode) {
    if(current_call != null)
    {
         current_call.Accept();
         return true;
     }
        return false;
     }
	
	private void OnNewCall(int call_id, Direction IncomingOrOutgoing)
	{ 
			if(IncomingOrOutgoing == Direction.Incoming) {

				if(this.mSipIncomingListener != null) {
					//playCallSound(true, false);
					dir = Direction.Incoming;
					mSipIncomingListener.onCallIncoming(call_id);
				}
			} else if(IncomingOrOutgoing == Direction.Outgoing) {	
				if(this.mSipOutgoingListener != null) {
					//startCallingSound(true);
					dir = Direction.Outgoing;
					mSipOutgoingListener.onCallOutgoing(call_id);
				}
			}
	}
	
	public Direction GetCallDirection()
	{
		return dir;
	}
	
	public void startVideoSource() {
	 
	}
	public void stopVideoSource() {}
	
	public void onCameraSwitch() {
		
	}
	
	public void onCaptureFormatChange(int width, int height, int framerate) {}
	
	private MediaPlayer waitMedia = null;

	private void startCallingSound(boolean with_spk) {
	/*	if (waitMedia == null) { // 播放去电等待声音
			waitMedia = UIUtils.phoneWaitSound(this);
		}
		if (the_sipengine_ != null) {
			the_sipengine_.GetMediaEngine().GetVoiceEngine()
					.SetLoudspeakerStatus(with_spk);
		}*/
	}

	private void stopCallingSound() {
		/*if (waitMedia != null) {
			waitMedia.stop();
			waitMedia.release();
			waitMedia = null;
		}*/
	}

	public void playCallSound(boolean isRinging, boolean isShock) {
	/*	if (mediaPlayer == null) {

			if (isRinging) {
				mediaPlayer = UIUtils.phoneSound(getApplicationContext());
				if (mediaPlayer != null) {
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.stop();
						mediaPlayer.reset();
					}
					mediaPlayer.start();
				}
			}
			if (isShock) {
				vib = UIUtils.Vibrate(this, true);
			}
		}*/
	}

	public void OnCallConnected(int call_id) {
	    if(this.mSipCallConnectedListener != null)
	    	mSipCallConnectedListener.onCallConnected(call_id);
	}


	public void OnCallEnded() {
		if(mSipCallDisconnectListener != null)
            mSipCallDisconnectListener.onCallDisConnect(CallState.Hangup.IntgerValue());
	}

	public void OnCallFailed(int status) {
		
	}
	
	class NetWorkBroadCast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			// 获得网络连接服务
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

			NetworkInfo mNetworkInfo = connManager.getActiveNetworkInfo();
			try {
				if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				} else {
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
		
	private final int STOPHOLDSOUND = 89;
	private AudioManager audioManager = null;
	public void playHoldSound() {
		try {
			if (audioManager == null) {
				audioManager = (AudioManager) this
						.getSystemService(Context.AUDIO_SERVICE);
			}
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			//UIUtils.playHoldSound(this);
			Message msg = new Message();
			msg.what = STOPHOLDSOUND;
			//handler.sendMessageDelayed(msg, 2000);
		} catch (Exception e) {
			e.printStackTrace();
			audioManager.setMode(AudioManager.MODE_NORMAL);
		}
	}
	
	/**
	 * 判断是否有系统来电
	 */
	public static boolean isSystemCalling = false;
	
	public boolean InCalling() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean InVideoCalling() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTvBoxMode() {
		// TODO Auto-generated method stub
		return false;
	}

	public void refreshRegistration() {
		// TODO Auto-generated method stub
		
	}

	public RegistrationState LastRegistrationState() {
		// TODO Auto-generated method stub
		return null;
	}

	public RegistrationErrorCode LastRegistrationErrorCode() {
		// TODO Auto-generated method stub
		return null;
	}

	public String SendTextMessage(String targetNum, String strMsg) {
		// TODO Auto-generated method stub
		return null;
	}

	public void enableStatsEvents(boolean enable, int periodMs) {
		if(this.current_call != null)
			current_call.enableStatsEvents(enable, periodMs);
	}

	public void StartVideoRender( 
			  final VideoRenderer.Callbacks localRender,
		      final VideoRenderer.Callbacks remoteRender) {
		System.out.println("==============StartVideoRender==============");
		if(this.current_call != null)
			  this.current_call.StartVideoRender(remoteRender);
		this.mRemoteRender = localRender;
	//	else if(callId > 0)
		 // this.current_call.StartVideoRender(localRender);
	}

	public void startMediaStream(SurfaceViewRenderer mRemoteRender) {
		if(this.current_call != null)
		  this.current_call.StartVideoRender(mRemoteRender);	
	}

	@Override
	public void OnCallStateChange(int call_id, int state_code) {
		// TODO Auto-generated method stub
		CallState state = CallState.fromInt(state_code);
		System.out.println("===========call====state_code===========:" +state_code);
		if (state.IntgerValue() == CallState.NewCall.IntgerValue())
        {
			  if(current_call != null)
              {
				 // current_call.Reject(486, "Busy Here");
                 // return;
              }
        }
		
		if (state.IntgerValue() == CallState.Hangup.IntgerValue()) {
			if(this.current_call != null) {
				current_call.close();
				current_call = null;
				this.OnCallEnded();
			}
            //current_call = call;
            //if(call.GetSupportVideo())
            //{
              //      call.GetMediaStream().GetVideoStream().RegisterVideoStreamObserver(this);
            //}

            //OnNewCall(Direction.Outgoing);
      }	
		
	 if (state.IntgerValue() == CallState.Answered.IntgerValue()) {
             OnCallConnected(this.current_call.GetCallId());
     } 
	}

	@Override
	public void OnRegistrationProgress(int acc_id) {
		// TODO Auto-generated method stub
		mSipRegisterListener.OnRegistrationProgress(acc_id);
	}

	@Override
	public void OnRegistrationSuccess(int acc_id) {
		// TODO Auto-generated method stub
		mSipRegisterListener.OnRegistrationSuccess(acc_id);
	}

	@Override
	public void OnRegistrationCleared(int acc_id) {
		// TODO Auto-generated method stub
		mSipRegisterListener.OnRegistrationCleared(acc_id);
	}

	@Override
	public void OnRegistrationFailed(int acc_id) {
		// TODO Auto-generated method stub
		mSipRegisterListener.OnRegisterationFailed(acc_id, 408, "");
	}

	@Override
	public void onAddStream(int callId, MediaStream stream) {
		// TODO Auto-generated method stub
		//if(this.mCallStreamListener != null)
			//mCallStreamListener.onAddStream(callId, stream);
	}

	@Override
	public void onRemoveStream(MediaStream stream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnIncomingCall(Call call) {
		// TODO Auto-generated method stub
		System.out.println("=================OnIncomingCall=============");
		if(current_call != null) {
			call.Reject(408, "busy");
			return;
		}
		current_call = call;
		current_call.RegisterCallStateObserver(this);
		current_call.RegisterCallStreamObserver(this);
		 this.OnNewCall(current_call.GetCallId(), Direction.Incoming);
	}
	
	public void loadConfig()
	{
	   SharedPreferences sharedPref;
	    sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
	    keyprefVideoCallEnabled = getString(R.string.pref_videocall_key);
	    keyprefCamera2 = getString(R.string.pref_camera2_key);
	    keyprefResolution = getString(R.string.pref_resolution_key);
	    keyprefFps = getString(R.string.pref_fps_key);
	    keyprefCaptureQualitySlider = getString(R.string.pref_capturequalityslider_key);
	    keyprefVideoBitrateType = getString(R.string.pref_startvideobitrate_key);
	    keyprefVideoBitrateValue = getString(R.string.pref_startvideobitratevalue_key);
	    keyprefVideoCodec = getString(R.string.pref_videocodec_key);
	    keyprefHwCodecAcceleration = getString(R.string.pref_hwcodec_key);
	    keyprefCaptureToTexture = getString(R.string.pref_capturetotexture_key);
	    keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
	    keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
	    keyprefAudioCodec = getString(R.string.pref_audiocodec_key);
	    keyprefNoAudioProcessingPipeline = getString(R.string.pref_noaudioprocessing_key);
	    keyprefAecDump = getString(R.string.pref_aecdump_key);
	    keyprefOpenSLES = getString(R.string.pref_opensles_key);
	    keyprefDisableBuiltInAec = getString(R.string.pref_disable_built_in_aec_key);
	    keyprefDisableBuiltInAgc = getString(R.string.pref_disable_built_in_agc_key);
	    keyprefDisableBuiltInNs = getString(R.string.pref_disable_built_in_ns_key);
	    keyprefEnableLevelControl = getString(R.string.pref_enable_level_control_key);
	    keyprefDisplayHud = getString(R.string.pref_displayhud_key);
	    keyprefTracing = getString(R.string.pref_tracing_key);
	    
		// Video call enabled flag.
	    boolean videoCallEnabled = sharedPref.getBoolean(keyprefVideoCallEnabled,
	        Boolean.valueOf(getString(R.string.pref_videocall_default)));

	    // Use Camera2 option.
	    boolean useCamera2 = sharedPref.getBoolean(keyprefCamera2,
	        Boolean.valueOf(getString(R.string.pref_camera2_default)));

	    // Get default codecs.
	    String videoCodec = sharedPref.getString(keyprefVideoCodec,
	        getString(R.string.pref_videocodec_default));
	    String audioCodec = sharedPref.getString(keyprefAudioCodec,
	        getString(R.string.pref_audiocodec_default));

	    // Check HW codec flag.
	    boolean hwCodec = sharedPref.getBoolean(keyprefHwCodecAcceleration,
	        Boolean.valueOf(getString(R.string.pref_hwcodec_default)));

	    // Check Capture to texture.
	    boolean captureToTexture = sharedPref.getBoolean(keyprefCaptureToTexture,
	        Boolean.valueOf(getString(R.string.pref_capturetotexture_default)));

	    // Check Disable Audio Processing flag.
	    boolean noAudioProcessing = sharedPref.getBoolean(
	        keyprefNoAudioProcessingPipeline,
	        Boolean.valueOf(getString(R.string.pref_noaudioprocessing_default)));

	    // Check Disable Audio Processing flag.
	    boolean aecDump = sharedPref.getBoolean(
	        keyprefAecDump,
	        Boolean.valueOf(getString(R.string.pref_aecdump_default)));

	    // Check OpenSL ES enabled flag.
	    boolean useOpenSLES = sharedPref.getBoolean(
	        keyprefOpenSLES,
	        Boolean.valueOf(getString(R.string.pref_opensles_default)));

	    // Check Disable built-in AEC flag.
	    boolean disableBuiltInAEC = sharedPref.getBoolean(
	        keyprefDisableBuiltInAec,
	        Boolean.valueOf(getString(R.string.pref_disable_built_in_aec_default)));

	    // Check Disable built-in AGC flag.
	    boolean disableBuiltInAGC = sharedPref.getBoolean(
	        keyprefDisableBuiltInAgc,
	        Boolean.valueOf(getString(R.string.pref_disable_built_in_agc_default)));

	    // Check Disable built-in NS flag.
	    boolean disableBuiltInNS = sharedPref.getBoolean(
	        keyprefDisableBuiltInNs,
	        Boolean.valueOf(getString(R.string.pref_disable_built_in_ns_default)));

	    // Check Enable level control.
	    boolean enableLevelControl = sharedPref.getBoolean(
	        keyprefEnableLevelControl,
	        Boolean.valueOf(getString(R.string.pref_enable_level_control_key)));

	    // Get video resolution from settings.
	    int videoWidth = 0;
	    int videoHeight = 0;
	    String resolution = sharedPref.getString(keyprefResolution,
	        getString(R.string.pref_resolution_default));
	    String[] dimensions = resolution.split("[ x]+");
	    if (dimensions.length == 2) {
	      try {
	        videoWidth = Integer.parseInt(dimensions[0]);
	        videoHeight = Integer.parseInt(dimensions[1]);
	      } catch (NumberFormatException e) {
	        videoWidth = 0;
	        videoHeight = 0;
	        Log.e(TAG, "Wrong video resolution setting: " + resolution);
	      }
	    }

	    // Get camera fps from settings.
	    int cameraFps = 0;
	    String fps = sharedPref.getString(keyprefFps,
	        getString(R.string.pref_fps_default));
	    String[] fpsValues = fps.split("[ x]+");
	    if (fpsValues.length == 2) {
	      try {
	        cameraFps = Integer.parseInt(fpsValues[0]);
	      } catch (NumberFormatException e) {
	        Log.e(TAG, "Wrong camera fps setting: " + fps);
	      }
	    }

	    // Check capture quality slider flag.
	    boolean captureQualitySlider = sharedPref.getBoolean(keyprefCaptureQualitySlider,
	        Boolean.valueOf(getString(R.string.pref_capturequalityslider_default)));

	    // Get video and audio start bitrate.
	    int videoStartBitrate = 0;
	    String bitrateTypeDefault = getString(
	        R.string.pref_startvideobitrate_default);
	    String bitrateType = sharedPref.getString(
	        keyprefVideoBitrateType, bitrateTypeDefault);
	    if (!bitrateType.equals(bitrateTypeDefault)) {
	      String bitrateValue = sharedPref.getString(keyprefVideoBitrateValue,
	          getString(R.string.pref_startvideobitratevalue_default));
	      videoStartBitrate = Integer.parseInt(bitrateValue);
	    }
	    int audioStartBitrate = 0;
	    bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
	    bitrateType = sharedPref.getString(
	        keyprefAudioBitrateType, bitrateTypeDefault);
	    if (!bitrateType.equals(bitrateTypeDefault)) {
	      String bitrateValue = sharedPref.getString(keyprefAudioBitrateValue,
	          getString(R.string.pref_startaudiobitratevalue_default));
	      audioStartBitrate = Integer.parseInt(bitrateValue);
	    }

	    // Check statistics display option.
	    boolean displayHud = sharedPref.getBoolean(keyprefDisplayHud,
	        Boolean.valueOf(getString(R.string.pref_displayhud_default)));

	    boolean tracing = sharedPref.getBoolean(
	            keyprefTracing, Boolean.valueOf(getString(R.string.pref_tracing_default)));

	    peerConnectionParameters = new PeerConnectionParameters(
	    		videoCallEnabled,
	    		tracing,
	            useCamera2,
	            videoWidth,
	            videoHeight,
	            cameraFps,
	            videoStartBitrate,
	            videoCodec,
	            hwCodec,
	            captureToTexture,
	            audioStartBitrate,
	            audioCodec,
	            noAudioProcessing,
	            aecDump,
	            useOpenSLES,
	            disableBuiltInAEC,
	            disableBuiltInAGC,
	            disableBuiltInNS,
	            enableLevelControl);
	    this.mCallManager.setPeerConnectionParameters(peerConnectionParameters);
	}
}
