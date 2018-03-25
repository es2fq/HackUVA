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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import java.util.Arrays;
import java.lang.Thread;

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
	
	public static Sequence s;
	public static Track t;
	public static int midiTicksPerFrame = 30;
	
	private static long startTime;
	
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
	public static boolean startRecording() throws InvalidMidiDataException {
		if (isRecording) {return false;}

		
		s = new Sequence(javax.sound.midi.Sequence.	SMPTE_30, midiTicksPerFrame);
		t = s.createTrack();

		isRecording = true;
		byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
		SysexMessage sm = new SysexMessage();
		sm.setMessage(b, 6);
		MidiEvent me = new MidiEvent(sm,(long)0);
		t.add(me);
		
		MetaMessage mt = new MetaMessage();
		byte[] bt = {0x02, (byte)0x00, 0x00};
		mt.setMessage(0x51 ,bt, 3);
		me = new MidiEvent(mt,(long)0);
		t.add(me);
		
		
		//****  set track name (meta event)  ****
		mt = new MetaMessage();
		String TrackName = new String("midifile track");
		mt.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
		me = new MidiEvent(mt,(long)0);
		t.add(me);

		
		//****  set omni on  ****
		ShortMessage mm = new ShortMessage();
		mm.setMessage(0xB0, 0x7D,0x00);
		me = new MidiEvent(mm,(long)0);
		t.add(me);
		
		//****  set poly on  ****
		mm = new ShortMessage();
		mm.setMessage(0xB0, 0x7F,0x00);
		me = new MidiEvent(mm,(long)0);
		t.add(me);
		
		//****  set instrument to Piano  ****
		mm = new ShortMessage();
		mm.setMessage(0xC0, 0x00, 0x00);
		me = new MidiEvent(mm,(long)0);
		t.add(me);

		isRecording = true;

		startTime = System.currentTimeMillis();
		return true;
	}
	public static boolean stopRecording() throws InvalidMidiDataException, IOException{
		if (!isRecording) {
			return false;
		}

	//wait for no midi notes to be played
		MetaMessage mt = new MetaMessage();
        byte[] bet = {}; // empty array
		mt.setMessage(0x2F,bet,0);
		MidiEvent me = new MidiEvent(mt, (long)((double)(System.currentTimeMillis() - startTime)/1000*30*midiTicksPerFrame));

		t.add(me);
		File f = new File("midifile.mid");
		isRecording = false;
		MidiSystem.write(s, 1, f);
		System.out.println("done");
		return true;
	}
	public static Controller controller;
	public static boolean isRecording = false;
	
	public static boolean initialized = false;
	public static void main(String[] args) throws IOException, MidiUnavailableException, InvalidMidiDataException
	{
		SpeechDetector sd = new SpeechDetector();
		CoreListener listener = new CoreListener();
		controller = new Controller();
		VoiceLauncher voiceLauncher = new VoiceLauncher();

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
		frame.getContentPane().setBackground(Color.BLACK);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		

		
		Runnable r = new Runnable() {
			public void run() {
				try {
					voiceLauncher.startListening();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
		
		
		Thread thread = new Thread(r);
		thread.start();
		
		if (midiInfo.length > 10) {//jasons computer
			numInstruments = 5;
			for (int a=0; a<5; a++) {
				MidiDevice selectedDevice = MidiSystem.getMidiDevice(midiInfo[8 + a]);
				selectedDevice.open();
				receivers[a] = selectedDevice.getReceiver();
			}
		} else {
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
			ci.yRange = 250;
			ci.instrument = 0;
			ci.controlNum = 16 + controlInputs.size();
			controlInputs.add(ci);
			controlChange(ci.controlNum, 0, ci.instrument);//reset the actual midi
		}
		for (int a=0; a<numColumns; a++) {
			ControlInput ci = new ControlSlider();
			ci.x = Core.worldXLeft + (controllerDimension * a);
			ci.y = 180 + 100 + controllerDimension;
			ci.xRange = controllerDimension;
			ci.yRange = 250;
			ci.instrument = 0;
			ci.controlNum = 16 + controlInputs.size();
			controlInputs.add(ci);
			controlChange(ci.controlNum, 0, ci.instrument);//reset the actual midi
		}
		
		
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
				ShortMessage sm = new ShortMessage(ShortMessage.NOTE_ON, 0, pitch, velocity);
				receivers[instrument].send(sm, System.nanoTime());
				if (isRecording) {
					t.add(new MidiEvent(sm, (long) ((double)(System.currentTimeMillis() - startTime)/1000*30*midiTicksPerFrame)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public static void noteOff(int pitch, int velocity, int instrument) {
			try {
				ShortMessage sm = new ShortMessage(ShortMessage.NOTE_OFF, 0, pitch, velocity);
				receivers[instrument].send(sm, System.nanoTime());
				if (isRecording) {
					t.add(new MidiEvent(sm, (long) ((double)(System.currentTimeMillis() - startTime)/1000*30*midiTicksPerFrame)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		public static void controlChange(int knob, int value, int instrument) {
			try {
				ShortMessage sm = new ShortMessage(ShortMessage.CONTROL_CHANGE, 0, knob, value);
				receivers[instrument].send(sm, System.nanoTime());
				if (isRecording) {
					t.add(new MidiEvent(sm, (long) ((double)(System.currentTimeMillis() - startTime)/1000*30*midiTicksPerFrame)));
				}
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
				return (int)maxZForControlZone - 20;
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
