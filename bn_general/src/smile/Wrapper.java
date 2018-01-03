package smile;

//import myutils.LoadSmileLibrary;

public abstract class Wrapper {
	public Wrapper() { 
		ptrNative = createNative(null); 
	}
	
	public Wrapper(Object param) {
		ptrNative = createNative(param);
	}
	
	public void dispose() {
		deleteNative(ptrNative);
		ptrNative = 0;
	}
	
	protected void finalize() { 
		deleteNative(ptrNative); 
	}
	
	protected abstract long createNative(Object param);
	protected abstract void deleteNative(long nativePtr);
	private static native String staticInitNative();

	protected long ptrNative = 0;
	
	static {
		String nativeLib = System.getProperty("jsmile.native.library");
		if (nativeLib != null) {
			System.load(nativeLib);	
		} else {
			System.loadLibrary("jsmile");
		}
		//LoadSmileLibrary.loadNativeLibrary();

		String jniVersion = staticInitNative();
		String pkgVersion = Wrapper.class.getPackage().getImplementationVersion();
		if (pkgVersion != null && !pkgVersion.equals(jniVersion)) {
			throw new SMILEException(
				String.format("JAR and JNI version mismatch - expected '%1s', JNI returned '%2s'",
					pkgVersion, jniVersion));
		}
    }
}
