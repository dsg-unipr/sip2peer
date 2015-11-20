package it.unipr.ce.dsg.s2p.util;

/*
 * Copyright (C) 2010 University of Parma - Italy
 * 
 * This source code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Designer(s):
 * Marco Picone (picone@ce.unipr.it)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * Michele Amoretti (michele.amoretti@unipr.it)
 * 
 * Developer(s)
 * Fabrizio Caramia (fabrizio.caramia@studenti.unipr.it)
 * 
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Class <code>FileHandler</code> is a file manager.
 * 
 * @author Fabrizio Caramia
 *
 */

public class FileHandler {


	/**
	 * Check if a file exists
	 * 
	 * @param fileName
	 * @return true if the file exists, false otherwise
	 */
	public boolean isFileExists(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

    /**
     * Open a file to be read
     * 
     * 
     * @param fileName the file to be opened
     * @return a {@code FileInputStream} associated with this file
     */
	public FileInputStream openFileToRead(String fileName){


		// File loadFile = new File(fileName);
		FileInputStream fis = null;
		try {
			
			fis = new FileInputStream(new File(fileName));

		} catch (FileNotFoundException e) {
			return null;
		}

		return fis;
	}

    /**
     * Open a file to be write
     * 
     * @param fileName the file to be written
     * @return a {@code FileOutputStream} associated with this file
     */
	public FileOutputStream openFileToWrite(String fileName){

		FileOutputStream fos = null;

		try {
			
			fos = new FileOutputStream(new File(fileName));
			
		} catch (FileNotFoundException e) {
			return null;
		}

		return fos;

	}
	
	/**
	 * Create a directory
	 * 
	 * @param pathName the directory's path
	 * @return true if the directory was created, false otherwise
	 */
	public boolean createDirectory(String pathName){
		
		
		File newDirectory = new File(pathName);
		
		return newDirectory.mkdir();
		
	}
	
	/**
	 * Check if a directory exists
	 * 
	 * @param pathName the directory's path
	 * @return true if the directory exists, false otherwise
	 */
	public boolean isDirectoryExists(String pathName){
		
		
		File directory = new File(pathName);
		
		return directory.isDirectory();
		
	}






}
