package org.webrtc;

import java.util.Vector;

public class LogLevel {
	private static Vector<LogLevel> values = new Vector<LogLevel>();
	public static LogLevel Crit = new LogLevel("Crit",0);
	public static LogLevel Err = new LogLevel("Err",1);
	public static LogLevel Warning = new LogLevel("Warning",2);
	public static LogLevel Info = new LogLevel("Info",3);
	public static LogLevel Notice = new LogLevel("Notice",4);
	public static LogLevel Debug = new LogLevel("Debug",5);
	public static LogLevel Stack = new LogLevel("Stack",6);
	
	private String mStringValue;
	private int mIntgerValue;
	private LogLevel(String aStringValue,int aIntgerValue) {
		mStringValue = aStringValue;
		mIntgerValue = aIntgerValue;
		values.addElement(this);
	}
	public String toString() {
		return mStringValue;
	}
	public int IntgerValue()
	{
		return mIntgerValue;
	}
	
	public static LogLevel fromInt(int value) {
		for (int i = 0; i < values.size(); i++) {
			LogLevel mtype = (LogLevel) values.elementAt(i);
			if (mtype.mIntgerValue == value) return mtype;
		}
		throw new RuntimeException("LogLevel not found [" + value + "]");
	}
}
