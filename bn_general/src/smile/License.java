package smile;

public class License {
	public License(String pp, byte[] qq) {
		synchronized(License.class) {
			if (I) {
				throw new RuntimeException("SMILE license is already initialized.");
			}
			P = pp;
			Q = qq;
			I = true;
		}
	}

	private static String P = null;
	private static byte[] Q = null;
	private static boolean I = false;
}
