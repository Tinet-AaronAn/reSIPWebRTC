package org.webrtc;
import android.content.Context;

abstract public class SipEngineFactory {
	private static String factoryName = "org.webrtc.SipEngineFactoryImpl";
	static SipEngineFactory theSipEngineFactory = null; 
	
	/**
	 * Indicate the name of the class used by this factory
	 * @param pathName
	 */
	public static void setFactoryClassName (String className) {
		factoryName = className;
	}
	
	public static SipEngineFactory instance() {
		try {
		if (theSipEngineFactory == null) {
			Class<?> lFactoryClass = Class.forName(factoryName);
			theSipEngineFactory = (SipEngineFactory) lFactoryClass.newInstance();
		}
		} catch (Exception e) {
			System.err.println("Cannot instanciate factory ["+factoryName+"]");
		}
		return theSipEngineFactory;
	}

	abstract public SipEngine CreateSipEngine(Context context);
}
