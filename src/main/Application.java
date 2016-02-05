package main;
import java.io.File;
import javax.swing.JFileChooser;
import audio.WaveFilter;

public class Application {

	/**
	 * Launch the application
	 * @param args This parameter is ignored
	 */
	public static void main(String args[]) {
		System.out.println("Audio Resample project!\n");
		
		File file = null;
		JFileChooser fileChooser = new JFileChooser();
		
		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		    System.out.println("Selected file: " + file.getAbsolutePath());
		}
		
		if (file != null) {
			WaveFilter wave = new WaveFilter(file);
			wave.printHeader();
		}
		
		
		
	}
}
