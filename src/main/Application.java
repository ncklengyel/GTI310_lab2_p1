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
		
		WaveFilter waveFilter = new WaveFilter(waveFile,"/Users/nick/wevwvwewfwfewf.wav");
		//waveFilter.printSourceHeader();
		waveFilter.process();
		
		
	}
}
