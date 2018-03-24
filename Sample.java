import com.leapmotion.leap.*;
import java.io.IOException;

class SampleListener extends Listener {
    
    public void onConnect(Controller controller) {
        System.out.println("Connected");
    }
    
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();
        
        System.out.println("Frame id: " + frame.id()
        + ", timestamp: " + frame.timestamp()
        + ", hands: " + frame.hands().count()
        + ", fingers: " + frame.fingers().count());
    }
}

class Sample {
    public static void main(String[] args) {
        // Create a sample listener and controller
        SampleListener listener = new SampleListener();
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