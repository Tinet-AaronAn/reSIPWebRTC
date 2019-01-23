package com.cloudrtc.ui;

//import org.webrtc.videoengine.ViERenderer;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cloudrtc.R;
import com.cloudrtc.sdk.PeerConnectionStatsReport;
import com.cloudrtc.service.PhoneService;
import com.cloudrtc.util.Debug;

import org.webrtc.EglBase;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRendererGui;

import java.util.HashMap;
import java.util.Map;

public class VideoTalkFragment extends Fragment implements PeerConnectionStatsReport
{
	private boolean usingFrontCamera = true;
	private Button mHangup, mSwapCam, mMute;//, mSpeaker;
	int currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	int currentCameraOrientation = 0;
	int numCamera = -1;
	public Handler mHandler = null;
	//private SurfaceView remoteRender;
	private GLSurfaceView videoView;
	private EglBase rootEglBase;
	private SurfaceViewRenderer localRender;
	private SurfaceViewRenderer remoteRender;
	private boolean hidden = false;
	private LinearLayout mLocalLayout = null;
	private LinearLayout mRemoteLayout = null;
	/**
	 * 是否静音
	 */
	private boolean isMute = false;

	/**
	 * 是否为免提
	 */
	private boolean isSpeaker = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_video_screen, null);
		init(view);
		return view;
	}

	private void init(View view) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());

		mHandler = new Handler();
        PhoneService.instance().setPeerConnectionStatsReport(this);
        PhoneService.instance().enableStatsEvents(true, 1000);

        //numCamera = UIUtils.getNumberOfCameras();
		//usingFrontCamera = UIUtils.checkCameraAndChoiceBetter();

        mLocalLayout = (LinearLayout)view.findViewById(R.id.llLocalView);
        mRemoteLayout = (LinearLayout)view.findViewById(R.id.llRRemoteView);

		mSwapCam = (Button) view.findViewById(R.id.swap_cam);
		mSwapCam.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				usingFrontCamera = !usingFrontCamera;
				//int cameraOrientation = PhoneService.instance().GetCameraOrientation(usingFrontCamera ? 1 : 0);
				//PhoneService.instance().SwapCamera(usingFrontCamera ? 1 : 0, cameraOrientation, null);
				//PhoneService.instance().SetCameraOutputRotation(getCameraOrientation(cameraOrientation));
			}
		});

		mHangup = (Button) view.findViewById(R.id.hangup);
		mHangup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PhoneService.instance().Hangup();
				getActivity().finish();
			}
		});

		mMute = (Button) view.findViewById(R.id.mute);
		mMute.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isMute) {
					PhoneService.instance().MuteMic(true);
					isMute = !isMute;
				} else {
					PhoneService.instance().MuteMic(false);
					isMute = !isMute;
				}
			}
		});

		/*mSpeaker = (Button) view.findViewById(R.id.Speaker);
		mSpeaker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isSpeaker) {
					PhoneService.instance().SetLoudspeakerStatus(true);
					isSpeaker = !isSpeaker;
				} else {
					//PhoneService.instance().unlock();//;
					isSpeaker = !isSpeaker;
				}
			}
		});*/


		// Create UI controls.
		videoView = (GLSurfaceView) view.findViewById(R.id.glview_call);

		// Create video renderers.
		VideoRendererGui.setView(videoView, new Runnable() {
			@Override
			public void run() {
				Debug.d("VideoTalking", "VideoRendererGui loading...");
			}
		});

		// Show/hide call control fragment on view click.
	    videoView.setOnClickListener(new OnClickListener() {

	      @Override
	      public void onClick(View view) {
	    	  hidden = !hidden;
	    	  if(hidden)
	    	  {
	    		  mHangup.setVisibility(View.INVISIBLE);
	    		  mSwapCam.setVisibility(View.INVISIBLE);
	    		  mMute.setVisibility(View.INVISIBLE);
	    		  //mSpeaker.setVisibility(View.INVISIBLE);
	    	  } else
	    	  {
	    		  mHangup.setVisibility(View.VISIBLE);
	    		  mSwapCam.setVisibility(View.VISIBLE);
	    		  mMute.setVisibility(View.VISIBLE);
	    		 // mSpeaker.setVisibility(View.VISIBLE);
	    	  }
	      }
	    });

		if(remoteRender == null) {
			remoteRender = new SurfaceViewRenderer(getActivity());
			LinearLayout r = new LinearLayout(getActivity());
			//r.setBackgroundResource(R.drawable.boader);
			r.addView(remoteRender);
			mRemoteLayout.addView(r);
		}

		if (localRender == null) {
			localRender = new SurfaceViewRenderer(getActivity());
			LinearLayout l = new LinearLayout(getActivity());
			//l.setBackgroundResource(R.drawable.boader);
			l.addView(localRender);
			mLocalLayout.addView(l);
		}

		rootEglBase = EglBase.create();
		localRender.init(rootEglBase.getEglBaseContext(), null);
		remoteRender.init(rootEglBase.getEglBaseContext(), null);
		localRender.setZOrderMediaOverlay(true);

        System.out.println("===============StartVideoRender============");
		PhoneService.instance().StartVideoRender(this.localRender, this.remoteRender);


        //PjSipService.instance().StartAudioManager();


		//remoteRender = ViERenderer.CreateRenderer(getActivity(), true);
		//videoView.addView(remoteRender);

		//int cameraOrientation = PhoneService.instance().GetCameraOrientation(usingFrontCamera ? 1 : 0);
		//PhoneService.instance().SetupVideoChannel(352, 288, 15, 384);

		if(numCamera == 1) {
			//PhoneService.instance().StartVideoReceiving(remoteRender);
			//PjSipService.instance().StartVideoChannel(0, 0, null, remoteRender);
		} else {
		//	PjSipService.instance().StartVideoReceiving(remoteRender);

			//PjSipService.instance().StartVideoChannel(usingFrontCamera ? 1 : 0, getCameraOrientation(cameraOrientation), null, remoteRender);
		}

		PhoneService.instance().SetLoudspeakerStatus(isSpeaker);
		//PhoneService.instance().StartVoiceChannel();
		//PhoneService.instance().SetCameraOutputRotation(getCameraOrientation(cameraOrientation));
		//System.out.println("======================cameraOrientation====================:" +getCameraOrientation(cameraOrientation));
   }

	public void stopVideoChannel() {

		//PhoneService.instance().StopVideoChannel();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//PjSipService.instance().StopAudioManager();
		//stopVideoChannel();
	}

	public int getCameraOrientation( int cameraOrientation)
	{

	    Display display = getActivity().getWindowManager().getDefaultDisplay();
		int displatyRotation = display.getRotation();
	    int degrees = 0;
	    switch (displatyRotation)
	    {
	        case Surface.ROTATION_0: degrees = 0; break;
	        case Surface.ROTATION_90: degrees = 90; break;
	        case Surface.ROTATION_180: degrees = 180; break;
	        case Surface.ROTATION_270: degrees = 270; break;
	    }

	    int result = 0;
	    if (cameraOrientation > 180) {
	        result = (cameraOrientation + degrees) % 360;
	    } else {
	        result = (cameraOrientation - degrees + 360) % 360;
	    }
	    return result;
	}

	public void SetupCameraRotation()
	{
		//int cameraOrientation = PhoneService.instance().GetCameraOrientation(usingFrontCamera ? 1 : 0);
		//PhoneService.instance().SetCameraOutputRotation(getCameraOrientation(cameraOrientation));
	}

	public void setVideoRenderViewOrientation(int orientation)
	{
		hidden = false;
		mHangup.setVisibility(View.VISIBLE);
	}

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        // TODO Auto-generated method stub

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateEncoderStatistics(reports);
            }
        });
    }

    private void updateEncoderStatistics(final StatsReport[] reports) {
        StringBuilder encoderStat = new StringBuilder(128);
        StringBuilder bweStat = new StringBuilder();
        StringBuilder connectionStat = new StringBuilder();
        StringBuilder videoSendStat = new StringBuilder();
        StringBuilder videoRecvStat = new StringBuilder();
        String fps = null;
        String targetBitrate = null;
        String actualBitrate = null;

        for (StatsReport report : reports) {
            if (report.type.equals("ssrc") && report.id.contains("ssrc")
                    && report.id.contains("send")) {
                // Send video statistics.
                Map<String, String> reportMap = getReportMap(report);
                String trackId = reportMap.get("googTrackId");
                if (trackId != null && trackId.contains("ARDAMSv0")) {
                    fps = reportMap.get("googFrameRateSent");
                    //System.out.println("==========SendVideoFps======:" +fps);
                    videoSendStat.append(report.id).append("\n");
                    for (StatsReport.Value value : report.values) {
                        // System.out.print("===========name========:" +value.name +"\n");
                        //System.out.print("===========value========:" +value.value +"\n");
                        String name = value.name.replace("goog", "");
                        videoSendStat.append(name).append("=").append(value.value).append("\n");
                    }
                }
            } else if (report.type.equals("ssrc") && report.id.contains("ssrc")
                    && report.id.contains("recv")) {
                // Receive video statistics.
                Map<String, String> reportMap = getReportMap(report);
                // Check if this stat is for video track.
                String frameWidth = reportMap.get("googFrameWidthReceived");
                if (frameWidth != null) {
                    videoRecvStat.append(report.id).append("\n");
                    for (StatsReport.Value value : report.values) {
                        String name = value.name.replace("goog", "");
                        //System.out.print("===========name========:" +value.name +"\n");
                        //System.out.print("===========value========:" +value.value +"\n");
                        videoRecvStat.append(name).append("=").append(value.value).append("\n");
                    }
                }
            } else if (report.id.equals("bweforvideo")) {
                // BWE statistics.
                Map<String, String> reportMap = getReportMap(report);
                targetBitrate = reportMap.get("googTargetEncBitrate");
                actualBitrate = reportMap.get("googActualEncBitrate");
                int TB = Integer.valueOf(targetBitrate).intValue();
                int AB = Integer.valueOf(actualBitrate).intValue();
                TB = TB/1000;
                AB = AB/1000;
                targetBitrate = String.valueOf(TB);
                actualBitrate = String.valueOf(AB);

                bweStat.append(report.id).append("\n");
                for (StatsReport.Value value : report.values) {
                    //System.out.print("===========name========:" +value.name +"\n");
                    //System.out.print("===========value========:" +value.value +"\n");
                    String name = value.name.replace("goog", "").replace("Available", "");
                    //bweStat.append(name).append("=").append(value.value).append("\n");
                }
            } else if (report.type.equals("googCandidatePair")) {
                // Connection statistics.
                Map<String, String> reportMap = getReportMap(report);
                String activeConnection = reportMap.get("googActiveConnection");
                if (activeConnection != null && activeConnection.equals("true")) {
                    connectionStat.append(report.id).append("\n");
                    for (StatsReport.Value value : report.values) {
                        String name = value.name.replace("goog", "");
                        //(name.equals("packetsDiscardedOnSend"))
                        //System.out.print("===========name========:" +value.name +"\n");
                          // System.out.print("===========packetsDiscardedOnSend========:" +value.value +"\n");
                        // connectionStat.append(name).append("=").append(value.value).append("\n");
                    }
                }
            }
        }
        //hudViewBwe.setText(bweStat.toString());
        //hudViewConnection.setText(connectionStat.toString());
        //hudViewVideoSend.setText(videoSendStat.toString());
        //hudViewVideoRecv.setText(videoRecvStat.toString());

        if (true) {
            if (fps != null) {
                encoderStat.append("Fps:  ").append(fps).append("\n");
            }
            if (targetBitrate != null) {
                encoderStat.append("Target BR: ").append(targetBitrate).append("\n");
            }
            if (actualBitrate != null) {
                encoderStat.append("Actual BR: ").append(actualBitrate).append("\n");
            }
        }

       /* if (cpuMonitor.sampleCpuUtilization()) {
            encoderStat.append("CPU%: ")
                    .append(cpuMonitor.getCpuCurrent()).append("/")
                    .append(cpuMonitor.getCpuAvg3()).append("/")
                    .append(cpuMonitor.getCpuAvgAll());
        }*/
        //encoderStatView.setText(encoderStat.toString());
    }

    private Map<String, String> getReportMap(StatsReport report) {
        Map<String, String> reportMap = new HashMap<String, String>();
        for (StatsReport.Value value : report.values) {
            reportMap.put(value.name, value.value);
        }
        return reportMap;
    }
}
