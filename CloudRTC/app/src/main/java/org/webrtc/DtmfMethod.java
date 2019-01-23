package org.webrtc;

import java.util.Vector;

public class DtmfMethod {
	private static Vector<DtmfMethod> values = new Vector<DtmfMethod>();
	public static DtmfMethod RFC2833 = new DtmfMethod("RFC2833",0);
	public static DtmfMethod INFO = new DtmfMethod("INFO",1);
	public static DtmfMethod INBAND = new DtmfMethod("INBAND",3);
	private String mStringValue;
	private int mIntgerValue;
	private DtmfMethod(String aStringValue,int aIntgerValue) {
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
	
	public static DtmfMethod fromInt(int value) {
		for (int i = 0; i < values.size(); i++) {
			DtmfMethod mtype = (DtmfMethod) values.elementAt(i);
			if (mtype.mIntgerValue == value) return mtype;
		}
		throw new RuntimeException("DtmfMethod not found [" + value + "]");
	}
}