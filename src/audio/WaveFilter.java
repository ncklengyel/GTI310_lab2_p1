package audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.stream.FileImageInputStream;

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

	public WaveFilter(File aWaveFile) {

		waveFile = aWaveFile;

		buildHeader(waveFile);

		if (bitsPerSample != 16) {
			System.out.println("le fichier audio n'a pas 16bits par echantillon");
		}

	}

	@Override
	public void process() {
		// TODO Auto-generated method stub

	}

	// methode qui build le header d'un fichier wave
	private void buildHeader(File aWaveFile) {

		try {
			FileImageInputStream f = new FileImageInputStream(aWaveFile);

			int content = 0;
			int count = 0;

			// while ((content = f.read()) != -1) {
			while (count <= DATA_OFFSET) {

				// System.out.println(count);
				content = f.read();

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

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getBitsPerSample() {
		return bitsPerSample;
	}

	public void setBitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}

	public void printHeader() {

		System.out.println("---Header of " + waveFile.getName() + "---");
		System.out.println("Number of channels:" + numOfChannels);
		System.out.println("Sample rate:" + sampleRate);
		System.out.println("ByteRate:" + byteRate);
		System.out.println("Bits per sample:" + byteRate);

	}

}
