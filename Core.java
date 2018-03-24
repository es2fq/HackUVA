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
            newHandle.x = h.palmPosition().getX();
            newHandle.y = h.palmPosition().getY();
            newHandle.z = h.palmPosition().getZ();
            int newFingers = h.fingers().extended().count();
            if (newHandle.lastFingerChangeTime + 150 < System.currentTimeMillis()) {
            	//the finger change time occured a while ago, so safe to say it is stable
                if (newHandle.fingers != newFingers) {
                	if (newFingers > 0) {
                		newHandle.lastFingerChangeTime = System.currentTimeMillis();
                	} else {
                        newHandle.fingers = newFingers;
                    }
                } else {
                    newHandle.fingers = newFingers;
                }
            } else {
            	if (newFingers == 0) {
            		newHandle.fingers = newFingers;
            	}
            }
            	
            newHandle.lastFrameId = frame.id();
            //System.out.println("H "+newHandle.x+", "+newHandle.y+", "+newHandle.z+", "+newHandle.fingers);
        }

        Iterator<Integer> handleIterator = handleMap.keySet().iterator();
        while (handleIterator.hasNext()) {
            Integer handleId = handleIterator.next();
            if (handleMap.get(handleId).lastFrameId != frame.id()) {
            	handleMap.get(handleId).isValid = false;
                handleMap.remove(handleId);
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