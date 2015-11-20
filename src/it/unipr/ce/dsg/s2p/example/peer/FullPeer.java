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
import it.unipr.ce.dsg.s2p.example.msg.PongMessage;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;
import it.unipr.ce.dsg.s2p.util.FileHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;

import org.zoolu.tools.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

			// Sostituito per non avere log vuoto
			//log = new Log(nodeConfig.log_path+"info_"+peerDescriptor.getAddress()+".log", Log.LEVEL_MEDIUM);
			log = new Log(nodeConfig.log_path+"info_"+peerDescriptor.getName()+".log", Log.LEVEL_MEDIUM);

		}

	}


	@Override
	protected void onReceivedJSONMsg(JsonObject peerMsg, Address sender) {

		try {

			JsonObject params = peerMsg.getAsJsonObject("payload").getAsJsonObject("params");
			//JSONObject params = peerMsg.getJSONObject("payload").getJSONObject("params");

			/*
			 * log - print info received message 
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
			if(peerMsg.get("type").getAsString().equals(PingMessage.MSG_PEER_PING)){
				
				//XXX PING-PONG
				System.out.println();
				System.out.println("Received PING");

				PeerDescriptor neighborPeerDesc = new PeerDescriptor(params.get("name").getAsString().toString(), params.get("address").getAsString().toString(), params.get("key").getAsString().toString(), params.get("contactAddress").getAsString().toString());
				addNeighborPeer(neighborPeerDesc);
				
				// XXX PING-PONG
				System.out.println("Sending PONG to "+neighborPeerDesc.getContactAddress()+"...");
				Thread.sleep(2000);
				send(new Address(neighborPeerDesc.getContactAddress()), new PongMessage(peerDescriptor));
				//

				/*
				 * peer list - write 
				 */
				if(nodeConfig.list_path!=null){

					if(!fileHandler.isDirectoryExists(nodeConfig.list_path))
						fileHandler.createDirectory(nodeConfig.list_path);

					peerList.writeList(fileHandler.openFileToWrite(nodeConfig.list_path+peerDescriptor.getAddress()+".json"));

				}

			}
			
			//XXX PING-PONG
			if(peerMsg.get("type").getAsString().equals(PongMessage.MSG_PEER_PONG)) {
				System.out.println();
				System.out.println("Received PONG");
				PeerDescriptor neighborPeerDesc = new PeerDescriptor(params.get("name").getAsString().toString(), params.get("address").getAsString().toString(), params.get("key").getAsString().toString(), params.get("contactAddress").getAsString().toString());
				System.out.println("Sending PING to "+neighborPeerDesc.getContactAddress()+"...");
				Thread.sleep(2000);
				send(new Address(neighborPeerDesc.getContactAddress()), new PingMessage(peerDescriptor));
			}
			//
			
			
			if(peerMsg.get("type").getAsString().equals(PeerListMessage.MSG_PEER_LIST)){

				//Iterator<String> iter = params.keys();
				//Iterator<String> iter = params.;

				//TODO controllare
				Gson gson = new Gson();
				@SuppressWarnings("unchecked")
				Hashtable<String, Object> map = gson.fromJson(params, Hashtable.class);
				Iterator<String> iter = map.keySet().iterator();

				while(iter.hasNext()){

					String key = (String) iter.next();

					//JSONObject keyPeer = params.getJSONObject(key);
					JsonObject keyPeer = params.getAsJsonObject(key);
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
					System.out.println("My contact address is: "+peer.peerDescriptor.getContactAddress());
				}
				else if(args[i].equals("-pp")){ 
					peer.joinToBootstrapPeer();
					//XXX PING-PONG
					System.out.println("My contact address is: "+peer.peerDescriptor.getContactAddress());
					System.out.print("Address for PING-PONG: ");
					try {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String addr = br.readLine().trim();
					peer.pingToPeer(addr);
					br.close();
					} catch(IOException e) {}
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
