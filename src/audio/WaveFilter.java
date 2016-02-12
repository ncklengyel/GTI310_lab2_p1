package audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.FileSink;
import io.FileSource;

public class WaveFilter implements AudioFilter {
	// Constantes
	private final int NUMOFCHANNELS_OFFSET = 22;
	private final int SAMPLERATE_OFFSET = 24;
	private final int BYTERATE_OFFSET = 28;
	private final int BITSPERSAMPLE_OFFSET = 34;
	private final int DATA_OFFSET = 44;

	// Variable privées
	private File waveFile;
	private int numOfChannels; // offset 22
	private int sampleRate; // offset 24
	private int byteRate; // offset 28
	private int bitsPerSample; // offset 34
	
	
	private byte[] byteArray = new byte[1];
	
	private FileInputStream fileInputStream;
	private FileSink fileSink;
	private FileSource fileSource;
	
	/*
	 * Constructeur
	 */
	public WaveFilter(File aWaveFile) {

		bitsPerSample = 8;
		
		
		waveFile = aWaveFile;
		
		try {
			fileSource = new FileSource(waveFile.getAbsolutePath());
			fileSink = new FileSink("/home/nick/test.wav");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		try {
			fileInputStream = new FileInputStream(waveFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void process() {
		
		buildHeader();
		
		try {
			
			int nombreDeBytes = fileInputStream.available()-DATA_OFFSET;
			
			int nombreShort = nombreDeBytes/2;
			
			int multiple = trouverMultiple(nombreShort);
			
			int length = nombreShort/multiple;
			
			for (int i = 0; i < length; i++) {
				
				short[] tabShort = fileSource.popShort(multiple);
				fileSink.push(convertToEightBits(tabShort));
				
			}
			
			//byte[] array = fileSource.pop(max-DATA_OFFSET);
			//fileSink.push(array);		
			fileSink.close();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// methode qui build le header d'un fichier wave
	private void buildHeader() {
		
		byte[] byteRate = new byte [1];
		byteRate[0] = 8;
		
		byte[] byteArray = null;
		//
		byteArray = fileSource.pop(28);
		
		fileSink.push(byteArray);
		
		fileSource.pop(1);
		fileSink.push(byteRate);
		
		byteArray = fileSource.pop(12);
		fileSink.push(byteArray);
		

	}

	/*
	 * Accesseurs et mutateurs
	 */
	public int getBitsPerSample() {
		return bitsPerSample;
	}

	public void setBitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}

	/*
	 * Méthode permettant d'afficher le header du fichier passé en paramètre
	 */
	public void printHeader() {

		System.out.println("---Header of " + waveFile.getName() + "---");
		System.out.println("Number of channels:" + numOfChannels);
		System.out.println("Sample rate:" + sampleRate);
		System.out.println("ByteRate:" + byteRate);
		System.out.println("Bits per sample:" + byteRate);

	}
	
	private int trouverMultiple(int aNumber){
		
		int i = 9;
		
		while (aNumber % i !=0) {
			
			i++;
			
		}
		
		return i;
		
	}
	
	private byte[] convertToEightBits(short[] tab){
		System.out.println(tab.length);
		byte tabByte[] = new byte[tab.length];
		
		for (int i = 0; i < tab.length; i++) {
			tabByte[i] = (byte) (tab[i] >> 8);
		}
		
		return tabByte;
		
	}

}
