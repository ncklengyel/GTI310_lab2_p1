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
			int max = fileInputStream.available();
			byte[] array = fileSource.pop(max-DATA_OFFSET);
			fileSink.push(array);		
			fileSink.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		int content;
		byte[] byteArray = new byte[1];
		try {
			while ((content = fileInputStream.read()) != -1) {
				
				byteArray[0] = (byte) content;
				fileSink.push(byteArray);
			}
			
			fileSink.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

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
		
		/*
		for (int i = 0; i < byteArray.length; i++) {
			System.out.println(i+1+"---"+byteArray[i]);
		}
		*/
		
		/*
			int content = 0;
			int count = 0;

			
			// while ((content = f.read()) != -1) {
			while (count <= DATA_OFFSET) {

				// System.out.println(count);
				try {
					content = fileInputStream.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (count == NUMOFCHANNELS_OFFSET) {
					numOfChannels = content;
				}

				if (count == SAMPLERATE_OFFSET) {
					sampleRate = content;
				}

				if (count == BYTERATE_OFFSET) {
					byteRate = content;
				}

				if (count == BITSPERSAMPLE_OFFSET) {
					bitsPerSample = content;
				}

				count++;

			}
		*/		

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

}
