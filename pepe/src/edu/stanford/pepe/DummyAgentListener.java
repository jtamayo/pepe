package edu.stanford.pepe;

public class DummyAgentListener implements AgentListener {

	@Override
	public void onMethodEntry(String clazz, String methodName) {
		if (Math.random() < 0.0001) {			
//			System.out.println("PEPE: Entering " + clazz + "." + methodName);
		}
	}

	@Override
	public void onMethodExit(String clazz, String methodName) {
//		if (Math.random() < 0.0001) {
			System.out.println("PEPE: Exiting " + clazz + "." + methodName);
//		}
	}

}
