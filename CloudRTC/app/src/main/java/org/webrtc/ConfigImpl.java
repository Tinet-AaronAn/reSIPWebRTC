package org.webrtc;

import org.webrtc.Config;

public class ConfigImpl extends Config {
	//implement for Config_JNI.cpp
	private long nativePtr = 0;
	private native void SaveStringConfig(long nativePtr, String key, String value);
	private native void SaveIntgerConfig(long nativePtr, String key, int value);
	private native void SaveBooleanConfig(long nativePtr, String key, boolean value);
	
	public ConfigImpl(long aNativePtr)
	{
		nativePtr = aNativePtr;
	}

	@Override
	public void Save() {
		
		SaveStringConfig(nativePtr,"user_agent",user_agent);
		SaveIntgerConfig(nativePtr,"udp_port",udp_port);
		SaveIntgerConfig(nativePtr,"tcp_port",tcp_port);
		SaveIntgerConfig(nativePtr,"tls_port",tls_port);
		
		SaveStringConfig(nativePtr,"video_codecs",video_codecs);
		SaveStringConfig(nativePtr,"audio_codecs",audio_codecs);

		SaveIntgerConfig(nativePtr,"rtp_port_start",rtp_port_start);
		SaveIntgerConfig(nativePtr,"rtp_port_end",rtp_port_end);
		SaveIntgerConfig(nativePtr,"mtu",mtu);
		

		SaveStringConfig(nativePtr,"stun_server",stun_server);
		SaveIntgerConfig(nativePtr,"stun_server_port",stun_server_port);
		SaveStringConfig(nativePtr,"turn_server",turn_server);
		SaveIntgerConfig(nativePtr,"turn_server_port",turn_server_port);
		SaveStringConfig(nativePtr,"turn_username",turn_username);
		SaveStringConfig(nativePtr,"turn_password",turn_password);
		
		SaveIntgerConfig(nativePtr,"log_level",log_level.IntgerValue());
		SaveBooleanConfig(nativePtr,"log_on",log_on);
		
		SaveStringConfig(nativePtr,"sip_trac_file",sip_trac_file);
		SaveStringConfig(nativePtr,"voe_trac_file",voe_trac_file);
		SaveStringConfig(nativePtr,"vie_trac_file",vie_trac_file);
	}
}
