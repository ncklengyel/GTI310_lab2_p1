/**
 * Auteur: Nicolas Lengyel, Vuong Viet Vu, Jia Ji Ho
 * 
 * WaveFilter implémente AudioFilter et permet de créer un filtre concret
 * pour manipuler des fichiers audio de format wave.
 */

package audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import io.FileSink;
import io.FileSource;

public class WaveFilter implements AudioFilter {

	// Constantes
	private final int DATA_OFFSET = 44;

	// Variable privées
	private File waveFile; // Le fichier wave Source (16bits pas echantillon)
	private String writePath; // Le chemin de mon fichier converti
	private File writeFile; // fichier en sortie (fichier converti)

	private String chunkID = "RIFF"; // offset 0
	private int chunkSize; // offset 4
	private String format = "WAVE"; // offset 8
	private String subChunk1ID = "fmt "; // offset 12
	private int subChunk1Size = 16; // offset 16
	private short audioFormat = 1; // offset 20
	private short numOfChannels; // offset 22
	private int sampleRate; // offset 24
	private int byteRate; // offset 28
	private short blockAlign; // offset 32
	private short bitsPerSample = 8; // offset 34
	private String subChunk2ID = "data"; // offset 36
	private int subChunk2Size; // offset 40

	private int nombreDeBytesData; // nombre de bytes du data
	private int numSample; // Nombre d'échantillon dans le fichier converti

	/*
	 * Constructeur
	 */
	public WaveFilter(File aWaveFile, String aPath) {

		// Initialisation des variables privées
		waveFile = aWaveFile;
		writePath = aPath;

	}

	/*
	 * Méthode qui permet la convertion du fichier en paramètre 16bits à 8bits
	 * par échantillon
	 */
	public void process() {

		FileSink fileSink;
		FileSource fileSource;

		writeFile = new File(writePath);
		
		// Si mon fichier entré en paramètre est valide et que
		// l'emplacement ou écrire le fichier converti est ok
		if (isValid(waveFile) && isWriteLocationGood(writeFile)) {

			try {

				fileSink = new FileSink(writePath);
				fileSource = new FileSource(waveFile.getAbsolutePath());

				// build le header du fichier à convertir
				buildHeader(fileSource, fileSink);

				/*
				 * La façon donc je procede pour convertir le fichier audio: Une
				 * fois que mon header est build je regarde le nombre de bytes
				 * qu'il me reste a lire. Ensuite je divise ce nombre de bytes
				 * par 2 afin d'obtenir le nombre de shorts. Je trouve un
				 * diviseur du nombre de short afin de pourvoir traité mon
				 * fichier par petit array. Je divise le nombre de short par le
				 * diviseur trouver afin de savoir combien de fois je traite mes
				 * petit arrays
				 */

				// nombre de shorts que mon fichier audio contient
				int nombreShort = fileSource.available() / 2;

				// je trouver un diviseur du nombre de short
				int multiple = trouverDiviseur(nombreShort);

				// nombre de fois que je dois traiter les petit arrays
				int length = nombreShort / multiple;

				// Pour tout mon fichier audio (length)
				for (int i = 0; i < length; i++) {

					// récupération d'un array
					int[] tabData = fileSource.popShort(multiple);

					// Converti en 8 bits mon petit array et le push dans
					// mon fichier en sorti
					fileSink.push(convertToEightBits(tabData));

				}

				// Un fois terminé, je ferme mon fichier en sorti
				fileSink.close();

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

		}

	}

	/*
	 * Méthode permttant de build mon header du fichier à convertir
	 */
	private void buildHeader(FileSource fileSource, FileSink fileSink) {

		byte[] headerSource;
		byte[] headerOut;

		headerSource = fileSource.pop(44);
		headerOut = new byte[44];

		nombreDeBytesData = fileSource.available() / 2;

		sampleRate = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 24, 28)).order(ByteOrder.LITTLE_ENDIAN).getInt();
		numOfChannels = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 22, 24)).order(ByteOrder.LITTLE_ENDIAN)
				.getShort();
		subChunk1Size = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 16, 20)).order(ByteOrder.LITTLE_ENDIAN)
				.getInt();
		audioFormat = ByteBuffer.wrap(Arrays.copyOfRange(headerSource, 20, 22)).order(ByteOrder.LITTLE_ENDIAN)
				.getShort();

		numSample = nombreDeBytesData / 2 / 1;
		subChunk2Size = numSample * numOfChannels * bitsPerSample / 8;
		chunkSize = 36 + subChunk2Size;
		blockAlign = (short) (numOfChannels * bitsPerSample / 8);
		byteRate = sampleRate * numOfChannels * bitsPerSample / 8;

		// ChunkID
		ByteBuffer b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		insertInArray(headerOut, b.put(chunkID.getBytes()).array(), 0, 4);

		// chunksize
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(chunkSize).array(), 4, 8);

		// Format
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		insertInArray(headerOut, b.put(format.getBytes()).array(), 8, 12);

		// subchunk1id
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		insertInArray(headerOut, b.put(subChunk1ID.getBytes()).array(), 12, 16);

		// Subchunk1Size
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(subChunk1Size).array(), 16, 20);

		// AudioFormat
		b = ByteBuffer.allocate(2);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putShort(audioFormat).array(), 20, 22);

		// NumChannels
		b = ByteBuffer.allocate(2);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putShort(numOfChannels).array(), 22, 24);

		// SampleRate
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(sampleRate).array(), 24, 28);

		// ByteRate
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(byteRate).array(), 28, 32);

		// BlockAlign
		b = ByteBuffer.allocate(2);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putShort(blockAlign).array(), 32, 34);

		// BitsPerSample
		b = ByteBuffer.allocate(2);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putShort(bitsPerSample).array(), 34, 36);

		// Subchunk2ID
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.BIG_ENDIAN);
		insertInArray(headerOut, b.put(subChunk2ID.getBytes()).array(), 36, 40);

		// Subchunk2Size
		b = ByteBuffer.allocate(4);
		b.order(ByteOrder.LITTLE_ENDIAN);
		insertInArray(headerOut, b.putInt(subChunk2Size).array(), 40, 44);

		fileSink.push(headerOut);

	}

	//methode qui determine le plus petit diviseur plus grand ou égale à 4 d'un nombre
	private int trouverDiviseur(int aNumber) {

		int i = 4;

		while (aNumber % i != 0) {

			i++;

		}

		return i;

	}

	/*
	 * méthode privée qui copie un tableau dans une autre tableau à l’aide de borne.
	 */
	private void insertInArray(byte[] tabDestination, byte[] tabSource, int borneInf, int bornSup) {

		int count = 0;
		for (int i = borneInf; i < bornSup; i++) {
			tabDestination[i] = tabSource[count];
			count++;
		}

	}

	/*
	 * Méthode qui print le header du fichier en parametre (Utilisation: java -jar programme.jar --print-header fichier)
	 */
	public void printSourceHeader() {

		System.out.println("--HEADER OF " + waveFile.getName() + " ---");

			try {
				FileSource fileSourceTemp = new FileSource(waveFile.getAbsolutePath());

				byte[] headerSource = fileSourceTemp.pop(44);

				byte[] chunkIDarray = Arrays.copyOfRange(headerSource, 0, 4);
				String chunkID = new String(chunkIDarray, StandardCharsets.US_ASCII);
				System.out.println("ChunkID: " + chunkID);

				byte[] chunkSizeArray = Arrays.copyOfRange(headerSource, 4, 8);
				int chunckSize = ByteBuffer.wrap(chunkSizeArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
				System.out.println("ChunckSize: " + chunckSize);

				byte[] formatArray = Arrays.copyOfRange(headerSource, 8, 12);
				String format = new String(formatArray, StandardCharsets.US_ASCII);
				System.out.println("format: " + format);

				byte[] chunkSize1IDarray = Arrays.copyOfRange(headerSource, 12, 16);
				String chunkSize1ID = new String(chunkSize1IDarray, StandardCharsets.US_ASCII);
				System.out.println("chunkSize1ID: " + chunkSize1ID);

				byte[] subChunk1SizeArray = Arrays.copyOfRange(headerSource, 16, 20);
				int subChunk1Size = ByteBuffer.wrap(subChunk1SizeArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
				System.out.println("SubChunk1Size: " + subChunk1Size);

				byte[] audioFormatArray = Arrays.copyOfRange(headerSource, 20, 22);
				short audioFormat = ByteBuffer.wrap(audioFormatArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
				System.out.println("AudioFormat: " + audioFormat);

				byte[] numChannelsArray = Arrays.copyOfRange(headerSource, 22, 24);
				short numChannels = ByteBuffer.wrap(numChannelsArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
				System.out.println("NumChannels: " + numChannels);

				byte[] sampleRateArray = Arrays.copyOfRange(headerSource, 24, 28);
				int sampleRate = ByteBuffer.wrap(sampleRateArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
				System.out.println("SampleRate: " + sampleRate);

				byte[] byteRateArray = Arrays.copyOfRange(headerSource, 28, 32);
				int byteRate = ByteBuffer.wrap(byteRateArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
				System.out.println("ByteRate: " + byteRate);

				byte[] blockAlignArray = Arrays.copyOfRange(headerSource, 32, 34);
				short blockAlign = ByteBuffer.wrap(blockAlignArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
				System.out.println("BlockAlign: " + blockAlign);

				byte[] bitsPerSampleArray = Arrays.copyOfRange(headerSource, 34, 36);
				short bitsPerSample = ByteBuffer.wrap(bitsPerSampleArray).order(ByteOrder.LITTLE_ENDIAN).getShort();
				System.out.println("BitsPerSample: " + bitsPerSample);

				byte[] chunkSize2IDarray = Arrays.copyOfRange(headerSource, 36, 40);
				String chunkSize2ID = new String(chunkSize2IDarray, StandardCharsets.US_ASCII);
				System.out.println("ChunkSize2ID: " + chunkSize2ID);

				byte[] subChunk2SizeArray = Arrays.copyOfRange(headerSource, 40, 44);
				int subChunk2Size = ByteBuffer.wrap(subChunk2SizeArray).order(ByteOrder.LITTLE_ENDIAN).getInt();
				System.out.println("SubChunk2Size: " + subChunk2Size);

				fileSourceTemp.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
			}

	}

	/*
	 * Méthode qui retourne si le fichier passer en paramètre est valid pour la conversion
	 */
	private boolean isValid(File file) {

		boolean valid = true;

		try {

			// Création d'un fileSource
			FileSource fileSourceTemp = new FileSource(file.getAbsolutePath());

			// Je met dans un variable le file size total du fichier
			int fileSize = fileSourceTemp.available();

			// Initialise mon array contenant le header
			byte[] headerSource = fileSourceTemp.pop(44);

			// Met dans un variable le format du fichier
			byte[] formatArray = Arrays.copyOfRange(headerSource, 8, 12);
			String format = new String(formatArray, StandardCharsets.US_ASCII);

			// subChunk1size
			byte[] subChunk1SizeArray = Arrays.copyOfRange(headerSource, 16, 20);
			int subChunk1Size = ByteBuffer.wrap(subChunk1SizeArray).order(ByteOrder.LITTLE_ENDIAN).getInt();

			// Bits per sample
			byte[] bitsPerSampleArray = Arrays.copyOfRange(headerSource, 34, 36);
			short bitsPerSample = ByteBuffer.wrap(bitsPerSampleArray).order(ByteOrder.LITTLE_ENDIAN).getShort();

			// Ferme mon fichier
			fileSourceTemp.close();

			// Si le fichier est plus petit que 44bytes
			if (fileSize < DATA_OFFSET) {

				System.out.println("Le fichier reçu en paramètre semble trop petit (moins de 44 octets)");
				valid = false;

				// Si le fichier n'est pas de format WAVE
			} else if (!format.equals(this.format)) {

				System.out.println("Le fichier reçu en paramètre n'est pas un fichier audio de format wave");

				valid = false;

				// Le fichier n'est pas 16 bits pas échantillon
			} else if (bitsPerSample != 16) {

				System.out.println("Le fichier audio entré en paramètre est " + bitsPerSample
						+ "bits par échantillon\nLe programme converti "
						+ "seulement les fichiers wave 16bits par échantillon");

				valid = false;

				// Si le fichier n'est pas PCM
			} else if (subChunk1Size != 16) {

				System.out.println("Le fichier audio n'est pas encoder en PCM");

				valid = false;

			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return valid;

	}

	/*
	 * Methode qui qui permet de cast les valeurs d'un array de int en byte
	 */
	private byte[] convertToEightBits(int[] tab) {

		byte tabByte[] = new byte[tab.length];

		for (int i = 0; i < tab.length; i++) {

			/*
			 * Source:
			 * https://community.oracle.com/thread/1273228?start=0&tstart=0
			 */
			tabByte[i] = (byte) (tab[i] ^ 0x80); // Convertion int en byte

		}

		return tabByte;

	}

	/*
	 * Méthode qui vérifie si la destination de création du fichier converti
	 */
	private boolean isWriteLocationGood(File writeFile) {

		Scanner keyboard = new Scanner(System.in);
		String ans = "";
		boolean isGood = false;

		// Si le fichier existe déja
		if (writeFile.exists()) {

			System.out.println("Le fichier destination " + writeFile.getAbsolutePath() + " exist");
			System.out.print("Voulez-vous l'écraser ? (y/n) : ");
			ans = keyboard.next();

			// Si la réponse est égale a "y"
			if (ans.equalsIgnoreCase("y")) {

				isGood = true;

			}

		} else {

			isGood = true;

		}

		return isGood;

	}

}
