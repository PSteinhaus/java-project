package helperFPPPPackage;

public class Helper {
	static public int ensureRange(int value, int min, int max) {
	   return Math.min(Math.max(value, min), max);
	}
	static public boolean inRange(int value, int min, int max) {
		return (value>= min) && (value<= max);
	}
}
//flags for the VM:
//-Djava.library.path=lib/lwjgl-2.9.3/native/windows