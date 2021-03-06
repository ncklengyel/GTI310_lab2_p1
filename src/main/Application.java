package main;

import java.io.File;

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
		
		/*
		 * Si l'argument 1 est egale a --print-header
		 */
		if (args[0].equals("--print-header")) {
			
			File waveFile = new File(args[1]);
			WaveFilter waveFilter = new WaveFilter(waveFile, null);
			
			//Afficher de header du fichier passé en 2e paramètre
			waveFilter.printSourceHeader();
			
		}else{
		
			File waveFile = new File(args[0]);
		
		
		//Si le fichier reçu en paramètre existe
			if (waveFile.exists()) {
			
				WaveFilter waveFilter = new WaveFilter(waveFile,args[1]);
				//Lance la conversion
				waveFilter.process();
			
			}else {
			
				System.out.println("Le fichier "+args[0]+" n'a pas été trouver");
			
			}
			
		}
	
	}
		
}
