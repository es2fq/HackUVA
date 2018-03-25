//Imports
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import java.io.IOException;
import java.io.*;
import java.net.URLConnection;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;

/**
*
* @author ex094
*/
public class VoiceLauncher {
    Configuration configuration;
    LiveSpeechRecognizer recognize;
    
    public VoiceLauncher() {
        //System.out.println(new File("").getAbsoluteFile());
        // uploadFile("Mii_Channel.mid", "Test File", "This is my test file");
        //Configuration Object
        configuration = new Configuration();
        
        // Set path to the acoustic model.
        configuration.setAcousticModelPath("resources/edu/cmu/sphinx/models/en-us/en-us");
        // Set path to the dictionary.
        configuration.setDictionaryPath("resources/edu/2020.dic");
        // Set path to the language model.
        configuration.setLanguageModelPath("resources/edu/2020.lm");
        
        try {
            recognize = new LiveSpeechRecognizer(configuration);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void upload() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date date = new Date();
        uploadFile("midifile.mid", "My Midi File "+dateFormat.format(date), "I love HackUVA!");
        GraphPanel.setToast("Uploading Midi", 5000);
    }
    public void startListening() {
        //Recognizer object, Pass the Configuration object
        try {
            //Start Recognition Process (The bool parameter clears the previous cache if true)
            recognize.startRecognition(true);
            
            //Creating SpeechResult object
            SpeechResult result;
            
            //Check if recognizer recognized the speech
            while ((result = recognize.getResult()) != null) {
                
                //Get the recognized speech
                String command = result.getHypothesis();
                //Match recognized speech with our commands
                if(command.equalsIgnoreCase("hey midi start recording")) {
                	MidiControl.startRecording();
                    System.out.println("Starting!");
                }
                else if(command.equalsIgnoreCase("hey midi stop recording")) {
                	MidiControl.stopRecording();
                    System.out.println("Stopping!");
                }
                else if(command.equalsIgnoreCase("hey midi upload file")) {
                    System.out.println("Uploading!");
                    upload();
                }
                else if(command.equalsIgnoreCase("hey midi see major")) {
                    System.out.println("Changed to C Major!");
                    MidiControl.selectedScale = 0;
                    MidiControl.makePitches(MidiControl.selectedScale);
                    MidiControl.cb.setSelectedIndex(MidiControl.selectedScale);
                    MidiControl.cb.repaint();
                    GraphPanel.setToast("Changing Key to C Major", 5000);
                }
                else if(command.equalsIgnoreCase("hey midi gee major")) {
                    System.out.println("Changed to G Major!");
                    MidiControl.selectedScale = 7;
                    MidiControl.makePitches(MidiControl.selectedScale);
                    MidiControl.cb.setSelectedIndex(MidiControl.selectedScale);
                    MidiControl.cb.repaint();
                    GraphPanel.setToast("Changing Key to G Major", 5000);
                }
                else if(command.equalsIgnoreCase("hey midi eff major")) {
                    System.out.println("Changed to F Major!");
                    MidiControl.selectedScale = 5;
                    MidiControl.makePitches(MidiControl.selectedScale);
                    MidiControl.cb.setSelectedIndex(MidiControl.selectedScale);
                    MidiControl.cb.repaint();
                    GraphPanel.setToast("Changing Key to F Major", 5000);
                }
                else if(command.equalsIgnoreCase("hey midi ee minor")) {
                    System.out.println("Changed to E Minor!");
                    MidiControl.selectedScale = 16;
                    MidiControl.makePitches(MidiControl.selectedScale);
                    MidiControl.cb.setSelectedIndex(MidiControl.selectedScale);
                    MidiControl.cb.repaint();
                    GraphPanel.setToast("Changing Key to E Minor", 5000);
                }
                else if(command.equalsIgnoreCase("hey midi bye bye")) {
                	quit();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void quit() {

    	for (MidiControl.HandleMusician m : MidiControl.handleMusicians) {
    		if (m.notePlaying) {
				MidiControl.noteOff(m.currentPitch, m.currentVelocity, m.currentInstrument);
    		}
    	}
    	System.exit(0);
    }
    public void stopListening() {
        try {
            recognize.stopRecognition();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void uploadFile(String filename, String name, String desc){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost ("https://midiweb.cfapps.us10.hana.ondemand.com/upload");
        File file = new File (filename) ;
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        StringBody myName = new StringBody(name, ContentType.MULTIPART_FORM_DATA);
        StringBody myDesc = new StringBody(desc, ContentType.MULTIPART_FORM_DATA);
        
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("midifile", fileBody);
        builder.addPart("name", myName);
        builder.addPart("desc", myDesc);
        HttpEntity entity = builder.build();
        
        post.setEntity(entity);
        try {
            CloseableHttpResponse response = httpclient.execute(post);
        } catch(Exception e){
            
        }
        return;
    }
}