package audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
	
	private String chunkID = "RIFF"; //offset 0
	private int chunkSize; //offset 4
	private String format = "WAVE"; //offset 8
	private String subChunk1ID = "fmt"; //offset 12
	private int subChunk1Size = 16; //offset 16
	private short audioFormat = 1; //offset 20
	private short numOfChannels; // offset 22
	private int sampleRate; // offset 24
	private int byteRate; // offset 28
	private short blockAlign; //offset 32
	private short bitsPerSample; // offset 34
	private String subChunkSize = "data"; //offset 36
	private int subChunk2Size; //offset 40
	
	private int nombreDeBytesData;
	private int numSample;
	
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
		chunkSize = 0;
		
		try {
			
			//Je vais chercher le path de mon fichier wave
			fileSource = new FileSource(waveFile.getAbsolutePath());
			
			//Emplacement pour l'écriture du fichier converti en 8 bits par echantillion
			//si le systeme d'explotation est une version de windows (j'utilise un \)
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				fileSink = new FileSink(System.getProperty("user.home")+"\\"+"convertedAudio.wav");
				
			//sinon, l'os est unix-like (j'utilise un /)
			//Je prends pour aquis que l'utilisateur utilise Windows ou unix-like comme OS
			}else{
				fileSink = new FileSink(System.getProperty("user.home")+"/convertedAudio.wav");
			}
			
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
		
		try {
			nombreDeBytesData = fileInputStream.available()-DATA_OFFSET;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		buildHeader2();

	}

	@Override
	public void process() {
				
			/*
			 * La façon donc je procede pour convertir le fichier audio:
			 * Une fois que mon header est build je regarde le nombre de bytes qu'il me reste a lire.
			 *Ensuite je divise ce nombre de bytes par 2 afin d'obtenir le nombre de shorts.
			 *Je trouve un diviseur  du nombre de short afin de pourvoir traité mon fichier par petit array
			 *Je divise le nombre de short par le diviseur trouver afin de savoir combien de fois je traite mes petit arrays
			 */
			
			//nombre de shorts que mon fichier audio contient
			int nombreShort = nombreDeBytesData/2;

			//je trouver un diviseur du nombre de short
			//Je sectionne mon fichier audio en petits arrays afin de le traiter
			int multiple = trouverMultiple(nombreShort);
			
			//nombre de fois que je dois traiter les petit arrays
			int length = nombreShort/multiple;
			
			//Pour tout mon fichier audio
			for (int i = 0; i < length; i++) {
				
				//Lecture du fichier source, je pop dans un array
				int[] tabData = fileSource.popShort(multiple);
				
				//Converti en 8 bits mon petit array et le push dans mon fichier
				//fileSink.pushShort(convertToEightBits(tabData));
				fileSink.push(convertToEightBits2(tabData));
				
			}
		
			//Je ferme mon fichier
			fileSink.close();	

	}

	// methode qui build le header d'un fichier wave
	private void buildHeader() {

	/*	
		fileSink.push(fileSource.pop(34));
		
		fileSink.push(ByteBuffer.allocate(2).putShort(bitsPerSample).order(ByteOrder.LITTLE_ENDIAN).array());
		fileSource.pop(2);
		
		fileSink.push(fileSource.pop(8));
		
	}
	*/
		
		
		fileSink.push(fileSource.pop(4));
		
		//4
		//fileSink.push(ByteBuffer.allocate(4).putInt(nombreDeBytesData).order(ByteOrder.LITTLE_ENDIAN).array());
		//fileSource.pop(4);
		
		fileSink.push(fileSource.pop(4));
		
		//8
		fileSink.push(fileSource.pop(14));
		
		//22
		numOfChannels = ByteBuffer.wrap(fileSource.pop(2)).order(ByteOrder.LITTLE_ENDIAN).getShort();
		fileSink.push(ByteBuffer.allocate(2).putShort(numOfChannels).order(ByteOrder.LITTLE_ENDIAN).array());
		
		//24
		sampleRate = ByteBuffer.wrap(fileSource.pop(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
		fileSink.push(ByteBuffer.allocate(4).putInt(sampleRate).order(ByteOrder.LITTLE_ENDIAN).array());
		
		//28
		byteRate = sampleRate * numOfChannels;
		fileSink.push(ByteBuffer.allocate(4).putInt(byteRate).order(ByteOrder.LITTLE_ENDIAN).array());
		fileSource.pop(4);
		
		//32
		blockAlign = numOfChannels;
		fileSink.push(ByteBuffer.allocate(2).putShort(blockAlign).order(ByteOrder.LITTLE_ENDIAN).array());
		fileSource.pop(2);
		
		//34
		fileSink.push(ByteBuffer.allocate(2).putShort(bitsPerSample).order(ByteOrder.LITTLE_ENDIAN).array());
		fileSource.pop(2);
		
		//36
		fileSink.push(fileSource.pop(4));
		
		//40
		subChunk2Size = nombreDeBytesData * numOfChannels;
		fileSink.push(ByteBuffer.allocate(4).putInt(subChunk2Size).order(ByteOrder.LITTLE_ENDIAN).array());
		fileSource.pop(4);

	}


	/*
	 * Méthode permettant d'afficher le header du fichier passé en paramètre
	 */
	public void printHeader() {

		System.out.println("---Header of " + waveFile.getName() + "---");
		//System.out.println("ChunkID: "+chunkID);
		System.out.println("ChunkSize: "+chunkSize);
		System.out.println("Number of channels:" + numOfChannels);
		System.out.println("Sample rate:" + sampleRate);
		System.out.println("ByteRate:" + byteRate);
		System.out.println("Bits per sample:" + bitsPerSample);

	}
	
	public void buildHeader2(){
		/*
		 * Push directement
		 * 
		 * chunkID
		 * Format
		 * subChunk1ID
		 * subChunk2ID
		 * 
		 * 
		 */
		
		
		byte[] headerSource = fileSource.pop(44);
		
		sampleRate = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 24, 28)).order(ByteOrder.LITTLE_ENDIAN).getInt();
		numOfChannels = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 22, 24)).order(ByteOrder.LITTLE_ENDIAN).getShort();
		numSample = nombreDeBytesData/1;
		subChunk2Size = numSample * numOfChannels * bitsPerSample / 8;
		chunkSize = 36 + subChunk2Size;
		blockAlign = (short) (numOfChannels * bitsPerSample / 8);
		byteRate = sampleRate * numOfChannels * bitsPerSample / 8;
		subChunk1Size = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 16, 20)).order(ByteOrder.LITTLE_ENDIAN).getInt();
		audioFormat = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 20, 22)).order(ByteOrder.LITTLE_ENDIAN).getShort();
		
		System.out.println("sampleRate: "+sampleRate);
		System.out.println("numOfChannels: "+numOfChannels);
		System.out.println("numSample: "+numSample);
		System.out.println("subChunk2Size: "+subChunk2Size);
		System.out.println("chunkSize: "+chunkSize);
		System.out.println("blockAlign: "+blockAlign);
		System.out.println("byteRate: "+byteRate);
		System.out.println("subChunk1Size: "+subChunk1Size);
		System.out.println("audioFormat: "+audioFormat);


		
		
		
		
		
	}
	
	private int trouverMultiple(int aNumber){
		
		int i = 9;
		
		while (aNumber % i !=0) {
			
			i++;
			
		}
		
		return i;
		
	}
	
	/*
	 * Methode qui qui permet de cast les valeurs d'un array de int en byte
	 */
	private int[] convertToEightBits(int[] tab){

		int tabInt[] = new int[tab.length];
		
		for (int i = 0; i < tab.length; i++) {
		
			tabInt[i] = (byte)tab[i];
		}
		
		return tabInt;
		
	}
	
	private byte[] convertToEightBits2(int[] tab){

		byte tabByte[] = new byte[tab.length];
		
		for (int i = 0; i < tab.length; i++) {
		
			tabByte[i] = (byte)tab[i];
		}
		
		return tabByte;
		
	}

}
