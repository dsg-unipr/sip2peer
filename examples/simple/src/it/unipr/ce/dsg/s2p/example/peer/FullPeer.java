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
import it.unipr.ce.dsg.s2p.example.msg.PingMessage;
import it.unipr.ce.dsg.s2p.message.parser.JSONParser;
import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;
import it.unipr.ce.dsg.s2p.util.FileHandler;

import java.util.Iterator;

import org.zoolu.tools.Log;

/**
 * Class <code>FullPeer</code> implements many features of a peer.
 * FullPeer manages PEERLIST message and PING message. 
 * 
 * 
 * @author Fabrizio Caramia
 *
 */


public class FullPeer extends Peer {

	protected PeerConfig peerConfig;

	private FileHandler fileHandler;

	private Log log;

	public FullPeer(String pathConfig, String key) {
		super(pathConfig, key);

		init(pathConfig);
	}

	public FullPeer(String pathConfig, String key, String peerName, int peerPort) {
		super(pathConfig, key, peerName, peerPort);

		init(pathConfig);
	}

	private void init(String pathConfig){

		//peer configuration 
		this.peerConfig = new PeerConfig(pathConfig);

		//handler for write and read file
		fileHandler = new FileHandler();

		/*
		 * log - new istance
		 */
		if(nodeConfig.log_path!=null){
			if(!fileHandler.isDirectoryExists(nodeConfig.log_path))
				fileHandler.createDirectory(nodeConfig.log_path);

			log = new Log(nodeConfig.log_path+"info_"+peerDescriptor.getAddress()+".log", Log.LEVEL_MEDIUM); 

		}

	}


	@Override
	protected void onReceivedJSONMsg(JSONObject peerMsg, Address sender) {

		try {

			JSONObject params = peerMsg.getJSONObject("payload").getJSONObject("params");

			/*
			 * log - print info received message 
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
			if(peerMsg.get("type").equals(PingMessage.MSG_PEER_PING)){

				PeerDescriptor neighborPeerDesc = new PeerDescriptor(params.get("name").toString(), params.get("address").toString(), params.get("key").toString(), params.get("contactAddress").toString());
				addNeighborPeer(neighborPeerDesc);

				/*
				 * peer list - write 
				 */
				if(nodeConfig.list_path!=null){

					if(!fileHandler.isDirectoryExists(nodeConfig.list_path))
						fileHandler.createDirectory(nodeConfig.list_path);

					peerList.writeList(fileHandler.openFileToWrite(nodeConfig.list_path+peerDescriptor.getAddress()+".json"));

				}

			}
			if(peerMsg.get("type").equals(PeerListMessage.MSG_PEER_LIST)){

				Iterator<String> iter = params.keys();

				while(iter.hasNext()){

					String key = (String) iter.next();

					JSONObject keyPeer = params.getJSONObject(key);
					PeerDescriptor neighborPeerDesc = new PeerDescriptor(keyPeer.get("name").toString(), keyPeer.get("address").toString(), keyPeer.get("key").toString());

					if(keyPeer.get("contactAddress").toString()!="null")
						neighborPeerDesc.setContactAddress(keyPeer.get("contactAddress").toString());

					addNeighborPeer(neighborPeerDesc);

					/*
					 * peer list - write
					 */
					if(nodeConfig.list_path!=null){

						if(!fileHandler.isDirectoryExists(nodeConfig.list_path))
							fileHandler.createDirectory(nodeConfig.list_path);

						peerList.writeList(fileHandler.openFileToWrite(nodeConfig.list_path+peerDescriptor.getAddress()+".json"));

					}

				}

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



	public void joinToBootstrapPeer(){

		if(peerConfig.bootstrap_peer!=null){

			JoinMessage newJoinMsg = new JoinMessage(peerDescriptor);
			newJoinMsg.setNumPeerList(peerConfig.req_npeer);

			send(new Address(peerConfig.bootstrap_peer), newJoinMsg);

		}

	}

	public void pingToPeer(String address){

		PingMessage newPingMsg = new PingMessage(peerDescriptor);

		//!!!!!!send to local address 
		send(new Address(address), null, newPingMsg);

	}

	public void pingToPeerFromList(){

		PingMessage newPingMsg = new PingMessage(peerDescriptor);

		if(!peerList.isEmpty()){

			Iterator<String> iter = peerList.keySet().iterator();

			//send pingMessage to first peer in the PeerListManager
			String key = iter.next();

			NeighborPeerDescriptor neighborPeer = peerList.get(key);

			send(neighborPeer, newPingMsg);

		}
	}

	public void pingToPeerRandomFromList(){

		PingMessage newPingMsg = new PingMessage(peerDescriptor);
		NeighborPeerDescriptor neighborPeer;

		if(!peerList.isEmpty()){
			//get set size
			int nKeys =  peerList.keySet().size();
			//get a random number
			int indexKey = (int) (Math.random()*nKeys);
			Iterator<String> iter = peerList.keySet().iterator();
			int i=0;
			String key = null;
			//break while when i is equal to random number
			while(iter.hasNext()){
				key = iter.next();

				if(i==indexKey){
					break;
				}
				i++;
			}
			//send ping message to peer	
			if(key!=null){
				neighborPeer = peerList.get(key);
				send(neighborPeer, newPingMsg);
			}


		}
	}

	public void contactSBC(){

		if(nodeConfig.sbc!=null){
	
			requestPublicAddress();
		}
		else
			System.out.println("no sbc address found");
	}


	public void disconnectGWP(){

		closePublicAddress();
	}



	public static void main(String[] args) {

		boolean active = true;

		if(args.length!=0){
			FullPeer peer = null;
			if(args.length==3){
				//args[0]=file peer configuration args[1]=key
				peer = new FullPeer(args[0], args[1]);

			}
			else if(args.length==5){
				//args[0]=file peer configuration args[1]=key args[2]=peer name args[3]=peer port
				peer = new FullPeer(args[0], args[1], args[2], new Integer(args[3]));

			}
			for(int i=0; i<args.length; i++){

				/*
				 * join to bootstrapPeer
				 */
				if(args[i].equals("-j")){ 
					peer.joinToBootstrapPeer();

				}
				/*
				 * request public address from SBC
				 */
				else if(args[i].equals("-s")){
					peer.contactSBC();
				}
				/*
				 * join to bootstrapPeer, wait and send ping message to random peer
				 */
				else if(args[i].equals("-jp")){

					peer.joinToBootstrapPeer();
					//wait for 3 seconds
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					peer.pingToPeerRandomFromList();
				}
				/*
				 * join to bootstrapPeer, wait and send ping message to random peer recursively
				 */
				else if(args[i].equals("-jr")){

					peer.joinToBootstrapPeer();

					while(active){

						//wait for 15 seconds
						try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//ping to random peer
						peer.pingToPeerRandomFromList();
					}
				}

				else if(args[i].equals("-p")){

					peer.pingToPeer(args[5]);
				}

				else if(args[i].equals("-sd")){

					peer.contactSBC();
					try {
						Thread.sleep(7000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					peer.disconnectGWP();

				}
				/*
				 * contact SBC, wait, join to bootstrapPeer, wait and send ping message to random peer recursively
				 */
				else if(args[i].equals("-a")){

					peer.contactSBC();

					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					peer.joinToBootstrapPeer();

					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					peer.pingToPeerRandomFromList();
				}


			}

		}
	}


}
