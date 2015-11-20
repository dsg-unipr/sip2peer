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
import it.unipr.ce.dsg.s2p.message.parser.JSONParser;
import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;
import it.unipr.ce.dsg.s2p.util.FileHandler;

import org.zoolu.tools.Log;


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

		if(nodeConfig.log_path!=null){

			//handler for write and read file
			fileHandler = new FileHandler();

			if(!fileHandler.isDirectoryExists(nodeConfig.log_path))
				fileHandler.createDirectory(nodeConfig.log_path);

			log = new Log(nodeConfig.log_path+"info_"+peerDescriptor.getAddress()+".log", Log.LEVEL_MEDIUM);
	
		}
	}

	@Override
	protected void onReceivedJSONMsg(JSONObject peerMsg, Address sender) {

		try {
			/*
			 *log - print info received message 
			 */

			if(nodeConfig.log_path!=null){

				String typeMsg = peerMsg.get("type").toString();
				int lengthMsg = peerMsg.toString().length();

				JSONObject info = new JSONObject();
				info.put("timestamp", System.currentTimeMillis());
				info.put("type", "recv");
				info.put("typeMessage", typeMsg);
				info.put("byte", lengthMsg);
				info.put("sender", sender.getURL());
				printJSONLog(info, log, false);
			}

			//add peer descriptor to list
			if(peerMsg.get("type").equals(JoinMessage.MSG_PEER_JOIN)){

				JSONObject params = peerMsg.getJSONObject("payload").getJSONObject("params");

				PeerDescriptor neighborPD = new PeerDescriptor(params.get("name").toString(), params.get("address").toString(), params.get("key").toString(), params.get("contactAddress").toString());
				NeighborPeerDescriptor neighborPeer = addNeighborPeer(neighborPD);

				//check the numPeerList field
				int numPeer = (Integer) peerMsg.get("numPeerList");

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
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}


	}

	@Override
	protected void onDeliveryMsgFailure(String peerMsg, Address receiver, String contentType) {

		String typeMessage = null;
		JSONObject jsonMsg = null;
		long rtt = 0;
		
		if(contentType.equals(JSONParser.MSG_JSON)){
			try {
				jsonMsg = new JSONObject(peerMsg);
				typeMessage= (String) jsonMsg.get("type");

				long sendedTime = (Long) jsonMsg.get("timestamp");
				long receivedTime = System.currentTimeMillis();
				rtt = receivedTime - sendedTime;

			} catch (JSONException e) {
				e.printStackTrace();
			}

			/*
			 *log - print info sent message 
			 */
			if(nodeConfig.log_path!=null){

				try {
					JSONObject info = new JSONObject();
					info.put("timestamp", System.currentTimeMillis());
					info.put("type", "sent");
					info.put("typeMessage", typeMessage);
					info.put("transaction", "failed");
					info.put("receiver", receiver.getURL());
					info.put("RTT", rtt);
					info.put("byte", peerMsg.length());
					printJSONLog(info, log, false);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		}


	}

	@Override
	protected void onDeliveryMsgSuccess(String peerMsg, Address receiver, String contentType) {


		String typeMessage = null;
		JSONObject jsonMsg = null;
		long rtt = 0;
		
		if(contentType.equals(JSONParser.MSG_JSON)){

			try {
				jsonMsg = new JSONObject(peerMsg);
				typeMessage= (String) jsonMsg.get("type");

				long sendedTime = (Long) jsonMsg.get("timestamp");
				long receivedTime = System.currentTimeMillis();
				rtt = receivedTime - sendedTime;

			} catch (JSONException e) {
				e.printStackTrace();
			}

			/*
			 *log - print info sent message 
			 */
			if(nodeConfig.log_path!=null){

				try {
					JSONObject info = new JSONObject();
					info.put("timestamp", System.currentTimeMillis());
					info.put("type", "sent");
					info.put("typeMessage", typeMessage);
					info.put("transaction", "successful");
					info.put("receiver", receiver.getURL());
					info.put("RTT", rtt);
					info.put("byte", peerMsg.length());
					printJSONLog(info, log, false);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}



	}

	public static void main(String[] args) {

		if(args.length!=0){
			BootstrapPeer peer = new BootstrapPeer(args[0], args[1]);
		}
		else
			System.out.println("run python command: startBootstrapPeer.py");

	}


}
