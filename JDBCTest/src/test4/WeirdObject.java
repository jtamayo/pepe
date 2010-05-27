package test4;

public class WeirdObject implements WeirdInterface {
	public static final WeirdInterface listener = new WeirdObject();

	@Override
	public void onMethodEntry(String clazz, String methodName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMethodExit(String clazz, String methodName) {
		// TODO Auto-generated method stub
		
	}

}
