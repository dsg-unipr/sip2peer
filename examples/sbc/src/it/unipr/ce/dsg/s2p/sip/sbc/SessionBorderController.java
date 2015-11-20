package it.unipr.ce.dsg.s2p.sip.sbc;

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


import java.util.HashMap;
import java.util.Iterator;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

import local.server.Proxy;
import local.server.ServerProfile;

/**
 * Class <code>SessionBorderController</code> implements a Session Border Controller SIP Server.
 * <br>
 * The SBC check if a peer is behind NAT and assigns the public address to local peer.
 * 
 * @author Fabrizio Caramia
 *
 */
public class SessionBorderController extends Proxy implements TransactionClientListener, TestNATListener{

	/** A list of GatewayPeerw with (int) port as key*/
	private HashMap<Integer, GatewayPeer> listGatewayPeer;

	/** NameAddress of Session Border Controller */
	private NameAddress SBCAddress;

	private TestNATPeer testNATPeer;

	private SessionBorderControllerConfig sbcConfig;

	/**
	 * Create a new SBC
	 * 
	 * @param sipProvider the SipProvider
	 * @param serverProfile the ServerProfile
	 * @param file the file configuration
	 */
	SessionBorderController(SipProvider sipProvider, ServerProfile serverProfile, String file){

		super(sipProvider, serverProfile); 

		init(file);
	}

	// initialize the SBC
	private void init(String file){

		this.sbcConfig = new SessionBorderControllerConfig(file);

		this.SBCAddress = new NameAddress(sip_provider.getContactAddress("SBC"));
		// new TestNATPeer
		if(sbcConfig.testNATPort!=0)
			this.testNATPeer = new TestNATPeer("testnat", sbcConfig.testNATPort, this);
		// new map for GatewayPeer
		this.listGatewayPeer = new HashMap<Integer, GatewayPeer>(sbcConfig.nMaxGatewayPeer);


	}

	/**
	 * From Proxy class
	 * 
	 */
	
	@Override
	public void processRequestToLocalServer(Message msg){

		//if Request message is REGISTER
		if(msg.isRegister())
			super.processRequestToLocalServer(msg);
		else
		{	//other message 
			String messageBody = msg.getBody();

			//if the peer has sended MESSAGE with TEST_NAT
			if(messageBody.equals("TEST_NAT")){

				//send response message
				TransactionServer transServer=new TransactionServer(sip_provider, msg, null);
				transServer.respondWith(MessageFactory.createResponse(msg, 200, SipResponses.reasonOf(200), null));

				String namePeer=null;

				NameAddress nameAddPeer = msg.getFromHeader().getNameAddress();

				if(nameAddPeer.hasDisplayName())		
					namePeer=nameAddPeer.getDisplayName();
				else
					namePeer=nameAddPeer.getAddress().getUserName();

				//Hold the local NameAddress of peer
				NameAddress localAddress = testNATPeer.makeNameAddress(namePeer, msg.getViaHeader().getHost(), msg.getViaHeader().getPort());
				//Hold the remote NameAddress of peer, that is the address released by the NAT/Router
				NameAddress remoteAddress = testNATPeer.makeNameAddress(namePeer, msg.getRemoteAddress(), msg.getRemotePort());

				//send MESSAGE Request with TEST_NAT_FOR_PEER
				//NOTE: localAddress is hold on ToHeader, remoteAddress is hold on Request line
				testNATPeer.sendMessageRequest(localAddress, remoteAddress, "TEST_NAT_FOR_PEER");


			}
			else if(messageBody.equals("REMOVE_PORT")){


				//send response message
				TransactionServer transServer=new TransactionServer(sip_provider, msg, null);
				transServer.respondWith(MessageFactory.createResponse(msg, 200, SipResponses.reasonOf(200), null));

				//get Iterator with listePort
				Iterator<Integer> iter =  listGatewayPeer.keySet().iterator();
				int portKey=0;
				
				//get address of the local peer
				SipURL senderLocalAdd = new SipURL(msg.getViaHeader().getSipURL().toString());
				
				while(iter.hasNext()){

					portKey = iter.next();
					//get address of the local peer stored in the list with current portKey
					SipURL localPeerAdd = listGatewayPeer.get(portKey).getLocalPeerAddress().getAddress();
					//check if the sender peer has a GatewayPeer with the same local host and port
					if(senderLocalAdd.getHost().equals(localPeerAdd.getHost()))
						if(senderLocalAdd.getPort()==localPeerAdd.getPort()){

							GatewayPeer gwPeer = listGatewayPeer.remove(portKey);
							gwPeer.halt();
							
						}
				}

			}
			//if the peer has sended MESSAGE with REQUEST_PORT
			else if(messageBody.equals("REQUEST_PORT")){

				String namePeer=null;
				int listenPort=0;

				NameAddress nameAddPeer = msg.getFromHeader().getNameAddress();

				if(nameAddPeer.hasDisplayName())		
					namePeer=nameAddPeer.getDisplayName();
				else
					namePeer=nameAddPeer.getAddress().getUserName();


				listenPort = getFreePort();
				if(listenPort!=-1){
					//port available

					GatewayPeer gatewayPeer = new GatewayPeer(namePeer, listenPort);
					//add GatewayPeer instance to list 

					this.listGatewayPeer.put(listenPort, gatewayPeer);

					//send response message + contact <sip:namePeer@gatewayPeerAddress>
					TransactionServer transServer=new TransactionServer(sip_provider, msg, null);
					transServer.respondWith(MessageFactory.createResponse(msg, 200, SipResponses.reasonOf(200), gatewayPeer.getAddress()));

				}
				else{
					//port not available

					//send response message without contact
					TransactionServer transServer=new TransactionServer(sip_provider, msg, null);
					transServer.respondWith(MessageFactory.createResponse(msg, 200, SipResponses.reasonOf(200), null));
				}


			}
			//do nothing	
			else{

				TransactionServer transServer=new TransactionServer(sip_provider, msg, null);
				transServer.respondWith(MessageFactory.createResponse(msg, 200, SipResponses.reasonOf(200), null));

			}


		}


	}

	/**
	 * Sends SIP MESSAGE where "to" is the destination/recipient and 
	 * "msgBody" is the message that will be sended 
	 * 
	 * @param to destination address
	 * @param msgBody message hold into SIP Body
	 */
	public void sendMessageRequest(NameAddress to, String msgBody)
	{  

		Message message=MessageFactory.createMessageRequest(sip_provider, to, SBCAddress , null, "application/text", msgBody);

		TransactionClient tClient=new TransactionClient(sip_provider, message, this);
		tClient.request();

	}

	/**
	 * From TransactionClientListener
	 */
	@Override
	public void onTransFailureResponse(TransactionClient arg0, Message msg) {
		// TODO Auto-generated method stub
	}
	/**
	 * From TransactionClientListener
	 */
	@Override
	public void onTransProvisionalResponse(TransactionClient arg0, Message msg) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * From TransactionClientListener
	 */
	@Override
	public void onTransSuccessResponse(TransactionClient arg0, Message msg) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * From TransactionClientListener
	 */
	@Override
	public void onTransTimeout(TransactionClient arg0) {
		// TODO Auto-generated method stub

	}


    /**
     * From TestNATListener
     * 
     */
	@Override
	public void onFailureTest(String type, SipURL localAddress, SipURL remoteAddress) {

		
		//peer behind NAT -- symmetric cone or restricted
		/*if(!localAddress.equals(remoteAddress))
			System.out.println("host and peer are differents");*/

		sendMessageRequest(new NameAddress(remoteAddress), "NAT_ON");
	}

    /**
     * 
     * From TestNATListener
     * 
     */
	@Override
	public void onSuccessTest(String type, SipURL localAddress, SipURL remoteAddress) {
		/*
		 * part of SBC Handshaking 
		 */
		if(!localAddress.equals(remoteAddress)){
			//host and peer are different
			//peer behind NAT -- full cone
			sendMessageRequest(new NameAddress(remoteAddress), "NAT_ON");
		}
		else{
			//the peer has a public ip in its network
			sendMessageRequest(new NameAddress(remoteAddress), "NAT_OFF");
		}

	}

	/**
	 * Get a free port for GatawayPeer
	 * 
	 * @return	the port available for GatawayPeer
	 */
	private int getFreePort(){
		//verificare le porte libere facendo leva sul listGatewayPeer

		int freePort=-1;
		if(listGatewayPeer.size()<sbcConfig.nMaxGatewayPeer){

			int initPort=0;
			if(sbcConfig.initPort!=0)
				initPort = sbcConfig.initPort;
			int maxPort = initPort+sbcConfig.nMaxGatewayPeer;

			for(int i=initPort; i<maxPort+1; i++){

				if(!listGatewayPeer.containsKey(i)){
					freePort=i;
					break;	
				}
			}
			return freePort;
		}

		else
			return freePort;
	}




	/* ************* MAIN ********************* */

	public static void main(String[] args) {

		if(args.length!=0){

			String pathFile = "config/"+args[0];

			SipProvider sipProvider = new SipProvider(pathFile);

			ServerProfile serverP = new ServerProfile(null);

			@SuppressWarnings("unused")
			SessionBorderController sbc = new SessionBorderController(sipProvider, serverP, pathFile);

		}

	}


}
