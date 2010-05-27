package edu.stanford.pepe;

/**
 * Object that can receive callbacks from PepeAgent.
 * 
 * @author jtamayo
 */
public interface AgentListener {
	void onMethodEntry(String clazz, String methodName);

	/**
	 * Invoked on a method exit. This might be due to a return statement or
	 * because an exception is thrown. Currently there's no way to distinguish
	 * between each.
	 * 
	 * @param clazz
	 * @param methodName
	 */
	void onMethodExit(String clazz, String methodName);
}
