package com.cloudrtc.sdk;

import org.webrtc.StatsReport;

public interface PeerConnectionStatsReport {
	
	public void onPeerConnectionStatsReady(StatsReport[] reports);
	
}
