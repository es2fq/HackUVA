import java.awt.Point;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.util.ArrayList;
public class InputController {
	public static ArrayList<Handle> handles;
	public static Handle mouseHandle = new Handle();//only for testing with the mouse
	public static class Handle {
		public double x, y, z;
		public boolean isValid = true;
		public long lastFrameId;
		//todo: extend this for other leap motion bs
		public Handle() {
			
		}
	}
	public static void update() {
		Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
		mouseHandle.x = mouseLoc.getX();
		mouseHandle.y = mouseLoc.getY();
		
		if (mouseHandle != null) {
			if (!handles.contains(mouseHandle) && mouseHandle.isValid) {
				handles.add(mouseHandle);
				MidiControl.addNewPitchHandle(mouseHandle);
			}
		}
	}
}