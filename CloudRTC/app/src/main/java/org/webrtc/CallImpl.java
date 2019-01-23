package org.webrtc;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.cloudrtc.sdk.PeerConnectionStatsReport;

import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.SessionDescription.Type;
import org.webrtc.VideoRenderer.Callbacks;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallImpl implements Call {
	
	public CallImpl(int accId, int callId, CallManager callManager, ScheduledExecutorService executor)  {
		//nativePtr = aNativePtr;
		this.accId = accId;
		this.callId = callId;
		mCallManager = callManager;	
		
	    videoCallEnabled = true;
        // Reset variables to initial states.
        peerConnection = null;
        preferIsac = false;
        videoSourceStopped = false;
        isError = false;
        localSdp = null; // either offer or answer SDP
        mediaStream = null;
        renderVideo = true;
        localVideoTrack = null;
        remoteVideoTrack = null;
        enableAudio = true;
        
        peerConnectionParameters = mCallManager.getPeerConnectionParameters();
        
        this.executor = executor;
        statsTimer = new Timer();
        
	}
	
	private int accId;
	private int callId;
	private CallManager mCallManager;
    private static final String TAG = "reSipWebRTC";
    private MediaStream mStream = null;
    private boolean isConnected = false;
    private PeerConnectionStatsReport mPeerConnectionStatsReport;
    private PeerConnectionParameters peerConnectionParameters;

	public static final String VIDEO_TRACK_ID = "ARDAMSv0";
	public static final String AUDIO_TRACK_ID = "ARDAMSa0";
	private static final String FIELD_TRIAL_AUTOMATIC_RESIZE =
	      "WebRTC-MediaCodecVideoEncoder-AutomaticResize/Enabled/";
	private static final String VIDEO_CODEC_VP8 = "VP8";
	  private static final String VIDEO_CODEC_VP9 = "VP9";
	  private static final String VIDEO_CODEC_H264 = "H264";
	  private static final String AUDIO_CODEC_OPUS = "opus";
	  private static final String AUDIO_CODEC_ISAC = "ISAC";
	  private static final String AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl";
	  private static final String VIDEO_CODEC_PARAM_START_BITRATE =
	      "x-google-start-bitrate";
	  private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
	  private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
	  private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT= "googAutoGainControl";
	  private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT  = "googHighpassFilter";
	  private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
	  private static final String MAX_VIDEO_WIDTH_CONSTRAINT = "maxWidth";
	  private static final String MIN_VIDEO_WIDTH_CONSTRAINT = "minWidth";
	  private static final String MAX_VIDEO_HEIGHT_CONSTRAINT = "maxHeight";
	  private static final String MIN_VIDEO_HEIGHT_CONSTRAINT = "minHeight";
	  private static final String MAX_VIDEO_FPS_CONSTRAINT = "maxFrameRate";
	  private static final String MIN_VIDEO_FPS_CONSTRAINT = "minFrameRate";
	  private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
	  private static final int HD_VIDEO_WIDTH = 1280;
	  private static final int HD_VIDEO_HEIGHT = 720;
	  private static final int MAX_VIDEO_WIDTH = 1280;
	  private static final int MAX_VIDEO_HEIGHT = 1280;
	  private static final int MAX_VIDEO_FPS = 30;
	  private boolean videoCallEnabled;
	  private boolean MediaStreamReady = false;
	  private Timer statsTimer;
	  private boolean enableAudio;
	  private int videoWidth;
	  private int videoHeight;
	  private int videoFps;
	  
	  private ScheduledExecutorService executor;
	  private PeerConnection peerConnection;
		private MediaConstraints pcConstraints;
		private MediaConstraints videoConstraints;
		private MediaConstraints audioConstraints;
		private ParcelFileDescriptor aecDumpFileDescriptor;
		private MediaConstraints sdpMediaConstraints;
		private VideoSource videoSource;
		private boolean preferIsac;
		private String preferredVideoCodec;
		private boolean videoSourceStopped;
		private boolean isError;
		private final PCObserver pcObserver = new PCObserver();
		private final SDPObserver sdpObserver = new SDPObserver();
		
		private VideoRenderer.Callbacks localRender;
	    private VideoRenderer.Callbacks remoteRender;
	    private VideoTrack localVideoTrack;
	    private VideoTrack remoteVideoTrack;
	    private AudioTrack localAudioTrack;
	    
	    private boolean isInitiator;
	    private SessionDescription localSdp; // either offer or answer SDP
	    private String localStringSdp;
	    private MediaStream mediaStream;
	    private int numberOfCameras;
	    // enableVideo is set to true if video should be rendered and sent.
	    private boolean renderVideo;
		private String RemoteSdp = null;

		private String peerNumber;
		
		/**
		   * The default max video height of video.
		   */
		  private static final String DEFAULT_MAX_VIDEO_H = "240";
		  /**
		   * The default max video width of video.
		   */
		  private static final String DEFAULT_MAX_VIDEO_W = "320";
		  private static final String DEFAULT_STUNSERVER = "stun:120.25.211.29:3478";
		  /**
		   * The default turn server(pa server).
		   */
		  private static final String DEFAULT_TURN_URI = "turn:120.25.211.29:3478";
		  /**
		   * The default user name of turn server(pa server).
		   */
		  private static final String DEFAULT_TURN_USERNAME = "700";
		  /**
		   * The default password of turn server(pa server).
		   */
		  private static final String DEFAULT_TURN_PASSWORD = "700";
		  /**
		   * The max video height of video.
		   */
		  public static String mMaxVideoHeight = DEFAULT_MAX_VIDEO_H;
		  /**
		   * The max video width of video.
		   */
		  public static String mMaxVideoWidth = DEFAULT_MAX_VIDEO_W;
		  /**
		   * The stun server (STUN protocol).
		   */
		  public static String mStunServer = DEFAULT_STUNSERVER;
		  /**
		   * The uri of turn server (TURN protocol).
		   */
		  public static String mTurnUri = DEFAULT_TURN_URI;
		  /**
		   * The user name of turn server (TURN protocol).
		   */
		  public static String mTurnUser = DEFAULT_TURN_USERNAME;
		  /**
		   * The password of turn server (TURN protocol).
		   */
		  public static String mTurnPassword = DEFAULT_TURN_PASSWORD;
		  
		  private CallStateEventListener mCallStateEventListener = null;
		  private CallStreamObserver mCallStreamObserver = null;

	@Override
	public void RegisterCallStateObserver(CallStateEventListener observer) {
		this.mCallStateEventListener = observer;
	}

	@Override
	public void RegisterCallStreamObserver(CallStreamObserver Observer) {
        this.mCallStreamObserver = Observer;
    }

	@Override
	public void StartVideoRender(final Callbacks remoteRender)
	{
		executor.execute(new Runnable() {
            @Override
             public void run() {
                System.out.println("=============StartVideoRender==1========");
                if(mStream != null) {
                  if (mStream.videoTracks.size() == 1) {
                         remoteVideoTrack = mStream.videoTracks.get(0);
                           //add by david.xu
                         if(remoteRender != null) {
                             System.out.println("=============StartVideoRender==2========");
                           remoteVideoTrack.setEnabled(renderVideo);
                           remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
                         }
                     }
               } else {
                       System.out.println("=============mStream==NULL========");
               }
           }
        });

	}
		  
	@Override
	public void MakeCall(String peerNumber)
	{
		this.peerNumber = peerNumber;
		this.createPeerConnection(null);
		this.createOffer();	
	}
	
	
	@Override
	public void Accept() {
		
		this.createPeerConnection(null);
    	System.out.println("====================Accept========:" +RemoteSdp);
    	this.setRemoteDescription(this.RemoteSdp, SessionDescription.Type.OFFER);
		this.createAnswer();
	}
	
	@Override
	public void Reject(int code, String reason) {
		if(this.mCallManager != null)
			mCallManager.Reject(code, reason);
	}
	
	@Override
	public int Hangup() {
		executor.execute(new Runnable() {
		    @Override
		  public void run() {
		        closeInternal();
		      }
		 });
		if(this.mCallManager != null)
			mCallManager.Hangup(this.callId);
		return 0;
	}
	
	@Override
	public int GetCallId()
	{
		return this.callId;
	}
	
	@Override
	public void OnCallOffer(String offerSdp)
	{
		RemoteSdp = offerSdp;
		//RemoteSdp = this.changeCandidate(offerSdp);
		System.out.println("=========OnCallOffer======:\n" +RemoteSdp);
	}
	
	
	@Override
	public void OnCallAnswer(String answerSdp)
	{
		RemoteSdp = answerSdp;
		//RemoteSdp = this.changeCandidate(answerSdp);
		this.setRemoteDescription(this.RemoteSdp, SessionDescription.Type.ANSWER);
	}
	
	private String changeCandidate(String sdpDescription) {
		// TODO Auto-generated method stub
		String[] lines = sdpDescription.split("\r\n");
	    int mLineIndex = -1;
	    String mediaPort = null;
	    StringBuilder newSdpDescription = new StringBuilder();
		StringBuilder newCandidateDescription = new StringBuilder();
		
	    String mediaDescription = "m=video ";
	    if (false) {
	      mediaDescription = "m=audio ";
	    }
	    
	    for (int i = 0; (i < lines.length)
	        && (mLineIndex == -1); i++) {
		     newSdpDescription.append(lines[i]).append("\r\n");
	      if (lines[i].startsWith(mediaDescription)) {
	        mLineIndex = i;
	        continue;
	      }
	    }
	    
	    if (mLineIndex == -1) {
		      Log.w(TAG, "No " + mediaDescription + " line, so can't prefer ");
		   return sdpDescription;
		} else {
			String[] mediaDescriptions =  lines[mLineIndex].split(" ");
			mediaPort = mediaDescriptions[1];

		    for (int i = mLineIndex+1; (i < lines.length); i++) {
		    	if(lines[i].indexOf("candidate:") != -1)
		    	{
		    		mLineIndex = i;
		    		String candidate = lines[i];
		    		String[] candidateDescription =  lines[mLineIndex].split(" ");
		    		candidateDescription[4] = "120.76.225.49";
		    		candidateDescription[5] = mediaPort;
					for(int j = 0; j < candidateDescription.length; j++)
						newCandidateDescription.append(candidateDescription[j]).append(" ");
					System.out.println("======mediaDescriptions===========:" +newCandidateDescription.toString());
		    	}
		    	if(mLineIndex != i) {
	    			newSdpDescription.append(lines[i]).append("\r\n");
	    		} else {
	    			newSdpDescription.append(newCandidateDescription.toString()).append("\r\n");
	    		}
		    }
		    
		}
	    
	    mLineIndex = -1;
	    

	   /* for (int i = 0; (i < lines.length); i++) {
	    	if(lines[i].indexOf("candidate:") != -1)
	    	{
	    		mLineIndex = i;
	    		String candidate = lines[i];
	    		String[] candidateDescription =  lines[mLineIndex].split(" ");
	    		candidateDescription[4] = "120.76.225.49";
	    		candidateDescription[5] = mediaPort;
				//mediaPort = mediaDescriptions[1];
				for(int j = 0; j < candidateDescription.length; j++)
					newCandidateDescription.append(candidateDescription[j]).append(" ");
				System.out.println("======mediaDescriptions===========:" +newCandidateDescription.toString());
	    	}
	    	if(mLineIndex != i) {
    			newSdpDescription.append(lines[i]).append("\r\n");
    		} else {
    			newSdpDescription.append(newCandidateDescription.toString()).append("\r\n");
    		}
	    }*/
		System.out.println("======newSdpDescription===========:" +newSdpDescription.toString());

		return newSdpDescription.toString();
	}

	
	//@Override
	//public int UpdateCall(boolean enable_video) {
	//	return UpdateCall(nativePtr,enable_video);
	//}
	//@Override
	//public int Hold() {
	//	return Hold(nativePtr);
	//}
	//@Override
	//public int UnHold() {
	//	return UnHold(nativePtr);
	//}
	//@Override
	//public int SendDtmf(DtmfMethod dtmf_method, String tone,
	//		boolean play_dtmf_tone) {
	//	return SendDtmf(nativePtr,dtmf_method.IntgerValue(),tone,play_dtmf_tone);
	//}
	//@Override
	//public String GetCallerId() {
	//	return GetCallerId(nativePtr);
	//}
	//@Override
	//public Direction GetDirection() {
	//	int dir_int = GetDirection(nativePtr);
	//	return (dir_int == Direction.Incoming.IntgerValue())
		//		? Direction.Incoming : Direction.Outgoing;
	//}
	/*@Override
	public CallState GetCallState() {
		int state_int = GetCallState(nativePtr);
		
		if(state_int == CallState.NewCall.IntgerValue())
			return CallState.NewCall;
		else if(state_int == CallState.Cancel.IntgerValue())
			return CallState.Cancel;
		else if(state_int == CallState.Failed.IntgerValue())
			return CallState.Failed;
		else if(state_int == CallState.Rejected.IntgerValue())
			return CallState.Rejected;
		else if(state_int == CallState.EarlyMedia.IntgerValue())
			return CallState.EarlyMedia;
		else if(state_int == CallState.Ringing.IntgerValue())
			return CallState.Ringing;
		else if(state_int == CallState.Answered.IntgerValue())
			return CallState.Answered;
		else if(state_int == CallState.Hangup.IntgerValue())
			return CallState.Hangup;
		else if(state_int == CallState.Pausing.IntgerValue())
			return CallState.Pausing;
		else if(state_int == CallState.Paused.IntgerValue())
			return CallState.Paused;
		else if(state_int == CallState.Resuming.IntgerValue())
			return CallState.Resuming;
		else if(state_int == CallState.Resumed.IntgerValue())
			return CallState.Resumed;
		else if(state_int == CallState.Updating.IntgerValue())
			return CallState.Updating;
		else if(state_int == CallState.Updated.IntgerValue())
			return CallState.Updated;
		
		return CallState.Unknown;
	}
	@Override
	public boolean GetSupportVideo() {
		return GetSupportVideo(nativePtr);
	}
	@Override
	public boolean GetSupportData() {
		return GetSupportVideo(nativePtr);
	}
	//@Override
	//public SipProfile GetProfile() {
	//	long sipProfilePtr = GetProfile(nativePtr);
		//return new SipProfileImpl(sipProfilePtr);
	//}
	//@Override
	//public MediaStream GetMediaStream() {
	//	long mediaStreamPtr = GetMediaStream(nativePtr);
	//	return new MediaStreamImpl(mediaStreamPtr);
	//}
	@Override
	public int GetErrorCode() {
		return GetErrorCode(nativePtr);
	}
	@Override
	public String GetErrorReason() {
		return GetErrorReason(nativePtr);
	}
	@Override
	public CallReport GetCallReport() {
		long callReportPtr = GetCallReport(nativePtr);
		return new CallReportImpl(callReportPtr);
	}
	@Override
	public String CallStateName(CallState state) {
		return CallStateName(nativePtr, state.IntgerValue());
	}
	@Override
	public String GetUniqueId() {
		return GetUniqueId(nativePtr);
	}
	@Override
	public void Accept(boolean send_audio, boolean send_video) {
		Accept2(nativePtr,send_audio,send_video);
	}*/
	

	private void createPeerConnection(
		      final EglBase.Context renderEGLContext) {
		    //if (peerConnectionParameters == null) {
		     // Log.e(TAG, "Creating peer connection without initializing factory.");
		      //return;
		    //}

		   // statsTimer = new Timer();
		    executor.execute(new Runnable() {
		      @Override
		      public void run() {
		        createMediaConstraintsInternal();
		        createPeerConnectionInternal(renderEGLContext);
		      }
		    });
    }
	
	private void createMediaConstraintsInternal() {
	    // Create peer connection constraints.
		 pcConstraints = new MediaConstraints();
		    // Enable DTLS for normal calls and disable for loopback calls.
	
		 pcConstraints.optional.add(
		    new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "false"));
		 
		 
		    
		    // Check if there is a camera on device and disable video call if not.
		    numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
		    if (numberOfCameras == 0) {
		      Log.w(TAG, "No camera on device. Switch to audio only call.");
		      videoCallEnabled = false;
		    }
		    // Create video constraints if video call is enabled.
		    if (videoCallEnabled) {
		      videoWidth = peerConnectionParameters.videoWidth;
		      videoHeight = peerConnectionParameters.videoHeight;
		      videoFps = peerConnectionParameters.videoFps;

		      // If video resolution is not specified, default to HD.
		      if (videoWidth == 0 || videoHeight == 0) {
		        videoWidth = HD_VIDEO_WIDTH;
		        videoHeight = HD_VIDEO_HEIGHT;
		      }

		      // If fps is not specified, default to 30.
		      if (videoFps == 0) {
		        videoFps = 30;
		      }

		      videoWidth = Math.min(videoWidth, MAX_VIDEO_WIDTH);
		      videoHeight = Math.min(videoHeight, MAX_VIDEO_HEIGHT);
		      videoFps = Math.min(videoFps, MAX_VIDEO_FPS);
		    }

		    // Create audio constraints.
		    audioConstraints = new MediaConstraints();
		    // added for audio performance measurements
		    if (peerConnectionParameters.noAudioProcessing) {
		      Log.d(TAG, "Disabling audio processing");
		      audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
		            AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
		      audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
		            AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
		      audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
		            AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
		      audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
		           AUDIO_NOISE_SUPPRESSION_CONSTRAINT , "false"));
		    }
		    if (peerConnectionParameters.enableLevelControl) {
		      Log.d(TAG, "Enabling level control.");
		      audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
		          AUDIO_LEVEL_CONTROL_CONSTRAINT, "true"));
		    }
		    // Create SDP constraints.
		    sdpMediaConstraints = new MediaConstraints();
		    sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
		        "OfferToReceiveAudio", "true"));
		    if (videoCallEnabled) {
		      sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
		          "OfferToReceiveVideo", "true"));
		    } else {
		      sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
		          "OfferToReceiveVideo", "false"));
		    }
		    
		    sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
			          "googUseRtpMUX", "false"));
	  }

	  private void createPeerConnectionInternal(EglBase.Context renderEGLContext) {
	    if (mCallManager.getPeerConnectionFactory() == null || isError) {
	      Log.e(TAG, "Peerconnection factory is not created");
	      return;
	    }
	    Log.d(TAG, "Create peer connection.");

	    Log.d(TAG, "PCConstraints: " + pcConstraints.toString());
	    if (videoConstraints != null) {
	      Log.d(TAG, "VideoConstraints: " + videoConstraints.toString());
	    }

	    //if (videoCallEnabled) {
	      //Log.d(TAG, "EGLContext: " + renderEGLContext);
	      //mCallManager.getPeerConnectionFactory().setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
	   // }

	    // Check preferred video codec.
	    preferredVideoCodec = VIDEO_CODEC_VP8;
	    if (videoCallEnabled && peerConnectionParameters.videoCodec != null) {
	      if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_VP9)) {
	        preferredVideoCodec = VIDEO_CODEC_VP9;
	      } else if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_H264)) {
	        preferredVideoCodec = VIDEO_CODEC_H264;
	      }
	    }
	    Log.d(TAG, "Pereferred video codec: " + preferredVideoCodec);

	    // Check if ISAC is used by default.
	    preferIsac = peerConnectionParameters.audioCodec != null
	        && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC);

	    // Enable/disable OpenSL ES playback.
	    if (!peerConnectionParameters.useOpenSLES) {
	      Log.d(TAG, "Disable OpenSL ES audio even if device supports it");
	      WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true /* enable */);
	    } else {
	      Log.d(TAG, "Allow OpenSL ES audio if device supports it");
	      WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
	    }

	    if (peerConnectionParameters.disableBuiltInAEC) {
	      Log.d(TAG, "Disable built-in AEC even if device supports it");
	      WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
	    } else {
	      Log.d(TAG, "Enable built-in AEC if device supports it");
	      WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false);
	    }

	    if (peerConnectionParameters.disableBuiltInAGC) {
	      Log.d(TAG, "Disable built-in AGC even if device supports it");
	      WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true);
	    } else {
	      Log.d(TAG, "Enable built-in AGC if device supports it");
	      WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(false);
	    }

	    if (peerConnectionParameters.disableBuiltInNS) {
	      Log.d(TAG, "Disable built-in NS even if device supports it");
	      WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
	    } else {
	      Log.d(TAG, "Enable built-in NS if device supports it");
	      WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(false);
	    }

	    //add by david.xu
	    LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
	    PeerConnection.IceServer stunServer = new PeerConnection.IceServer(
	                    mStunServer);
	    PeerConnection.IceServer turnIceServer = new PeerConnection.IceServer(
	    		 mTurnUri, mTurnUser, mTurnPassword);
	    //iceServers.add(stunServer);
	    //iceServers.add(turnIceServer);
	    
	    PeerConnection.RTCConfiguration rtcConfig =
	        new PeerConnection.RTCConfiguration(iceServers);
	    // TCP candidates are only useful when connecting to a server that supports
	    // ICE-TCP.
	    //add by david.xu
	    //rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.RELAY;
	    rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
	    rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.BALANCED;
	    rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
	    //rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
	    //rtcConfig.keyType = PeerConnection.KeyType.RSA;

	    peerConnection = mCallManager.getPeerConnectionFactory().createPeerConnection(
	        rtcConfig, pcConstraints, pcObserver);
	    isInitiator = false;

	    // Set default WebRTC tracing and INFO libjingle logging.
	    // NOTE: this _must_ happen while |factory| is alive!
	    Logging.enableTracing(
	        "logcat:",
	        EnumSet.of(Logging.TraceLevel.TRACE_DEFAULT));
	    
	    mediaStream = mCallManager.getPeerConnectionFactory().createLocalMediaStream("ARDAMS");
	    if (videoCallEnabled) {
	      mediaStream.addTrack(createVideoTrack());
	    }

	   mediaStream.addTrack(createAudioTrack());

	    peerConnection.addStream(mediaStream);

	  }
	  
	  private void setRemoteDescription(final String sdp, final Type type) {
		    executor.execute(new Runnable() {
		      @Override
		      public void run() {
		        if (peerConnection == null || isError) {
		          return;
		        }
		      SessionDescription mRemoteSdp = 
		    		  new SessionDescription(type, sdp);

		        String sdpDescription = mRemoteSdp.description;
		       // if (preferIsac) {
		         // sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
		        //}
		        //if (videoCallEnabled) {
		        //  sdpDescription = preferCodec(sdpDescription, preferredVideoCodec, false);
		       // }
		       if (videoCallEnabled) {
		        //  sdpDescription = setStartBitrate(VIDEO_CODEC_VP8, true,
		          //    sdpDescription, 1000);
		        //  sdpDescription = setStartBitrate(VIDEO_CODEC_VP9, true,
		          //    sdpDescription, peerConnectionParameters.videoStartBitrate);
		         // sdpDescription = setStartBitrate(VIDEO_CODEC_H264, true,
		           //   sdpDescription, peerConnectionParameters.videoStartBitrate);
		        }
		        //if (peerConnectionParameters.audioStartBitrate > 0) {
		        //  sdpDescription = setStartBitrate(AUDIO_CODEC_OPUS, false,
		          //    sdpDescription, 32);
		       // }
		        Log.d(TAG, "Set remote SDP====:" +mRemoteSdp.description);
		        SessionDescription sdpRemote = new SessionDescription(
		        		mRemoteSdp.type, sdpDescription);
		        peerConnection.setRemoteDescription(sdpObserver, sdpRemote);
		      }
		    });
		}

	  private AudioTrack createAudioTrack() {
		    localAudioTrack = mCallManager.getPeerConnectionFactory().createAudioTrack(
		        AUDIO_TRACK_ID,
		        mCallManager.createlocalAudioSource());
		    localAudioTrack.setEnabled(true);
		    return localAudioTrack;
	  }
	  
	  private VideoTrack createVideoTrack() {
		    localVideoTrack = mCallManager.getPeerConnectionFactory().createVideoTrack(VIDEO_TRACK_ID, 
		    		mCallManager.createlocalVideoSource(videoWidth, videoHeight, videoFps));
		    
		    //add by david.xu
		    if(localRender != null) {
		      localVideoTrack.setEnabled(renderVideo);
		      localVideoTrack.addRenderer(new VideoRenderer(localRender));
		    }
		    
		    return localVideoTrack;
	 }
	  
	  @Override
	  public void setAudioEnabled(final boolean enable) {
		    executor.execute(new Runnable() {
		      @Override
		      public void run() {
		        enableAudio = enable;
		        if (localAudioTrack != null) {
		          localAudioTrack.setEnabled(enableAudio);
		        }
		      }
		    });
		  }
	  
	  @Override
		  public void setVideoEnabled(final boolean enable) {
		    executor.execute(new Runnable() {
		      @Override
		      public void run() {
		        renderVideo = enable;
		        if (localVideoTrack != null) {
		          localVideoTrack.setEnabled(renderVideo);
		        }
		        if (remoteVideoTrack != null) {
		          remoteVideoTrack.setEnabled(renderVideo);
		        }
		      }
		    });
		  }

	
	private void createOffer() {
		    executor.execute(new Runnable() {
		      @Override
		      public void run() {
		        if (peerConnection != null && !isError) {
		          isInitiator = true;
		          System.out.println("====================createOffer========:");
		          peerConnection.createOffer(sdpObserver, sdpMediaConstraints);
		        }
		      }
		    });
		  }

	private void createAnswer() {	  
		    executor.execute(new Runnable() {
		      @Override
		      public void run() {
		          Log.d(TAG, "========PC create ANSWER===========1");
		        if (peerConnection != null && !isError) {
		          Log.d(TAG, "========PC create ANSWER===========2");
		          isInitiator = false;
		          peerConnection.createAnswer(sdpObserver, sdpMediaConstraints);
		        }
		      }
		    });
	}
	
	 private static String setStartBitrate(String codec, boolean isVideoCodec,
		      String sdpDescription, int bitrateKbps) {
		    String[] lines = sdpDescription.split("\r\n");
		    int rtpmapLineIndex = -1;
		    boolean sdpFormatUpdated = false;
		    String codecRtpMap = null;
		    // Search for codec rtpmap in format
		    // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
		    String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
		    Pattern codecPattern = Pattern.compile(regex);
		    for (int i = 0; i < lines.length; i++) {
		      Matcher codecMatcher = codecPattern.matcher(lines[i]);
		      if (codecMatcher.matches()) {
		        codecRtpMap = codecMatcher.group(1);
		        rtpmapLineIndex = i;
		        break;
		      }
		    }
		    if (codecRtpMap == null) {
		      Log.w(TAG, "No rtpmap for " + codec + " codec");
		      return sdpDescription;
		    }
		    Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap
		        + " at " + lines[rtpmapLineIndex]);

		    // Check if a=fmtp string already exist in remote SDP for this codec and
		    // update it with new bitrate parameter.
		    regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
		    codecPattern = Pattern.compile(regex);
		    for (int i = 0; i < lines.length; i++) {
		      Matcher codecMatcher = codecPattern.matcher(lines[i]);
		      if (codecMatcher.matches()) {
		        Log.d(TAG, "Found " +  codec + " " + lines[i]);
		        if (isVideoCodec) {
		          lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE
		              + "=" + bitrateKbps;
		        } else {
		          lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE
		              + "=" + (bitrateKbps * 1000);
		        }
		        Log.d(TAG, "Update remote SDP line: " + lines[i]);
		        sdpFormatUpdated = true;
		        break;
		      }
		    }

		    StringBuilder newSdpDescription = new StringBuilder();
		    for (int i = 0; i < lines.length; i++) {
		      newSdpDescription.append(lines[i]).append("\r\n");
		      // Append new a=fmtp line if no such line exist for a codec.
		      if (!sdpFormatUpdated && i == rtpmapLineIndex) {
		        String bitrateSet;
		        if (isVideoCodec) {
		          bitrateSet = "a=fmtp:" + codecRtpMap + " "
		              + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
		        } else {
		          bitrateSet = "a=fmtp:" + codecRtpMap + " "
		              + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
		        }
		        Log.d(TAG, "Add remote SDP line: " + bitrateSet);
		        newSdpDescription.append(bitrateSet).append("\r\n");
		      }

		    }
		    return newSdpDescription.toString();
	   }
	 
	 private static String preferCodec(
		      String sdpDescription, String codec, boolean isAudio) {
		    String[] lines = sdpDescription.split("\r\n");
		    int mLineIndex = -1;
		    String codecRtpMap = null;
		    // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
		    String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
		    Pattern codecPattern = Pattern.compile(regex);
		    String mediaDescription = "m=video ";
		    if (isAudio) {
		      mediaDescription = "m=audio ";
		    }
		    for (int i = 0; (i < lines.length)
		        && (mLineIndex == -1 || codecRtpMap == null); i++) {
		      if (lines[i].startsWith(mediaDescription)) {
		        mLineIndex = i;
		        continue;
		      }
		      Matcher codecMatcher = codecPattern.matcher(lines[i]);
		      if (codecMatcher.matches()) {
		        codecRtpMap = codecMatcher.group(1);
		      }
		    }
		    if (mLineIndex == -1) {
		      Log.w(TAG, "No " + mediaDescription + " line, so can't prefer " + codec);
		      return sdpDescription;
		    }
		    if (codecRtpMap == null) {
		      Log.w(TAG, "No rtpmap for " + codec);
		      return sdpDescription;
		    }
		    Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + ", prefer at "
		        + lines[mLineIndex]);
		    String[] origMLineParts = lines[mLineIndex].split(" ");
		    if (origMLineParts.length > 3) {
		      StringBuilder newMLine = new StringBuilder();
		      int origPartIndex = 0;
		      // Format is: m=<media> <port> <proto> <fmt> ...
		      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		      newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		      newMLine.append(codecRtpMap);
		      for (; origPartIndex < origMLineParts.length; origPartIndex++) {
		        if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
		          newMLine.append(" ").append(origMLineParts[origPartIndex]);
		        }
		      }
		      lines[mLineIndex] = newMLine.toString();
		      Log.d(TAG, "Change media description: " + lines[mLineIndex]);
		    } else {
		      Log.e(TAG, "Wrong SDP media description format: " + lines[mLineIndex]);
		    }
		    StringBuilder newSdpDescription = new StringBuilder();
		    for (String line : lines) {
		      newSdpDescription.append(line).append("\r\n");
		    }
		    return newSdpDescription.toString();
	 }
	 
	 private static String preferTransportCandidate(
		      String sdpDescription, String codec, boolean isAudio) {
		    String[] lines = sdpDescription.split("\r\n");
		    int mLineIndex = -1;
		    // a=candidate:1510613869 1 udp 2122129151 127.0.0.1 33936 typ host generation 0 network-id 1
		    // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
		    
		    StringBuilder newSdpDescription = new StringBuilder();
		    for (int i = 0; (i < lines.length); i++) {
		    	if(lines[i].indexOf("candidate:") != -1)
		    	{
		    		if(lines[i].indexOf(codec) != -1) {
				        mLineIndex = i;
		    		}
		    	}
		    	if(mLineIndex != i)
	    			newSdpDescription.append(lines[i]).append("\r\n");
		    }
		    
		    return newSdpDescription.toString();	    
	 }
	 
	 private void switchCameraInternal() {
		    if (!videoCallEnabled || numberOfCameras < 2 || isError) {
		      Log.e(TAG, "Failed to switch camera. Video: " + videoCallEnabled + ". Error : "
		          + isError + ". Number of cameras: " + numberOfCameras);
		      return;  // No video is sent or only one camera is available or error happened.
		    }
		    Log.d(TAG, "Switch camera");
		    //videoCapturer.switchCamera(null);
		  }

		  public void switchCamera() {
		    executor.execute(new Runnable() {
		      @Override
		      public void run() {
		        switchCameraInternal();
		      }
		    });
		  }

		  public void changeCaptureFormat(final int width, final int height, final int framerate) {
		    executor.execute(new Runnable() {
		      @Override
		      public void run() {
		        changeCaptureFormatInternal(width, height, framerate);
		      }
		    });
		  }

		  private void changeCaptureFormatInternal(int width, int height, int framerate) {
		    if (!videoCallEnabled || isError) {
		      Log.e(TAG, "Failed to change capture format. Video: " + videoCallEnabled + ". Error : "
		          + isError);
		      return;
		    }
		    Log.d(TAG, "changeCaptureFormat: " + width + "x" + height + "@" + framerate);
		    //videoCapturer.onOutputFormatRequest(width, height, framerate);
		  }

	 
	 // Implementation detail: observe ICE & stream changes and react accordingly.
	  private class PCObserver implements PeerConnection.Observer {
	    @Override
	    public void onIceCandidate(final IceCandidate candidate){
	      executor.execute(new Runnable() {
	        @Override
	        public void run() {
	        	 Log.d(TAG, "===============onIceCandidate=================: " + candidate.sdp);	
	          //if(!iceGatherComplete)
	           // AddIceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
	        }
	      });
	    }

	    @Override
	    public void onSignalingChange(
	        PeerConnection.SignalingState newState) {
	      Log.d(TAG, "SignalingState: " + newState);
	    }

	    @Override
	    public void onIceConnectionChange(
	        final PeerConnection.IceConnectionState newState) {
	      executor.execute(new Runnable() {
	        @Override
	        public void run() {
	          Log.d(TAG, "===============IceConnectionState=================: " + newState);
	          if (newState == IceConnectionState.CONNECTED) {
	            //events.onIceConnected();
	          } else if (newState == IceConnectionState.DISCONNECTED) {
	            //events.onIceDisconnected();
	          } else if (newState == IceConnectionState.FAILED) {
	            reportError("ICE connection failed.");
	          }
	        }
	      });
	    }

	    @Override
	    public void onIceGatheringChange(
	    		final PeerConnection.IceGatheringState newState) {
	    	 executor.execute(new Runnable() {
	    	     @Override
	    	      public void run() {
	   	          Log.d(TAG, "===============IceGatheringState=================: " + newState);
	    	      if (newState == IceGatheringState.COMPLETE) {
	    	    	//  iceGatherComplete = true;
	    	    	  //onIceGatherComplete();
	    	    	  CallOrAnswer(peerConnection.getLocalDescription());
	    	    	  
	    	      } else if (newState == IceGatheringState.GATHERING) {
	    	        // events.onIceDisconnected();
	    	      }
	    	    }
	        });
	    }

	    @Override
	    public void onIceConnectionReceivingChange(boolean receiving) {
	      Log.d(TAG, "IceConnectionReceiving changed to " + receiving);
	    }

	    @Override
	    public void onAddStream(final MediaStream stream) {
	      executor.execute(new Runnable() {
	        @Override
	        public void run() {
	      		System.out.println("============onAddStream=========");

	          if (peerConnection == null || isError) {
	            return;
	          }
	          if (stream.audioTracks.size() > 1 || stream.videoTracks.size() > 1) {
	            reportError("Weird-looking stream: " + stream);
	            return;
	          }
	          
	          MediaStreamReady = true;
	          mStream = stream;
	          
	          if(isConnected) {
	      		System.out.println("=======isConnected=====onAddStream=========:" +peerConnection.getRemoteDescription().description);
	      		if(mCallStreamObserver != null)
	    			  mCallStreamObserver.onAddStream(callId, stream);
	        	  if(mCallStateEventListener != null)
	        		  mCallStateEventListener.OnCallStateChange(callId, CallState.Answered.IntgerValue());
	          } else {
	        	  //mStream = stream; 
	          }
	        }
	      });
	    }

	    @Override
	    public void onRemoveStream(final MediaStream stream){
	      executor.execute(new Runnable() {
	        @Override
	        public void run() {
	          remoteVideoTrack = null;
	        }
	      });
	    }

	    @Override
	    public void onDataChannel(final DataChannel dc) {
	      reportError("AppRTC doesn't use data channels, but got: " + dc.label()
	          + " anyway!");
	    }

	    @Override
	    public void onRenegotiationNeeded() {
	      // No need to do anything; AppRTC follows a pre-agreed-upon
	      // signaling/negotiation protocol.
	    }

		@Override
		public void onIceCandidatesRemoved(IceCandidate[] arg0) {
			// TODO Auto-generated method stub
			
		}
	  }
	  
	  
		// Implementation detail: handle offer creation/signaling and answer setting,
		  // as well as adding remote ICE candidates once the answer SDP is set.
		  private class SDPObserver implements SdpObserver {
		    @Override
		    public void onCreateSuccess(final SessionDescription origSdp) {
		      if (localSdp != null) {
		        reportError("Multiple SDP create.");
		        return;
		      }
		      String sdpDescription = origSdp.description;
		     // if (preferIsac) {
		       // sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
		     // }
		     // if (videoCallEnabled) {
		     // //  sdpDescription = preferCodec(sdpDescription, preferredVideoCodec, false);
		     // }
		      final SessionDescription sdp = new SessionDescription(
		          origSdp.type, sdpDescription);
		      localSdp = sdp;
		      executor.execute(new Runnable() {
		        @Override
		        public void run() {
		          if (peerConnection != null && !isError) {
		            Log.d(TAG, "Set local SDP from " + sdp.type);
		            peerConnection.setLocalDescription(sdpObserver, sdp);
		          }
		        }
		      });
		    }

		    @Override
		    public void onSetSuccess() {
		      executor.execute(new Runnable() {
		        @Override
		        public void run() {
		          if (peerConnection == null || isError) {
		            return;
		          }
		          if (isInitiator) {
		            // For offering peer connection we first create offer and set
		            // local SDP, then after receiving answer set remote SDP.
		            if (peerConnection.getRemoteDescription() == null) {
		              // We've just set our local SDP so time to send it.
		              Log.d(TAG, "======offer===Local SDP set succesfully========");
		              //events.onLocalDescription(localSdp);
		              onLocalDescription(localSdp);
		            } else {
		              // We've just set remote description, so drain remote
		              // and send local ICE candidates.
		              Log.d(TAG, "=====offer=====Remote SDP set succesfully");
		              //drainCandidates();
		            }
		          } else {
		            // For answering peer connection we set remote SDP and then
		            // create answer and set local SDP.
		            if (peerConnection.getLocalDescription() != null) {
		              // We've just set our local SDP so time to send it, drain
		              // remote and send local ICE candidates.
		              Log.d(TAG, "=====answer===Local SDP set succesfully========");
		              onLocalDescription(localSdp);
		              //drainCandidates();
		            } else {
		              // We've just set remote SDP - do nothing for now -
		              // answer will be created soon.
		              Log.d(TAG, "=====answer===Remote SDP set succesfully");
		            }
		          }
		        }
				
		      });
		    }

		    @Override
		    public void onCreateFailure(final String error) {
		      reportError("createSDP error: " + error);
		    }

		    @Override
		    public void onSetFailure(final String error) {
		      reportError("setSDP error: " + error);
		      System.out.println("=========onSetFailure========:" +error);
		    }
		  }

		 private void reportError(final String errorMessage) {
			    Log.e(TAG, "Peerconnection error: " + errorMessage);
			    executor.execute(new Runnable() {
			      @Override
			      public void run() {
			        if (!isError) {
			         // events.onPeerConnectionError(errorMessage);
			          isError = true;
			        }
			      }
			});
		 }

		 private void onLocalDescription(SessionDescription localSdp) {
				// TODO Auto-generated method stub
	         Log.d(TAG, "======offer==onLocalDescription========");

					String type = null;
					if(localSdp.type == SessionDescription.Type.OFFER) {
						type = "offer";
					} else if(localSdp.type == SessionDescription.Type.ANSWER) {
						type = "answer";
					}
		 }
		
		 private void CallOrAnswer(final SessionDescription Localsdp)
		 {
			     String sdp =  preferTransportCandidate(Localsdp.description, "127.0.0.1", false);
			
				    	if(Localsdp != null) {
							if(isInitiator) {
								 Log.d(TAG, "=========nativeMakeCall========:" +sdp);
								 if(this.mCallManager != null)
										mCallManager.MakeCall(accId, this.callId, peerNumber, sdp);
							} else {
								 Log.d(TAG, "=========nativeAnswerCall========:" +sdp);
								 if(this.mCallManager != null)
									mCallManager.Accept(this.callId, sdp);
							}
						}
		 }
		 
		 @Override
			public void OnCallStateChange(final int call_id, final int state) {
				  callId = call_id;
				  final CallState state_code = CallState.fromInt(state);
				  executor.execute(new Runnable() {
				        @Override
				        public void run() {
		                          if(state_code.IntgerValue() == CallState.Answered.IntgerValue()) {
		                                  isConnected = true;
		                                  if(MediaStreamReady) {
		                                        System.out.println("=======MediaStreamReady=====OnCallStateChange=========:" +peerConnection.getRemoteDescription().description);
		                                        if(mCallStreamObserver != null)
		                                            mCallStreamObserver.onAddStream(callId, mStream);
		                                          if(mCallStateEventListener != null)
		                                        	  mCallStateEventListener.OnCallStateChange(callId, state);  
		                                  }
		                          } else {
		                      		  mCallStateEventListener.OnCallStateChange(callId, state);
		                          }
		                  }
				    });
			}
		 
		 
		 @Override
		 public void close() {
			  executor.execute(new Runnable() {
			    @Override
			  public void run() {
			        closeInternal();
			      }
			 });
	    }
		 
		 private void closeInternal() {
			    Log.d(TAG, "Closing peer connection.");
			    if (peerConnection != null) {
			      peerConnection.dispose();
			      peerConnection = null;
			    }
			    
			    statsTimer.cancel();

			    Log.d(TAG, "Closing video source.");
			    
			    
			    if(mCallManager != null) {
			    	//if(isLastCall())
			    	mCallManager.stoplocalMediaSource();
			    	mCallManager = null;
			    }
		
			    Log.d(TAG, "Closing peer connection done.");
	     }
		 
		 @Override
		 public void setPeerConnectionStatsReport(PeerConnectionStatsReport mListener) {
				this.mPeerConnectionStatsReport = mListener;
		 }
		 

		  private void getStats() {
		    if (peerConnection == null || isError) {
		      return;
		    }
		    boolean success = peerConnection.getStats(new StatsObserver() {
		      @Override
		      public void onComplete(final StatsReport[] reports) {
		    	  if(mPeerConnectionStatsReport   != null)
		    		  mPeerConnectionStatsReport.onPeerConnectionStatsReady(reports);
		      }
		    }, null);
		    if (!success) {
		      Log.e(TAG, "getStats() returns false!");
		    }
		  }
		  
		 @Override
		 public void enableStatsEvents(boolean enable, int periodMs) {
			    if (enable) {
			      try {
			        statsTimer.schedule(new TimerTask() {
			          @Override
			          public void run() {
			            executor.execute(new Runnable() {
			              @Override
			              public void run() {
			                getStats();
			              }
			            });
			          }
			        }, 0, periodMs);
			      } catch (Exception e) {
			        Log.e(TAG, "Can not schedule statistics timer", e);
			      }
			    } else {
			      statsTimer.cancel();
			    }
			  }
}
