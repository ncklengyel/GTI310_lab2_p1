package audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.stream.FileImageInputStream;

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
	
	private FileInputStream fileInputStream;
	
	private byte[] byteArray = new byte[1];
	
	private FileSink fileSink;
	
	private FileSource fileSource;
	
	/*
	 * Constructeur
	 */
	public WaveFilter(File aWaveFile) {

		waveFile = aWaveFile;
		
		try {
			fileSource = new FileSource(waveFile.getAbsolutePath());
			fileSink = new FileSink("/Users/am37580/test.wav");
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
		
		
		buildHeader();		

		if (bitsPerSample != 16) {
			System.out.println("le fichier audio n'a pas 16bits par echantillon");
		}

	}

	@Override
	public void process() {
		
		short content = 0;
		int count = 0;
		
		try {
			
			while (fileInputStream.read() != -1) {
				
				
				
				if(count>44){
				
					content = fileInputStream.readShort();
				
					byteArray[0] = (byte) content;
				
					//System.out.println(byteArray[0]);
					fileSink.push(byteArray);
				
				}else{
					
					byteArray[0] = (byte) fileInputStream.read();
					fileSink.push(byteArray);
					
				}
				
				count++;
				
			}
			
			fileSink.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	// methode qui build le header d'un fichier wave
	private void buildHeader() {
		
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
