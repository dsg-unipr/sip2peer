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


import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.sip.Address;
import it.unipr.ce.dsg.s2p.sip.Node;
import it.unipr.ce.dsg.s2p.util.ResolutionHost;

import java.util.Iterator;

import org.zoolu.tools.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The <code>Peer</code> is an abstract class that extends <code>Node</code> class.
 * <p>
 * Class Peer permits to send and receive message, add and remove peer descriptor into the list, 
 * print log.
 * 
 * @author Fabrizio Caramia
 *
 */

public abstract class Peer extends Node {

	protected PeerDescriptor peerDescriptor;

	protected PeerListManager peerList;

	//protected BasicParser parserMsg;

	/**
	 * Create a new Peer
	 * 
	 * @param pathConfig configuration file for peer
	 * @param key identifies the peer
	 */

	public Peer(String pathConfig, String key) {

		super(pathConfig);

		init(key);

	}

	/**
	 * Create a new Peer
	 * 
	 * @param pathConfig configuration file for peer
	 * @param key identifies the peer
	 * @param peerName name of the peer
	 * @param peerPort port (UDP for default) of the peer
	 */
	public Peer(String pathConfig, String key, String peerName, int peerPort){

		super(pathConfig, peerName, peerPort);
		init(key);
	}

	/**
	 * Create a new Peer
	 * 
	 * @param pathConfig configuration file for peer
	 * @param key identifies the peer
	 * @param peerName name of the peer
	 * @param peerPort port (UDP for default) of the peer
	 * @param parser to marshal peer message
	 */
	/*public Peer(String pathConfig, String key, String peerName, int peerPort, BasicParser parser) {

		super(pathConfig, peerName, peerPort);

		init(key, parser);

	}*/


	/**
	 * Create a new Peer
	 * 
	 * @param pathConfig configuration file for peer
	 * @param key identifies the peer
	 * @param parser to marshal peer message
	 */
	/*public Peer(String pathConfig, String key, BasicParser parser) {

		super(pathConfig);

		init(key, parser);

	}*/

	/**
	 * Initialize peer
	 * 
	 * @param key identifies the peer
	 */
	private void init(String key){

		this.peerDescriptor = new PeerDescriptor(nodeConfig.peer_name, getAddress().getURL(), key);

		this.peerList = new PeerListManager();

		// Default parser is JSONParser
		/*if(parser!=null)
			this.parserMsg = parser;
		else
			this.parserMsg = new JSONParser(nodeConfig.content_msg);*/

	}


	/**
	 * To send peer message through <code>toAddress</code>
	 * 
	 * @param toAddress destination address
	 * @param message the peer message
	 */
	public void send(Address toAddress, BasicMessage message){

		send(toAddress, null, message); 

	}


	/**
	 * To send peer message through <code>toContactAddress</code>
	 * 
	 * @param toAddress destination address (local)
	 * @param toContactAddress contact address (address)
	 * @param message the peer message
	 */

	public void send(Address toAddress, Address toContactAddress, BasicMessage message){
		
		if(nodeConfig.content_msg.equals("text")){
			sendMessage(toAddress, toContactAddress, new Address(peerDescriptor.getAddress()), message.toString(), message.getType());
		}
		else{
			// Convert the message into JSON format
			Gson gson = new Gson();
			String jsonString = gson.toJson(message);

			sendMessage(toAddress, toContactAddress, new Address(peerDescriptor.getAddress()), jsonString, "application/json");
		}

	}

	/**
	 * To send the peer message using NeighborPeerDescriptor of the recipient peer. If Contact Address, of the PeerDescriptor class,
	 * contains the public address the message is sent through it.
	 * 
	 * @param toNeighborPeer NeighborPeerDescriptor of the peer
	 * @param message the peer message
	 */

	//the one method that check if peer is reached locally or through gatewayPeer contact address
	public void send(NeighborPeerDescriptor toNeighborPeer, BasicMessage message){

		if(toNeighborPeer.localReachability()=="yes")
			send(new Address(toNeighborPeer.getAddress()), new Address(toNeighborPeer.getAddress()), message);
		else
			send(new Address(toNeighborPeer.getAddress()), new Address(toNeighborPeer.getContactAddress()), message);

	}

	/**
	 * 
	 * Stop peer communication
	 */

	public void halt(){

		sipProvider.halt();
	}


	/**
	 * Add peer descriptor of the near peer to list 
	 * 
	 * @param neighborPeer NeighborPeerDescriptor
	 * @return NeighborPeerDescriptor the update NeighborPeerDescriptor
	 */
	public NeighborPeerDescriptor addNeighborPeer(PeerDescriptor neighborPeer){

		NeighborPeerDescriptor neighborPD = null;

		try{

			neighborPD = new NeighborPeerDescriptor(neighborPeer);

			//Test if a neighbor peer is reached by "address" value of the PeerDescriptor

			if(nodeConfig.test_address_reachability.equals("yes")){
				//check if the local address of peer is reached
				ResolutionHost resHost = new ResolutionHost(new Address(neighborPeer.getAddress()));
				//timeout in milliseconds
				if(resHost.isReachable(3000)){
					neighborPD.setLocalReachability("yes");

				}
				else
					neighborPD.setLocalReachability("no");
			}


			peerList.put(neighborPD.getKey(), neighborPD);

		}
		catch (NullPointerException e) {
			return neighborPD;
		}
		return neighborPD;

	}

	/**
	 * Remove peer descriptor of the near peer from list 
	 * 
	 * @param key the key that identifies the peer
	 * @return boolean if true the removal was successful
	 */
	public boolean removeNeighborPeer(String key){

		try{
			peerList.remove(key);
		}
		catch (NullPointerException e) {
			return false;
		}
		return true;

	}

	/**
	 * 
	 * Remove peer descriptor of the near peer from list 
	 * 
	 * @param peerAddress the address that identifies the peer
	 * @return boolean if true the removal was successful
	 */

	public boolean removeNeighborPeer(Address peerAddress){

		if(peerAddress!=null){

			Iterator<String> iter = peerList.keySet().iterator();

			while(iter.hasNext()){

				String key = iter.next();

				NeighborPeerDescriptor peerDesc = peerList.get(key);

				if(peerDesc.getAddress().compareTo(peerAddress.getURL())==0){

					peerList.remove(key);
				}

			}

			return true;
		}
		else
			return false;

	}


	@Override
	protected void onReceivedMsg(String peerMsg, Address sender, String contentType) {

		if(contentType.equals("application/json")){
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonMsg = (JsonObject)jsonParser.parse(peerMsg);

			//JSONObject jsonMsg = (JSONObject) parserMsg.unmarshal(peerMsg);
			onReceivedJSONMsg(jsonMsg, sender);
		}
	}

	protected void onReceivedJSONMsg(JsonObject jsonMsg, Address sender){

	}


	@Override
	protected void onReceivedSBCMsg(String SBCMsg){
		/*
		 * part of SBC Handshaking 
		 */

		if(SBCMsg.equals("NAT_ON"))
			requestPublicAddress();
		//else NAT OFF 
		//the contact address is the local address
	}

	@Override
	protected void onReceivedSBCContactAddress(Address cAddress) {

		/*
		 * part of SBC Handshaking 
		 */
		this.peerDescriptor.setContactAddress(cAddress.getURL());

		/*
		 * send message with HELLO string in the SIP Message body,
		 * this is important because opens the NAT port 
		 */
		sendMessage(cAddress, new Address(peerDescriptor.getAddress()), "HELLO", null);

	}

	/**
	 * Save log 
	 * 
	 * @param info information that must be saved
	 * @param log Log object
	 */
	public void printLog(String info, Log log){  

		log.println(info);

	}

	/**
	 * Save log in the JSON format
	 * 
	 * @param jsonInfo information that must be saved in the JSON format
	 * @param log Log object
	 * @param timestamp enable/disable timestamp print
	 */

	public void printJSONLog(JsonObject jsonInfo, Log log, boolean timestamp){
		log.setTimestamp(timestamp);
		log.println(jsonInfo.toString());
	}

}
