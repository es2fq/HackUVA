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

        List<InputController.Handle> handleList = new ArrayList<InputController.Handle>();
        for (Hand h : hands) {
            InputController.Handle newHandle;
            if (!handleMap.containsKey(h.id())) {
                newHandle = new InputController.Handle();
                handleMap.put(h.id(), newHandle);
            }
            newHandle = handleMap.get(h.id());
            newHandle.x = h.palmPosition().getX();
            newHandle.y = h.palmPosition().getY();
            newHandle.z = h.palmPosition().getZ();
            newHandle.lastFrameId = frame.id();
        }

        Iterator<InputController.Handle> handleIterator = handleList.iterator();
        while (handleIterator.hasNext()) {
            InputController.Handle handle = handleIterator.next();
            if (handle.lastFrameId != frame.id()) {
                handleIterator.remove();
            }
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