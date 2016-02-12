package main;

import java.io.File;
import java.io.FileFilter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import audio.AudioFilter;
import audio.WaveFilter;

public class Application {

	/**
	 * Launch the application
	 * 
	 * @param args
	 *            This parameter is ignored
	 */
	public static void main(String args[]) {

		if (args.length!=2) {
			System.out.println("Le programme besoin de 2 arguments\nUn fichier en entrée et d'un fichier de sorti");
			System.exit(0);
		}
		
		File waveFile = new File(args[0]);
		
		WaveFilter waveFilter = new WaveFilter(waveFile);
		//waveFilter.printHeader();
		waveFilter.process();
		
		/*
		
		final String MAC = "Mac OS X";
		JFileChooser fileChooser = null;

		String homeDirectory = System.getProperty("user.home");
		String os = System.getProperty("os.name");

		if (os.equals(MAC)) {

			System.out.println(os);
			System.out.println(homeDirectory);
			
			fileChooser = new JFileChooser(homeDirectory + "/Documents/workspace");

		} else {

			fileChooser = new JFileChooser();

		}

		System.out.println("Audio Resample project!\n");

		File file = null;
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Wave file", "wav");
		fileChooser.setFileFilter(filter);

		int result = fileChooser.showOpenDialog(fileChooser);
		if (result == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			System.out.println("Selected file: " + file.getAbsolutePath());
		}

		if (file != null) {
			WaveFilter wave = new WaveFilter(file);
			wave.printHeader();
		}
		
		*/

	}
}
