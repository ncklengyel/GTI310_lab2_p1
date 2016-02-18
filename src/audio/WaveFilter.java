package audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
	private int audioFormat = 1; //offset 20
	private int numOfChannels; // offset 22
	private int sampleRate; // offset 24
	private int byteRate; // offset 28
	private int blockAlign; //offset 32
	private int bitsPerSample; // offset 34
	private String subChunkSize = "data"; //offset 36
	private int subChunk2Size; //offset 40
	
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
		
		buildHeader();

	}

	@Override
	public void process() {
		
		
		try {
			
			/*
			 * La façon donc je procede pour convertir le fichier audio:
			 * Une fois que mon header est build je regarde le nombre de bytes qu'il me reste a lire.
			 *Ensuite je divise ce nombre de bytes par 2 afin d'obtenir le nombre de shorts.
			 *Je trouve un diviseur  du nombre de short afin de pourvoir traité mon fichier par petit array
			 *Je divise le nombre de short par le diviseur trouver afin de savoir combien de fois je traite mes petit arrays
			 */
			
			//nombre de bits restant a lire du fichier audio (je retire les bytes du header que j'ai traité avec ma fonction priver buildHeader)
			int nombreDeBytes = fileInputStream.available()-DATA_OFFSET;
			
			//nombre de shorts que mon fichier audio contient
			int nombreShort = nombreDeBytes/2;

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
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// methode qui build le header d'un fichier wave
	private void buildHeader() {
	
		//chack block aligne
		byte[] byteRate = new byte [2];
		byteRate[0] = 8 ;
		byteRate[1] = 0;
		
		fileSink.push(fileSource.pop(34));
		ByteBuffer buffer = ByteBuffer.allocate(2); 
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(8);
		byte tab[] =buffer.array(); 
		
		for (int i = 0; i < tab.length; i++) {
			System.out.println(i+" "+tab[i ]);
		}
		fileSink.push(tab);
		fileSource.pop(2);
		fileSink.push(fileSource.pop(8));
		
		/*
		byte []tabByte = fileSource.pop(4);
		try {
			chunkID = new String(tabByte,"US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		chunkSize = ByteBuffer.wrap(fileSource.pop(4)).getInt();
		*/
		
//		byte[] byteRate = new byte [1];
//		byteRate[0] = 8;
//		
//		
//		fileSink.push(fileSource.pop(4));
//		fileSink.push(byteRate);
//		fileSink.push(fileSource.pop(29));
//		fileSink.push(byteRate);
//		fileSink.push(fileSource.pop(9));

		
		
		

//		byte[] byteRate = new byte [1];
//		byteRate[0] = 8;	
//		
//		byteArray = fileSource.pop(34);
//		
//		fileSink.push(byteArray);
//		
//		fileSource.pop(1);
//		fileSink.push(byteRate);
//		
//		byteArray = fileSource.pop(9);
//		fileSink.push(byteArray);		

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
		System.out.println("ChunkID: "+chunkID);
		System.out.println("ChunkSize: "+chunkSize);
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
