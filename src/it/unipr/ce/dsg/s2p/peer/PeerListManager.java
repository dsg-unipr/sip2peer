package it.unipr.ce.dsg.s2p.peer;

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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *Class <code>PeerListManager</code> extends <code>Hashtable</code>
 *used to hold the peer descriptor of known peers.
 *<p>
 *For each peer descriptor is associated the key of the peer. 
 * 
 * @author Fabrizio Caramia
 *
 */

public class PeerListManager extends Hashtable<String, NeighborPeerDescriptor> { 

	private static final long serialVersionUID = -6530442422393120638L;


	/**
	 * Create a new PeerListManager
	 * 
	 */
	public PeerListManager() {
		super();

	}

	/**
	 * Create a new PeerListManager
	 * 
	 * @param initialCapacity the initial capacity of the peer list
	 * @param loadFactor the load factor of the peer list
	 */
	public PeerListManager(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);

	}

	/**
	 * Create a new PeerListManager
	 *  
	 * @param initialCapacity the initial capacity of the peer list
	 */
	public PeerListManager(int initialCapacity) {
		super(initialCapacity);

	}

	/**
	 * Create a new PeerListManager
	 * 
	 * @param map the map whose mappings are to be placed in this peer list.
	 */
	public PeerListManager(Map<? extends String, ? extends NeighborPeerDescriptor> map) {
		super(map);

	}


	/**
	 * Get the list with max <code>number</code> descriptor
	 * 
	 * @param number the number max of element returned in the list
	 * @return PeerListManager PeerListManager
	 */
	public PeerListManager getRandomPeers(int number){

		if(number>0){
			//create new list
			PeerListManager newPeerList = new PeerListManager(number);
			Iterator<String> iter = this.keySet().iterator();

			//create array list to contain random key generated 
			ArrayList<Integer> arrayRdmNumber = new ArrayList<Integer>(number);
			//get the size of peer list
			int nKeys =  this.size();
			//initialize random number
			int rdmNumber = 0;

			//create a list of random number
			while(arrayRdmNumber.size()<number){

				rdmNumber = (int) (Math.random()*nKeys);
				//if rdmNumber not exists add 
				if(!arrayRdmNumber.contains(rdmNumber))
					arrayRdmNumber.add(rdmNumber);

			}

			// initialize index for while  
			int i=0;
			while(iter.hasNext()){

				//set current key
				String key = iter.next();
				//if key is equal to random key add it into the list
				if(arrayRdmNumber.contains(i)){					
					newPeerList.put(key, this.get(key));					
				}
				i++;
			}

			return newPeerList;

		}
		else
			return null;


	}

	/**
	 * Read list from the InputStream (es. file)
	 * 
	 * @param istream InputStream
	 * @return boolean return true if the InputStream has been read
	 */

	synchronized public boolean readList(InputStream istream){

		//read the stream
		BufferedReader buffer = new BufferedReader(new InputStreamReader(istream));

		try {

			//create a json object
			//JSONObject jsonObj = new JSONObject(buffer.readLine());
			JsonParser parser = new JsonParser();
			JsonObject jsonObj = (JsonObject) parser.parse(buffer.readLine());
			buffer.close();

			//JSONObject paramsPD;
			JsonObject paramsPD;
			//Iterator<String> peerKeys = jsonObj.keys();
			Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			Hashtable<String, Object> table = gson.fromJson(jsonObj, Hashtable.class);
			Iterator<String> peerKeys = table.keySet().iterator();

			//parse json peer list
			while(peerKeys.hasNext()){

				String peerKey = peerKeys.next();

				//paramsPD = jsonObj.getJSONObject(peerKey);
				paramsPD = jsonObj.getAsJsonObject(peerKey);

				PeerDescriptor peerD = new PeerDescriptor();

				/*peerD.setKey(paramsPD.getString("key"));
				peerD.setAddress(paramsPD.getString("address"));
				peerD.setName(paramsPD.getString("name"));
				peerD.setContactAddress(paramsPD.getString("contactAddress"));*/

				peerD.setKey(paramsPD.get("key").getAsString());
				peerD.setAddress(paramsPD.get("address").getAsString());
				peerD.setName(paramsPD.get("name").getAsString());
				peerD.setContactAddress(paramsPD.get("contactAddress").getAsString());

				this.put(peerKey, new NeighborPeerDescriptor(peerD));

			}

		} catch (IOException e) {
			return false;
		} catch (Exception e) {
			return false;
		}

		return true;
	}


	/**
	 * Write list to OutputStream (es. file). The format is JSON.
	 * 
	 * @param ostream OutputStream
	 * @return boolean return true if the OutputStream has been write
	 */
	synchronized public boolean writeList(OutputStream ostream){

		try {
			//JSONObject peerList = new JSONObject(this);

			// Convert this PeerListManager into a JSON string
			Gson gson = new Gson();
			String peerList = gson.toJson(this);

			//File newFile = new File(filePath+"cachelist.json");
			PrintStream printList = new PrintStream(ostream);
			printList.println(peerList.toString());
			printList.close();

		}
		catch (Exception e) {
			new RuntimeException(e);
			return false;
		}

		return true;
	}


}
