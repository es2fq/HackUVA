import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

/*
 * 
 * TO COMPILE:
 * javac -cp "LeapJava.jar" MidiControl.java
 * 
 * 
 */

public class MidiControl {
	private static Receiver receiver;
	public static ArrayList<HandleMusician> handleMusicians = new ArrayList<HandleMusician>();
	public static MidiMessage makeMidiMessage() {
		return null;
	}
	public static void main(String[] args) throws IOException, MidiUnavailableException, InvalidMidiDataException
	{
		while (true) {
			//List Midi Devices
			MidiDevice.Info[] midiInfo = MidiSystem.getMidiDeviceInfo();
			int i = 0;
			for (MidiDevice.Info info : midiInfo) {
				System.out.println(i + ": " +info.getName());
				i+=1;
			}
			//Select Device
			Scanner in = new Scanner(System.in);
			int input = in.nextInt();

			if(input == -1)
			{
				break;
			}
			//Get Device and Open it
			MidiDevice selectedDevice = MidiSystem.getMidiDevice(midiInfo[input]);
			selectedDevice.open();
			//Get Sequencer and Receiver, load the File and start playing
			receiver = selectedDevice.getReceiver();


			while (true) {
				try {
					Thread.sleep(25);
				} catch (Exception e) {
					e.printStackTrace();
				}
				InputController.update();
				update();
			}
			
		}
	}
	public static void update() {
		Iterator<HandleMusician> it = handleMusicians.iterator();
		while (it.hasNext()) {
			HandleMusician handleMusician = it.next();
			if (handleMusician.pitchHandle == null || !handleMusician.pitchHandle.isValid) {
				//remove musicians with invalid pitch handles, like if the users hand moves away from the leap motion
				it.remove();
				if (handleMusician.notePlaying) {
					noteOff(handleMusician.currentPitch, handleMusician.currentVelocity);
				}
			} else {
				int pitch = (int)(handleMusician.pitchHandle.y / 10);
				pitch = pitch % 128;
				if (handleMusician.notePlaying) {
					if (pitch != handleMusician.currentPitch) {
						noteOff(handleMusician.currentPitch, handleMusician.currentVelocity);
						noteOn(pitch, handleMusician.currentVelocity);
						handleMusician.currentPitch = pitch;
					}
				} else {
					noteOn(pitch, handleMusician.currentVelocity);
					handleMusician.currentPitch = pitch;
					handleMusician.notePlaying = true;
				}
			}
			
		}
		
		
		
		
		
	}
	public static void noteOn(int pitch, int velocity) {
		try {
			receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, pitch, velocity), System.nanoTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void noteOff(int pitch, int velocity) {
		try {
			receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, pitch, velocity), System.nanoTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void addNewPitchHandle(InputController.Handle handle) {
		//set a handle to act as a pitch handle for a musician
		MidiControl.handleMusicians.add(new MidiControl.HandleMusician(handle));
	}
	private static class HandleMusician {
		public InputController.Handle pitchHandle, velocityHandle;
		int currentPitch = -1;
		int currentVelocity = 100;
		boolean notePlaying = false;
		public HandleMusician(InputController.Handle pitchHandle) {
			this.pitchHandle = pitchHandle;
		}
	}
}