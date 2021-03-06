import com.leapmotion.leap.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

class CoreListener extends Listener {
	
    Map<Integer, InputController.Handle> handleMap;
    
    public void onConnect(Controller controller) {
        System.out.println("Connected");
        handleMap = new HashMap<Integer, InputController.Handle>();
    }
    public void onDeviceChange(Controller arg0) {
    	System.out.println("Device Change");
    }
    public void onDeviceFailure(Controller arg0) {
    	System.out.println("onDeviceFailure");
    }
    public void onDisconnect(Controller arg0) {
    	System.out.println("onDisconnect");
    }
    public void onExit(Controller arg0) {
    	System.out.println("onExit");
    }
    public void onFocusGained(Controller arg0) {
    	System.out.println("onFocusGained");
    }
    public void onFocusLost(Controller arg0) {
    	System.out.println("onFocusLost");
    }
    public void onServiceConnect(Controller arg0) {
    	System.out.println("onServiceConnect");
    }
    public void onServiceChange(Controller arg0) {
    	System.out.println("onServiceChange");
    }
    public void onServiceDisconnect(Controller arg0) {
    	System.out.println("onServiceDisconnect");
    }
    
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();
        
        // System.out.println("Frame id: " + frame.id()
        // + ", timestamp: " + frame.timestamp()
        // + ", hands: " + frame.hands().count()
        // + ", fingers: " + frame.fingers().count());

        HandList hands = frame.hands();

        ArrayList<InputController.Handle> handleList = new ArrayList<InputController.Handle>();
        for (Hand h : hands) {
            InputController.Handle newHandle;
            if (!handleMap.containsKey(h.id())) {
                newHandle = new InputController.Handle();
                MidiControl.addNewPitchHandle(newHandle);
                handleMap.put(h.id(), newHandle);
            }
            newHandle = handleMap.get(h.id());
            newHandle.hand = h;
            newHandle.x = h.palmPosition().getX();
            newHandle.y = h.palmPosition().getY();
            newHandle.z = h.palmPosition().getZ();
            int newFingers = h.fingers().extended().count();
            if (newFingers != newHandle.fingers || newFingers == 0) {
            	newHandle.lastFingerChangeTime = System.currentTimeMillis();
            }
            newHandle.pinchAmountPrevious = newHandle.pinchAmount;
            newHandle.pinchAmount = h.pinchStrength();
    		newHandle.fingers = newFingers;
//            newHandle.fingers = newFingers;	
            	
            newHandle.lastFrameId = frame.id();
            //System.out.println("H "+newHandle.x+", "+newHandle.y+", "+newHandle.z+", "+newHandle.fingers);
        }
        Iterator<InputController.Handle> handleIterator = InputController.handles.iterator();
        while (handleIterator.hasNext()) {
        	InputController.Handle handle = handleIterator.next();
            if (handle.lastFrameId != frame.id()) {
            	handle.isValid = false;
                handleMap.values().remove(handle);
            }
        }
        InputController.handles = new ArrayList<InputController.Handle>(handleMap.values());
        //System.out.println(handleMap.size());
        
        
        if (MidiControl.numInstruments != 0 && MidiControl.receivers[MidiControl.numInstruments - 1] != null) {
			InputController.update();
            MidiControl.update();
            MidiControl.graphPanel.update();
        }
    }
}

class Core {
	public static int worldXLeft = -400, worldXRight = 400, worldZFar = (int)MidiControl.maxZForControlZone, worldZNear = 200;
    public static void main(String[] args) {
        // Create a sample listener and controller
        CoreListener listener = new CoreListener();
        Controller controller = new Controller();
        
        // Have the sample listener receive events from the controller
        controller.addListener(listener);
        
        // Keep this process running until Enter is pressed
        System.out.println("Press Enter to quit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Remove the sample listener when done
        controller.removeListener(listener);
    }
}