package io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * A FileSource object opens a handle on a file and sends the data in
 * sequential order to a caller. When the data reaches the end-of-file marker,
 * it will return nothing.
 * 
 * @author Fran�ois Caron <francois.caron.7@ens.etsmtl.ca>
 */
public class FileSource {
	
	/* The file's handler */
	private DataInputStream _reader;

	/**
	 * Create a new FileSource. The instanciation will be cancelled if the
	 * specified path is not valid.
	 * 
	 * @param location The complete path to the file
	 * @throws FileNotFoundException If the path does not lead to a real file.
	 */
	public FileSource(String location) throws FileNotFoundException {
		try {
			/* open the handler on the specified file */
			_reader = new DataInputStream(
						new BufferedInputStream(
							new FileInputStream(location)));
		} catch (FileNotFoundException e) {
			/* the path is not valid */
			throw e;
		}
	}
	
	/**
	 * Unreference the file and close it cleanly.
	 */
	public void close() {
		try {
			/* close the handler */
			_reader.close();
		} catch (IOException e) {
			/* something went wrong */
		}
	}
	
	/**
	 * Query the handler for some bytes from the file. If the size is larger
	 * than the amount left to read in the file, it will return the number of
	 * bytes left in the file.
	 *  
	 * @param size The number of bytes to read.
	 * @return An array of bytes read in the file.
	 */
	public byte[] pop(int size) {
		try {
			/* create a new byte array for the number of bytes asked */
			byte[] buffer = new byte[size];
			
			/* read the number of bytes asked for, or the amount left in the
			 * file */
			_reader.read(buffer);
			
			/* return what was read */
			return buffer;
		} catch (IOException e) {
			/* something went wrong, or EOF reached */
			return null;
		}
	}
	
	public int[] popShort(int size){
		
		try {
			/* create a new byte array for the number of bytes asked */
			int[] buffer = new int[size];
			
			/* read the number of bytes asked for, or the amount left in the
			 * file */
			
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = _reader.readShort();
			}
			
			/* return what was read */
			return buffer;
		} catch (IOException e) {
			/* something went wrong, or EOF reached */
			return null;
		}	
		
	}
	
	public float popFloat(){
		
		float a = 0;
		try {
			 a = _reader.readFloat();
			return a;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;

	}
	
	public int available(){
		
		int nbBytes = 0;
		
		try {
			
			nbBytes = _reader.available();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nbBytes;
		
	}
	
}
