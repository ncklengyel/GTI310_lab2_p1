package audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
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
	private String subChunk1ID = "fmt "; //offset 12
	private int subChunk1Size = 16; //offset 16
	private short audioFormat = 1; //offset 20
	private short numOfChannels; // offset 22
	private int sampleRate; // offset 24
	private int byteRate; // offset 28
	private short blockAlign; //offset 32
	private short bitsPerSample; // offset 34
	private String subChunk2ID = "data"; //offset 36
	private int subChunk2Size; //offset 40
	
	private int nombreDeBytesData;
	private int numSample;
	
	private FileInputStream fileInputStream;
	private FileSink fileSink;
	private FileSource fileSource;
	
	/*
	 * Constructeur
	 */
	public WaveFilter(File aWaveFile) {

		chunkID = "RIFF";
		bitsPerSample = 8;
		waveFile = aWaveFile;
		
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
		//fileSource.close();
		//fileSink.close();

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
		fileSink.push(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(subChunk2Size).array());
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
		byte[] headerOut = new byte[44];
		
		sampleRate = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 24, 28)).order(ByteOrder.LITTLE_ENDIAN).getInt();
		numOfChannels = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 22, 24)).order(ByteOrder.LITTLE_ENDIAN).getShort();
		numSample = nombreDeBytesData/1;
		subChunk2Size = numSample * numOfChannels * bitsPerSample / 8;
		chunkSize = 36 + subChunk2Size;
		blockAlign = (short) (numOfChannels * bitsPerSample / 8);
		byteRate = sampleRate * numOfChannels * bitsPerSample / 8;
		subChunk1Size = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 16, 20)).order(ByteOrder.LITTLE_ENDIAN).getInt();
		audioFormat = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 20, 22)).order(ByteOrder.LITTLE_ENDIAN).getShort();
		
		//ChunkID
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		insertInArray(headerOut, b.put(chunkID.getBytes()).array(), 0, 4);
		
		//chunksize
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut,b.putInt(chunkSize).array(), 4, 8);
		
		//Format
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		insertInArray(headerOut, b.put(format.getBytes()).array(), 8, 12);
		
		//subchunk1id
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		insertInArray(headerOut, b.put(subChunk1ID.getBytes()).array(), 12, 16);

		//Subchunk1Size
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(subChunk1Size).array(), 16, 20);
		
		//AudioFormat
		b = ByteBuffer.allocate(2);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putShort(audioFormat).array(), 20, 22);

		//NumChannels
		b = ByteBuffer.allocate(2);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putShort(numOfChannels).array(), 22, 24);

		//SampleRate
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(sampleRate).array(), 24, 28);
		
		//ByteRate
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(byteRate).array(), 28, 32);
		
		//BlockAlign
		b = ByteBuffer.allocate(2);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putShort(blockAlign).array(), 32, 34);
		
		//BitsPerSample
		b = ByteBuffer.allocate(2);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putShort(bitsPerSample).array(), 34, 36);
		
		//Subchunk2ID
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		insertInArray(headerOut, b.put(subChunk2ID.getBytes()).array(), 36, 40);

		//Subchunk2Size
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(subChunk2Size).array(), 40, 44);

		fileSink.push(headerOut);

//		for (int i = 0; i < headerOut.length; i++) {
//			System.out.println(i+": "+headerOut[i]);
//		}
		
		System.out.println("PRIVATE VARS");
		System.out.println("chunkSize: "+chunkSize);
		System.out.println("subChunk1Size: "+subChunk1Size);
		System.out.println("AudioFormat: "+audioFormat);
		System.out.println("numOfChannels: "+numOfChannels);
		System.out.println("sampleRate: "+sampleRate);
		System.out.println("ByteRate: "+ byteRate);
		System.out.println("blockAlign: "+blockAlign);
		System.out.println("bitsPerSample: "+bitsPerSample);
		System.out.println("subChunk2Size: "+subChunk2Size);
		System.out.println("END\n");
		
		
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
	
	private void insertInArray(byte[ ]tabDestination,byte[] tabSource, int borneInf, int bornSup){
		
		int count = 0;
		for (int i = borneInf; i < bornSup; i++) {
			tabDestination[i] = tabSource[count];
			count++;
		}
		
		
		
	}
	
	public void printSourceHeader(){
		
		System.out.println("--HEADER OF "+waveFile.getName()+" ---");
		
		byte[] headerSource = fileSource.pop(44);
		
		byte[] chunkIDarray = Arrays.copyOfRange(headerSource, 0, 4);
		String chunkID = new String(chunkIDarray,StandardCharsets.US_ASCII); 
		System.out.println("ChunkID: " + chunkID);
		
		byte[] chunkSizeArray = Arrays.copyOfRange(headerSource, 4, 8);
		int chunckSize = ByteBuffer.wrap(chunkSizeArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
		System.out.println("ChunckSize: " + chunckSize);
		
		byte[] formatArray = Arrays.copyOfRange(headerSource, 8, 12);
		String format = new String(formatArray,StandardCharsets.US_ASCII); 
		System.out.println("format: " + format);
		
		byte[] chunkSize1IDarray = Arrays.copyOfRange(headerSource, 12, 16);
		String chunkSize1ID = new String(chunkSize1IDarray,StandardCharsets.US_ASCII); 
		System.out.println("chunkSize1ID: " + chunkSize1ID);
		
		byte[] subChunk1SizeArray = Arrays.copyOfRange(headerSource, 16, 20);
		int subChunk1Size =  ByteBuffer.wrap(subChunk1SizeArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
		System.out.println("SubChunk1Size: " + subChunk1Size);
		
		byte[] audioFormatArray = Arrays.copyOfRange(headerSource, 20, 22);
		short audioFormat =  ByteBuffer.wrap(audioFormatArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("AudioFormat: " + audioFormat);
		
		byte[] numChannelsArray = Arrays.copyOfRange(headerSource, 22, 24);
		short numChannels =  ByteBuffer.wrap(numChannelsArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("NumChannels: " + numChannels);
		
		byte[] sampleRateArray = Arrays.copyOfRange(headerSource, 24, 28);
		int sampleRate = ByteBuffer.wrap(sampleRateArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
		System.out.println("SampleRate: " + sampleRate);
		
		byte[] byteRateArray = Arrays.copyOfRange(headerSource, 28, 32);
		int byteRate = ByteBuffer.wrap(byteRateArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
		System.out.println("ByteRate: " + byteRate);
		
		byte[] blockAlignArray = Arrays.copyOfRange(headerSource, 32, 34);
		short blockAlign =  ByteBuffer.wrap(blockAlignArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("BlockAlign: " + blockAlign);
		
		byte[] bitsPerSampleArray = Arrays.copyOfRange(headerSource, 34, 36);
		short bitsPerSample =  ByteBuffer.wrap(bitsPerSampleArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("BitsPerSample: " + bitsPerSample);
		
		byte[] chunkSize2IDarray = Arrays.copyOfRange(headerSource, 36, 40);
		String chunkSize2ID = new String(chunkSize2IDarray,StandardCharsets.US_ASCII); 
		System.out.println("ChunkSize2ID: " + chunkSize2ID);
		
		byte[] subChunk2SizeArray = Arrays.copyOfRange(headerSource, 36, 44);
		short subChunk2Size =  ByteBuffer.wrap(subChunk2SizeArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
		System.out.println("SubChunk2Size: " + subChunk2Size);
		
		fileSource.close();
		
	}
	
	private byte[] convertToEightBits2(int[] tab){

		byte tabByte[] = new byte[tab.length];
		
		for (int i = 0; i < tab.length; i++) {
		
			tabByte[i] = (byte)tab[i];
		}
		
		return tabByte;
		
	}

}
