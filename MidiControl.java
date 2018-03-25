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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
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
	public static ArrayList<ControlInput> controlInputs = new ArrayList<ControlInput>();
	public static int numInstruments;
	public static int selectedScale = 0;

	public static JComboBox<String> cb;
	
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
		SpeechDetector sd = new SpeechDetector();
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

		cb = new JComboBox<String>(MidiControl.scaleTypes);
		cb.setVisible(true);
		
		JPanel container = new JPanel();
		container.add(graphPanel);
		container.add(cb);

		JFrame frame = new JFrame("Visualizer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(container);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
		System.out.println("How many midi devices would you like to connect? (up to 5)");
	    input = in.nextInt();
		numInstruments = Math.min(5, input);
	    
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
		}
		

		int numColumns = 5;
		int worldWidth = Core.worldXRight - Core.worldXLeft;
		int controllerDimension = worldWidth / numColumns;
		//initialize knobs
		for (int a=0; a<numColumns; a++) {
			ControlInput ci = new ControlKnob();
			ci.x = Core.worldXLeft + (controllerDimension * a);
			ci.y = 180;
			ci.xRange = controllerDimension;
			ci.yRange = controllerDimension;
			ci.instrument = 0;
			ci.controlNum = 16 + controlInputs.size();
			controlInputs.add(ci);
			controlChange(ci.controlNum, 0, ci.instrument);//reset the actual midi
		}
		for (int a=0; a<numColumns; a++) {
			ControlInput ci = new ControlSlider();
			ci.x = Core.worldXLeft + (controllerDimension * a);
			ci.y = 180 + controllerDimension;
			ci.xRange = controllerDimension;
			ci.yRange = controllerDimension;
			ci.instrument = 0;
			ci.controlNum = 16 + controlInputs.size();
			controlInputs.add(ci);
			controlChange(ci.controlNum, 0, ci.instrument);//reset the actual midi
		}
		

		frame.setVisible(true);

		while (true) {
			try {
				Thread.sleep(25);
//				if (MidiControl.numInstruments != 0 && MidiControl.receivers[MidiControl.numInstruments - 1] != null) {
//					InputController.update();
//					MidiControl.update();
//					MidiControl.graphPanel.update();
//				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
        // Remove the sample listener when done
	}
	
	
	
	public static double minZForInstruments = -50;
	public static double maxZForControlZone = -100;
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
				int pitchIndex = (int)(handleMusician.pitchHandle.y / 16);
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
				int setInstrument = -1;
				if (selectedInstrument >= 0) {
					//check to see that the selected instrument has stabilized
					if (handleMusician.pitchHandle.lastFingerChangeTime + 150 < System.currentTimeMillis() || handleMusician.instrumentSet) {
						if (handleMusician.pitchHandle.z < minZForInstruments && !handleMusician.notePlaying) {
							//don't start playing anything if the handle is in the control zone
							selectedInstrument = -1;
							handleMusician.instrumentSet = false;
						} else {
							selectedInstrument = handleMusician.currentInstrument; // keep the instrument from changing if its been stable
							handleMusician.instrumentSet = true;
						}
					} else {
						if (handleMusician.notePlaying) {
							noteOff(handleMusician.currentPitch, handleMusician.currentVelocity, handleMusician.currentInstrument);
							handleMusician.notePlaying = false;
						}
						handleMusician.instrumentSet = false;
						handleMusician.currentInstrument = selectedInstrument;
						selectedInstrument = -1;
					}
				} else {
					handleMusician.instrumentSet = false; 
				}
				
				//play the note now that we have selected the instrument for sure
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
						handleMusician.currentInstrument = setInstrument;
					}
				}
			}
		}
		
		// handle all Handles, for sliders and shit
		for (InputController.Handle handle : InputController.handles) {
			if (handle.musician.notePlaying) {
				continue;//skip handles that are controlling an instrument
			}
			double pinchThreshold = .5;
			if (handle.z < maxZForControlZone) {
				//we are in the control zone
				int pinchedItem = -1;
				handle.closestControlZone = -1;
				for (int a = 0; a < controlInputs.size(); a++) {
					ControlInput ci = controlInputs.get(a);
					if (handle.x >= ci.x && handle.y >= ci.y && handle.x < ci.x + ci.xRange && handle.y < ci.y + ci.yRange) {
						pinchedItem = a;
						handle.closestControlZone = pinchedItem;
						break;
					}
				}
				if (handle.pinchedInControlZone != -1) {
					handle.closestControlZone = handle.pinchedInControlZone;
					pinchedItem = handle.pinchedInControlZone;
				}
				if (handle.pinchAmount > pinchThreshold && handle.pinchAmountPrevious <= pinchThreshold && pinchedItem != -1) {
					if (handle.pinchedInControlZone == -1) {
						controlInputs.get(pinchedItem).previousControlVal = -1;
						handle.pinchedInControlZone = pinchedItem;
					}
				}
			} else {
				if (handle.pinchedInControlZone == -1) {
					handle.closestControlZone = -1;
				}
			}
			if (handle.pinchAmount < pinchThreshold) {
				handle.pinchedInControlZone = -1;
			}
			
			
			if (handle.pinchedInControlZone != -1) {
				controlInputs.get(handle.pinchedInControlZone).updateAndSendMidi(handle.hand);
			}
		}
		
		
		graphPanel.update();
		selectedScale = cb.getSelectedIndex();
		makePitches(selectedScale);
	}
	public static void noteOn(int pitch, int velocity, int instrument) {
		try {
			receivers[instrument].send(new ShortMessage(ShortMessage.NOTE_ON, 0, pitch, velocity), System.nanoTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void noteOff(int pitch, int velocity, int instrument) {
		try {
			receivers[instrument].send(new ShortMessage(ShortMessage.NOTE_OFF, 0, pitch, velocity), System.nanoTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void controlChange(int knob, int value, int instrument) {
		try {
			receivers[instrument].send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, knob, value), System.nanoTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void addNewPitchHandle(InputController.Handle handle) {
		//set a handle to act as a pitch handle for a musician
		MidiControl.HandleMusician musician = new MidiControl.HandleMusician(handle);
		handle.musician = musician;
		MidiControl.handleMusicians.add(musician);
	}
	public static class HandleMusician {
		public InputController.Handle pitchHandle, velocityHandle;
		int currentPitch = -1;
		int currentVelocity = 100;
		int currentInstrument = 0;
		boolean notePlaying = false, instrumentSet = false;
		public HandleMusician(InputController.Handle pitchHandle) {
			this.pitchHandle = pitchHandle;
		}
	}
	public static class ControlInput {
		public float value;
		public float previousControlVal = -1;
		public int controlNum;
		public int instrument;
		public int x, y, xRange, yRange;
		public float drawScale = 1;
		public void updateValue(Hand hand) {
			
		}
		public void updateAndSendMidi(Hand hand) {
			updateValue(hand);
			controlChange(controlNum, (int)(Math.min(Math.max(value, 0), 1) * 127), instrument);
		}
		public int getCenterX() {
			return x + xRange / 2;
		}
		public int getCenterY() {
			return y + yRange / 2;
		}
		public int getCenterZ() {
			return (int)maxZForControlZone;
		}
		public void clampValue() {
			value = clampedValue();
		}
		public float clampedValue() {
			if (value > 1) {
				return 1;
			}
			if (value < 0) {
				return 0;
			}
			return value;
		}
	}
	public static class ControlKnob extends ControlInput {
		@Override
		public void updateValue(Hand hand) {
			float controlVal = hand.palmNormal().roll();
			if (previousControlVal != -1) {
				float changeAmount = controlVal - previousControlVal;
				if (changeAmount > Math.PI) {
					changeAmount -= (float)(2 * Math.PI);
				}
				if (changeAmount < -Math.PI) {
					changeAmount += (float)(2 * Math.PI);
				}
				value -= (float)(changeAmount / 3);
			}
			
			previousControlVal = controlVal;
		}
	}
	public static class ControlSlider extends ControlInput {
		@Override
		public void updateValue(Hand hand) {
			float controlVal = hand.palmPosition().getY();
			if (previousControlVal != -1) {
				value += (controlVal - previousControlVal) / 250;
			}
			
			previousControlVal = controlVal;
		}
	}
}