package it.unipr.ce.dsg.s2p.example.peer;

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


import it.unipr.ce.dsg.s2p.example.msg.JoinMessage;
import it.unipr.ce.dsg.s2p.example.msg.PeerListMessage;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;
import it.unipr.ce.dsg.s2p.util.FileHandler;

import org.zoolu.tools.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Class <code>BootstrapPeer</code> implements a simple Bootstrap Peer.
 * BootstrapPeer manages JOIN peer message.
 * 
 * 
 * @author Fabrizio Caramia
 *
 */

public class BootstrapPeer extends Peer {

	private Log log;
	private FileHandler fileHandler;

	public BootstrapPeer(String pathConfig, String key) {
		super(pathConfig, key);
		init();
	}

	private void init(){

		/*try {
		FileWriter fw = new FileWriter("prova.txt");
		fw.write("*** Address: "+peerDescriptor.getAddress());
		fw.write("\nContact address: "+peerDescriptor.getContactAddress()+"***");
		fw.flush();
		fw.close();

		} catch(IOException e) {
			e.printStackTrace();
		}*/

		if(nodeConfig.log_path!=null){

			//handler for write and read file
			fileHandler = new FileHandler();

			if(!fileHandler.isDirectoryExists(nodeConfig.log_path))
				fileHandler.createDirectory(nodeConfig.log_path);

			log = new Log(nodeConfig.log_path+"info_"+peerDescriptor.getAddress()+".log", Log.LEVEL_MEDIUM);	
		}
	}

	@Override
	protected void onReceivedJSONMsg(JsonObject peerMsg, Address sender) {

		try {
			/*
			 *log - print info received message 
			 */

			if(nodeConfig.log_path!=null){

				String typeMsg = peerMsg.get("type").getAsString().toString();
				
				int lengthMsg = peerMsg.toString().length();

				JsonObject info = new JsonObject();
				info.addProperty("timestamp", System.currentTimeMillis());
				info.addProperty("type", "recv");
				info.addProperty("typeMessage", typeMsg);
				info.addProperty("byte", lengthMsg);
				info.addProperty("sender", sender.getURL());
				printJSONLog(info, log, false);
			}

			//add peer descriptor to list
			if(peerMsg.get("type").getAsString().toString().equals(JoinMessage.MSG_PEER_JOIN)){
				
				//DEBUG
				System.out.println("Received peer_join message.");

				JsonObject params = peerMsg.getAsJsonObject("payload").getAsJsonObject("params");
				
				PeerDescriptor neighborPD = new PeerDescriptor(params.get("name").toString(), params.get("address").toString(), params.get("key").toString(), params.get("contactAddress").toString());
				
				NeighborPeerDescriptor neighborPeer = addNeighborPeer(neighborPD);

				//check the numPeerList field
				int numPeer = Integer.parseInt(peerMsg.get("numPeerList").toString());

				if(numPeer>=0){

					PeerListMessage newPLMsg = null;

					if(numPeer==0 || (this.peerList.size()<=numPeer)){

						//create message and add the peer list 
						newPLMsg = new PeerListMessage(this.peerList);	
					}
					else{
						newPLMsg = new PeerListMessage(this.peerList.getRandomPeers(numPeer+1));	
					}

					//remove the current peer from payload
					if(newPLMsg.getPayload().containsKey(params.get("key").toString()))
						newPLMsg.getPayload().removeParam(params.get("key").toString());



					//send peer list to peer
					if(newPLMsg!=null)
						//send(new Address(neighborPeer.getAddress()), newPLMsg);
						send(neighborPeer, newPLMsg);

					if(nodeConfig.list_path!=null){

						if(!fileHandler.isDirectoryExists(nodeConfig.list_path))
							fileHandler.createDirectory(nodeConfig.list_path);

						peerList.writeList(fileHandler.openFileToWrite(nodeConfig.list_path+peerDescriptor.getAddress()+".json"));

					}

				}

				//else no list to send

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}


	}

	@Override
	protected void onDeliveryMsgFailure(String peerMsg, Address receiver, String contentType) {

		String typeMessage = null;
		JsonObject jsonMsg = null;
		long rtt = 0;

		if(contentType.equals("application/json")){
			try {
				JsonParser jsonParser = new JsonParser();
				jsonMsg = (JsonObject)jsonParser.parse(peerMsg);
				//jsonMsg = new JSONObject(peerMsg);
				typeMessage= (String) jsonMsg.get("type").getAsString().toString();

				long sentTime = Long.parseLong(jsonMsg.get("timestamp").toString());
				long receivedTime = System.currentTimeMillis();
				rtt = receivedTime - sentTime;

			} catch (Exception e) {
				e.printStackTrace();
			}

			/*
			 *log - print info sent message 
			 */
			if(nodeConfig.log_path!=null){

				try {
					JsonObject info = new JsonObject();
					info.addProperty("timestamp", System.currentTimeMillis());
					info.addProperty("type", "sent");
					info.addProperty("typeMessage", typeMessage);
					info.addProperty("transaction", "failed");
					info.addProperty("receiver", receiver.getURL());
					info.addProperty("RTT", rtt);
					info.addProperty("byte", peerMsg.length());
					printJSONLog(info, log, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}


	}

	@Override
	protected void onDeliveryMsgSuccess(String peerMsg, Address receiver, String contentType) {

		String typeMessage = null;
		JsonObject jsonMsg = null;
		long rtt = 0;

		if(contentType.equals("application/json")){

			try {
				JsonParser jsonParser = new JsonParser();
				jsonMsg = (JsonObject)jsonParser.parse(peerMsg);
				//jsonMsg = new JSONObject(peerMsg);
				typeMessage= (String) jsonMsg.get("type").getAsString().toString();

				long sentTime = Long.parseLong(jsonMsg.get("timestamp").toString());
				long receivedTime = System.currentTimeMillis();
				rtt = receivedTime - sentTime;

			} catch (Exception e) {
				e.printStackTrace();
			}

			/*
			 *log - print info sent message 
			 */
			if(nodeConfig.log_path!=null){

				try {
					JsonObject info = new JsonObject();
					info.addProperty("timestamp", System.currentTimeMillis());
					info.addProperty("type", "sent");
					info.addProperty("typeMessage", typeMessage);
					info.addProperty("transaction", "successful");
					info.addProperty("receiver", receiver.getURL());
					info.addProperty("RTT", rtt);
					info.addProperty("byte", peerMsg.length());
					printJSONLog(info, log, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}



	}

	public static void main(String[] args) {

		if(args.length!=0){
			BootstrapPeer peer = new BootstrapPeer(args[0], args[1]);
			System.out.println("My contact address: "+peer.peerDescriptor.getContactAddress());
		}
		else
			System.out.println("run python command: startBootstrapPeer.py");

	}


}
