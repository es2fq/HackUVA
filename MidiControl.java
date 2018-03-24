import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/*
 * 
 * TO COMPILE:
 * javac -cp "LeapJava.jar" MidiControl.java
 * 
 * 
 */

public class MidiControl {
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
			Receiver receiver = selectedDevice.getReceiver();


			boolean m = true;
			boolean s = false;
			if (m) {
				for (int a = 1; a < 126; a++) {
					receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, a, 100), System.nanoTime());
					try {
					Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
					receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, a, 100), System.nanoTime());
				}
			} else if (s) {
				Sequencer sequencer = MidiSystem.getSequencer();
				sequencer.getTransmitter().setReceiver(receiver);
				sequencer.open();
				sequencer.setSequence(MidiSystem.getSequence(new File("D:\\SomeWhereFarAwayStem.mid")));
				sequencer.start();

				sequencer.addMetaEventListener(new MetaEventListener() {
					@Override
					public void meta(MetaMessage meta) {
						if(meta.getType() == 47)
						{
							sequencer.close();
						}
					}
				});
				while (sequencer.isOpen()){}
				selectedDevice.close();
			}
break;
		}
	}
}