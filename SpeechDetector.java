import java.io.File;
import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.Recognizer;
import com.darkprograms.speech.recognizer.GoogleResponse;
import net.sourceforge.javaflacencoder.FLACFileWriter;

public class SpeechDetector {
    private Microphone mic;
    private File file;
    public SpeechDetector() {
        mic = new Microphone(FLACFileWriter.FLAC);
        file = new File("testfile2.flac");//Name your file whatever you want
    }

    public void startRecording() {
        try {
            mic.captureAudioToFile(file);
            System.out.println("Recording...");            
        } catch (Exception ex) {//Microphone not available or some other error.
            System.out.println("ERROR: Microphone is not availible.");
            ex.printStackTrace();
            //TODO Add your error Handling Here
        }
    }

    public void stopRecording() {
        try {
            mic.close();
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        
        mic.close();//Ends recording and frees the resources
        System.out.println("Recording stopped.");
        
        Recognizer recognizer = new Recognizer(Recognizer.Languages.ENGLISH_US, "AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw"); //Specify your language here.
        //Although auto-detect is avalible, it is recommended you select your region for added accuracy.
        try {
            int maxNumOfResponses = 4;
            GoogleResponse response = recognizer.getRecognizedDataForFlac(file, maxNumOfResponses, (int)mic.getAudioFormat().getSampleRate());
            System.out.println("Google Response: " + response.getResponse());
            System.out.println("Google is " + Double.parseDouble(response.getConfidence())*100 + "% confident in"
            + " the reply");
            System.out.println("Other Possible responses are: ");
            for(String s: response.getOtherPossibleResponses()){
                System.out.println("\t" + s);
            }
        } catch (Exception ex) {
            // TODO Handle how to respond if Google cannot be contacted
            System.out.println("ERROR: Google cannot be contacted");
            ex.printStackTrace();
        }
        
        file.deleteOnExit();//Deletes the file as it is no longer necessary.
    }
}