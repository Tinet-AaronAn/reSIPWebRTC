package org.webrtc;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.webrtc.Call;
import org.webrtc.CallManager;
import org.webrtc.CallStateEventListener;
import org.webrtc.voiceengine.WebRtcAudioManager;

import android.content.Context;
import android.util.Log;

public class CallManagerImpl implements CallManager, CallStateObserver {
	//implement for CallManager_JNI.cpp
	private long nativePtr = 0;
	private Call current_call = null;
	private CallStateEventListener mCallStateEventListener;
	private IncomingCallObserver mIncomingCallObserver;
	private native int CreateCall(long nativePtr, int accId);
	private native void MakeCall(long nativePtr, int accId, int callId, String peerNumber,  String localSdp);
	private native void Accept(long nativePtr, int callId,String localSdp);
	//private native void Accept2(long nativePtr,boolean send_audio, boolean send_video);
	private native void Reject(long nativePtr,int code, String reason);
	private native int Hangup(long nativePtr, int callId);
	private native boolean RegisterCallStateObserver(long nativePtr, CallStateObserver observer);
	private native void DeRegisterCallStateObserver(long nativePtr);
	private static final String TAG = "reSipWebRTC";
	private static final String FIELD_TRIAL_AUTOMATIC_RESIZE =
	   	      "WebRTC-MediaCodecVideoEncoder-AutomaticResize/Enabled/";
   
   private VideoSource videoSource;
   private AudioSource audioSource;
   private CameraVideoCapturer videoCapturer;
   private boolean videoCapturerStopped;
   private Context mContext;

   private boolean videoCallEnabled;
   private boolean preferIsac;
   private String preferredVideoCodec;
   private boolean videoSourceStopped;
   private boolean isError;
   private PeerConnectionParameters peerConnectionParameters;
   PeerConnectionFactory.Options options = null;
   private final ScheduledExecutorService executor;
   
   private PeerConnectionFactory factory;
   
	
	public CallManagerImpl(long aNativePtr, final Context context)
	{
		executor = Executors.newSingleThreadScheduledExecutor();
		nativePtr = aNativePtr;
		mContext = context;
		this.RegisterCallStateObserver(nativePtr, this);
		
		videoCallEnabled = true;
        videoSource = null;
        audioSource = null;
        videoCapturer = null;
        videoCapturerStopped = false;
        options = new PeerConnectionFactory.Options();
        options.disableEncryption = true;
        
		executor.execute(new Runnable() {
		      @Override
		      public void run() {
		  		createPeerConnectionFactoryInternal(mContext);
		      }
		});			
	}

	@Override
   	public PeerConnectionFactory getPeerConnectionFactory()
   	{
   		 return this.factory;
   	}

	@Override
	public Call CreateCall(int accId) {
		//SipProfileImpl profile_impl = (SipProfileImpl)profile;
		int call_id =  CreateCall(nativePtr, accId);
		//if(callPtr!=0)
		current_call = new CallImpl(accId, call_id, this, executor);
		return current_call;
	}

	@Override
	public void RegisterCallStateObserver(CallStateEventListener observer) {
		this.mCallStateEventListener = observer;
	}

	@Override
	public void DeRegisterCallStateObserver() {
		//DeRegisterCallStateObserver(nativePtr);
	}
	
	@Override
	public void MakeCall(int accId, int callId, String peerNumber,  String localSdp)
	{
		MakeCall(nativePtr, accId, callId, peerNumber, localSdp);
	}
	
	
	@Override
	public void Accept(int callId, String localSdp) {
		Accept(nativePtr, callId, localSdp);
	}
	
	@Override
	public void Reject(int code, String reason) {
		Reject(nativePtr, code, reason);
	}
	
	@Override
	public int Hangup(int callId) {
		return Hangup(nativePtr, callId);
	}
	@Override
	public void OnCallOffer(int callId, String offerSdp) {
		// TODO Auto-generated method stub
		current_call.OnCallOffer(offerSdp);
	}
	@Override
	public void OnCallAnswer(int callId, String answerSdp) {
		// TODO Auto-generated method stub
		current_call.OnCallAnswer(answerSdp);
	}
	
	@Override
	 public void registerCall(Call call)
	 {
		current_call = call;
	 }
	
	@Override
	 public void unregisterCall(Call call)
	 {
		current_call = null;
	 }
	
	private void createPeerConnectionFactoryInternal(Context context) {
	      //PeerConnectionFactory.initializeInternalTracer();
	     // if (peerConnectionParameters.tracing) {
	        //  PeerConnectionFactory.startInternalTracingCapture(
	             //     Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
	              //    + "webrtc-trace.txt");
	      //}
	    //Log.e(TAG, "====Create peer connection factory. Use video: " +
	      //  peerConnectionParameters.videoCallEnabled);
	    isError = false;

	    // Initialize field trials.
	    PeerConnectionFactory.initializeFieldTrials(FIELD_TRIAL_AUTOMATIC_RESIZE);

	    // Check preferred video codec.
	   /* preferredVideoCodec = VIDEO_CODEC_VP8;
	    if (videoCallEnabled && peerConnectionParameters.videoCodec != null) {
	      if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_VP9)) {
	        preferredVideoCodec = VIDEO_CODEC_VP9;
	      } else if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_H264)) {
	        preferredVideoCodec = VIDEO_CODEC_H264;
	      }
	    }
	  //  Log.e(TAG, "=============Pereferred video codec===========: " + preferredVideoCodec);

	    // Check if ISAC is used by default.
	    preferIsac = peerConnectionParameters.audioCodec != null
	            && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC);

	    // Enable/disable OpenSL ES playback.
	    if (!peerConnectionParameters.useOpenSLES) {
	      Log.d(TAG, "Disable OpenSL ES audio even if device supports it");
	      WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true);
	    } else {
	      Log.d(TAG, "Allow OpenSL ES audio if device supports it");
	      WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
	    }*/

	    // Create peer connection factory.
	    if (!PeerConnectionFactory.initializeAndroidGlobals(context, true, true, false)) {
	      //events.onPeerConnectionError("Failed to initializeAndroidGlobals");
	    }
	    if (options != null) {
	      Log.d(TAG, "Factory networkIgnoreMask option: " + options.networkIgnoreMask);
	    }
	    factory = new PeerConnectionFactory(options);
	    System.out.println("================createPeerConnectionFactoryInternal========2");
	    Log.d(TAG, "Peer connection factory created.");
	}
	
	private void createCapturer(CameraEnumerator enumerator) {
	    final String[] deviceNames = enumerator.getDeviceNames();

	    // First, try to find front facing camera
	    Logging.d(TAG, "Looking for front facing cameras.");
	    for (String deviceName : deviceNames) {
	      if (enumerator.isFrontFacing(deviceName)) {
	        Logging.d(TAG, "Creating front facing camera capturer.");
	        videoCapturer = enumerator.createCapturer(deviceName, null);

	        if (videoCapturer != null) {
	          return;
	        }
	      }
	    }

	    // Front facing camera not found, try something else
	    Logging.d(TAG, "Looking for other cameras.");
	    for (String deviceName : deviceNames) {
	      if (!enumerator.isFrontFacing(deviceName)) {
	        Logging.d(TAG, "Creating other camera capturer.");
	        videoCapturer = enumerator.createCapturer(deviceName, null);

	        if (videoCapturer != null) {
	          return;
	        }
	      }
	    }
	  }
	
	@Override
	public VideoSource createlocalVideoSource(int width, int height, int framerate)
	{
		if(videoSource != null)
			return videoSource;
		
		 //if (videoCallEnabled) {
		     // if (peerConnectionParameters.useCamera2) {
		      //  if (!peerConnectionParameters.captureToTexture) {
		        //  reportError(context.getString(R.string.camera2_texture_only_error));
		         // return;
		       // }

		       // Logging.d(TAG, "Creating capturer using camera2 API.");
		       // createCapturer(new Camera2Enumerator(mContext));
		      //} else {
		        Logging.d(TAG, "Creating capturer using camera1 API.");
		        createCapturer(new Camera1Enumerator(false));
		      //}

		      if (videoCapturer == null) {
		        reportError("Failed to open camera");
		        //return;
		      }
		 // }
		      
		   //videoSource = factory.createVideoSource(videoCapturer, videoConstraints); 

           videoSource = factory.createVideoSource(videoCapturer); 
           System.out.println("===========createlocalVideoSource=======:" +width +":" +height +":" +framerate);
           videoCapturer.startCapture(width, height, framerate);
           return videoSource; 
	}
	
	 private void reportError(final String errorMessage) {
		    Log.e(TAG, "Peerconnection error: " + errorMessage);
		  //  executor.execute(new Runnable() {
		     // @Override
		     // public void run() {
		        if (!isError) {
		          isError = true;
		        }
		      //}
		    //});
	 }
	 
	@Override
	public AudioSource createlocalAudioSource()
	{
		if(audioSource != null)
			return audioSource;
		
		MediaConstraints audioConstraints = new MediaConstraints();
        audioSource = this.factory.createAudioSource(audioConstraints);
       return audioSource;
	}
	
	@Override
	public void stoplocalMediaSource() {
		// TODO Auto-generated method stub
		if(audioSource != null) {
			audioSource.dispose();
			audioSource = null;
		}
		
		if (videoCapturer != null) {
		      try {
		        videoCapturer.stopCapture();
		      } catch(InterruptedException e) {
		        throw new RuntimeException(e);
		      }
		      videoCapturer.dispose();
		      videoCapturer = null;
		    }
		
		
		if(videoSource != null) {
			videoSource.dispose();
			videoSource = null;
		}	
	}
	
	@Override
    public void RegisterIncomingCallObserver(IncomingCallObserver observer)
    {
		 mIncomingCallObserver = observer;
    }

	@Override
	public void OnCallStateChange(int call_id, int state) {
		// TODO Auto-generated method stub
		System.out.println("==============OnCallStateChange===========:" +state);
		this.current_call.OnCallStateChange(call_id, state);
	}
	
	@Override
	public void OnIncomingCall(int acc_id, int call_id) {
		// TODO Auto-generated method stub
		if(this.mIncomingCallObserver != null) {
			Call call  = new CallImpl(acc_id, call_id, this, executor);
			current_call = call;
			mIncomingCallObserver.OnIncomingCall(call);
		}
	}
	
	@Override
	public void setPeerConnectionParameters(PeerConnectionParameters peerConnectionParameters) {
		// TODO Auto-generated method stub
		this.peerConnectionParameters = peerConnectionParameters;
	}
	
	@Override
   	public PeerConnectionParameters getPeerConnectionParameters()
   	{
   		return peerConnectionParameters;
   	}

}
