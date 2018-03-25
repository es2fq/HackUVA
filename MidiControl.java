import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.leapmotion.leap.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;
import java.awt.Dimension;
import javax.swing.JFrame;
import java.util.Arrays;

/*
 * 
 * TO COMPILE:
 * javac -cp "LeapJava.jar" MidiControl.java
 * 
 * 
 */

public class MidiControl {
	public static Receiver[] receivers = new Receiver[5];
	public static GraphPanel graphPanel;
	public static ArrayList<HandleMusician> handleMusicians = new ArrayList<HandleMusician>();
	public static int numInstruments;
	
	public static boolean[] enabledPitches = new boolean[128];
	public static int notePitches[] = new int[128];
	public static int notePitchesMaxIndex = 127;
	public static MidiMessage makeMidiMessage() {
		return null;
	}
	public static String[] scaleTypes = new String[] {"C Maj", "C# Maj", "D Maj", "D# Maj", "E Maj", "F Maj", "F# Maj", "G Maj", "G# Maj", "A Maj", "A# Maj", "B Maj",
			"C Min", "C# Min", "D Min", "D# Min", "E Min", "F Min", "F# Min", "G Min", "G# Min", "A Min", "A# Min", "B Min"};
	public static void makePitches(int s) {
		boolean notes[];
		if (s >= 24 || s < 0) {
			notes = new boolean[] {true, true, true, true, true, true, true, true, true, true, true, true};
		} else if (s >= 12) {
			//minor scales
			notes = new boolean[] {true, false, true, true, false, true, false, true, false, true, false, true};
		} else {
			//major scales
			notes = new boolean[] {true, false, true, false, true, true, false, true, false, true, false, true};
		}
		
		int scaleIndex = 0;
		int a = 60 + (s % 12);
		while (a > 0) {
			a -= 12;
		}
		int notesIndex = 0;
		for (; a<128; a++) {
			if (a > 0) {
				enabledPitches[a] = notes[scaleIndex];
				if (notes[scaleIndex]) {
					notePitches[notesIndex] = a;
					notePitchesMaxIndex = notesIndex;
					notesIndex ++;
				}
			}
			scaleIndex ++;
			if (scaleIndex >= 12) {
				scaleIndex = 0;
			}
		}
	}
	public static void main(String[] args) throws IOException, MidiUnavailableException, InvalidMidiDataException
	{
		makePitches(0);//TODO: remove this test code. currently set the scale to c major
		
		
        CoreListener listener = new CoreListener();
		Controller controller = new Controller();

        // Have the sample listener receive events from the controller
        controller.addListener(listener);

		//Select Device
		Scanner in = new Scanner(System.in);
		int input;
		
		int maxDataPoints = 40;
		int maxScore = 10;
		
		graphPanel = new GraphPanel();
		graphPanel.setPreferredSize(new Dimension(800, 600));
		JFrame frame = new JFrame("Visualizer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(graphPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
		System.out.println("How many midi devices would you like to connect?");
	    input = in.nextInt();
		numInstruments = input;
	    
		for (int a=0; a<numInstruments; a++) {
			System.out.println("\n\nSelect instrument " + a);
		    int i = 0;
			for (MidiDevice.Info info : midiInfo) {
				System.out.println(i + ": " +info.getName());
				i+=1;
			}
		    input = in.nextInt();
			if(input == -1)
			{
				numInstruments = a;
				break;
			}
			//Get Device and Open it
			MidiDevice selectedDevice = MidiSystem.getMidiDevice(midiInfo[input]);
			selectedDevice.open();
			receivers[a] = selectedDevice.getReceiver();
		    if (a > 0) {
		    	System.out.println(receivers[a] == receivers[a-1]);
		    }
		}

		frame.setVisible(true);

		while (true) {
			try {
				Thread.sleep(25);
				if (MidiControl.numInstruments != 0 && MidiControl.receivers[MidiControl.numInstruments - 1] != null) {
					InputController.update();
					MidiControl.update();
					MidiControl.graphPanel.update();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
        // Remove the sample listener when done
	}
	public static void update() {
		Iterator<HandleMusician> it = handleMusicians.iterator();
		while (it.hasNext()) {
			HandleMusician handleMusician = it.next();
			if (handleMusician.pitchHandle == null || !handleMusician.pitchHandle.isValid) {
				//remove musicians with invalid pitch handles, like if the users hand moves away from the leap motion
				it.remove();
				if (handleMusician.notePlaying) {
					noteOff(handleMusician.currentPitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
				}
			} else {
				int pitchIndex = (int)(handleMusician.pitchHandle.y / 10);
				if (pitchIndex < 0) {
					pitchIndex = 0;
				}
				if (pitchIndex > notePitchesMaxIndex) {
					pitchIndex = notePitchesMaxIndex;
				}
				int pitch = notePitches[pitchIndex];
				
				int selectedInstrument = handleMusician.pitchHandle.fingers - 1;
				if (selectedInstrument > numInstruments - 1) {
					selectedInstrument = numInstruments - 1;
				}
				//play the note
				if (selectedInstrument >= 0) {
					if (selectedInstrument == handleMusician.currentInstrument) { // if you have teh same instrument
						if (handleMusician.notePlaying) {
							if (pitch != handleMusician.currentPitch) {
								noteOff(handleMusician.currentPitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
								noteOn(pitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
								handleMusician.currentPitch = pitch;
							}
						} else {
							noteOn(pitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
							handleMusician.currentPitch = pitch;
							handleMusician.notePlaying = true;
						}
					} else {
						if (handleMusician.notePlaying) {
							noteOff(handleMusician.currentPitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
							handleMusician.currentInstrument = selectedInstrument;
							noteOn(pitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
							handleMusician.currentPitch = pitch;
						} else {
							handleMusician.currentInstrument = selectedInstrument;
							noteOn(pitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
							handleMusician.currentPitch = pitch;
							handleMusician.notePlaying = true;
						}
					}
				} else {
					if (handleMusician.notePlaying) {
						noteOff(handleMusician.currentPitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
						handleMusician.notePlaying = false;
						handleMusician.currentPitch = pitch;
					}
				}
			}
			
		}
		
		graphPanel.update();
	}
	public static void noteOn(int pitch, int velocity, int instrument) {
		try {
			System.out.println("Play note: "+instrument+", "+pitch+", "+velocity);
			receivers[instrument].send(new ShortMessage(ShortMessage.NOTE_ON, 0, pitch, velocity), System.nanoTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void noteOff(int pitch, int velocity, int instrument) {
		try {
			System.out.println("Stop note: "+instrument+", "+pitch+", "+velocity);
			receivers[instrument].send(new ShortMessage(ShortMessage.NOTE_OFF, 0, pitch, velocity), System.nanoTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void addNewPitchHandle(InputController.Handle handle) {
		//set a handle to act as a pitch handle for a musician
		MidiControl.handleMusicians.add(new MidiControl.HandleMusician(handle));
	}
	public static class HandleMusician {
		public InputController.Handle pitchHandle, velocityHandle;
		int currentPitch = -1;
		int currentVelocity = 100;
		int currentInstrument = 0;
		boolean notePlaying = false;
		public HandleMusician(InputController.Handle pitchHandle) {
			this.pitchHandle = pitchHandle;
		}
	}
}