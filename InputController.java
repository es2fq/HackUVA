import java.awt.Point;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.util.ArrayList;
import com.leapmotion.leap.*;
public class InputController {
	public static ArrayList<Handle> handles = new ArrayList<Handle>();
	public static Handle mouseHandle = new Handle();//only for testing with the mouse
	
	
	static boolean madeMouseusician = false;
	public static class Handle {
		public double x, y, z;
		public Hand hand;
		public int fingers = 0, fingersChangeTo = -1;
		public boolean isValid = true, pinchedInControlZone = false;
		public long lastFrameId, lastFingerChangeTime;
		public float pinchAmount = 0, pinchAmountPrevious = 0;
		public MidiControl.HandleMusician musician;
		
		
		public float pinchDrawRadius = 0;
		//todo: extend this for other leap motion bs
		public Handle() {
			
		}
	}
	public static void update() {
		
		if (mouseHandle != null) {
			Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
			mouseHandle.x = mouseLoc.getX();
			mouseHandle.y = mouseLoc.getY();
			mouseHandle.fingers = 1;
			
			if (!handles.contains(mouseHandle) && mouseHandle.isValid) {
				handles.add(mouseHandle);
				if (!madeMouseusician) {
					MidiControl.addNewPitchHandle(mouseHandle);
					madeMouseusician = true;
				}
			}
		}
	}
}