package it.unipr.ce.dsg.s2p.sip;

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


import java.util.Vector;

import org.zoolu.net.SocketAddress;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.ContactHeader;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.header.RouteHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipMethods;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.MethodId;
import org.zoolu.sip.provider.SipKeepAlive;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

/**
 * The <code>Node</code> is an abstract class to send and receive 
 * Peer Message, held into SIP MESSAGE. <code>Node</code> manage also
 * SIP MESSAGE incoming from Session Border Controller. 
 * <p>
 * The abstract class <code>Node</code> implements the SipProviderListener and 
 * the TransactionClientListener, from MjSIP library, that listens incoming SIP message 
 * request and response. 
 * <code>Node</code> allow to the peer to send, through SipProvider,
 * own messages to other peer and Session Border Controller (SBC). 
 * 
 * @author Fabrizio Caramia
 *
 */


public abstract class Node implements SipProviderListener, TransactionClientListener {

	private boolean requestPublicAddress = false;

	protected NodeConfig nodeConfig;

	protected SipProvider sipProvider;

	protected SipKeepAlive keepAlive;

	/**
	 * When a new message is received from the remote peer
	 * 
	 * @param peerMsg the message of the peer
	 * @param sender the address of the sender peer
	 * @param contentType the content type
	 */
	protected abstract void onReceivedMsg(String peerMsg, Address sender, String contentType);

	/**
	 * When message sent from peer fails
	 * 
	 * @param peerMsgSended the message sent from sender peer
	 * @param receiver the address of the receiver peer
	 * @param contentType the content type
	 */
	protected abstract void onDeliveryMsgFailure(String peerMsgSended, Address receiver, String contentType);

	/**
	 * When message sent from peer successful
	 * 
	 * @param peerMsgSended message sent from sender peer
	 * @param receiver address of the receiver peer
	 * @param contentType the content type
	 */
	protected abstract void onDeliveryMsgSuccess(String peerMsgSended, Address receiver, String contentType);

	/**
	 * When a new message is received from Session Border Controller
	 * 
	 * @param SBCMsg message sent from SBC
	 */
	protected abstract void onReceivedSBCMsg(String SBCMsg);

	/**
	 * When the Contact Address of the peer is received from SBC by its response
	 * 
	 * @param contactAddress
	 */
	protected abstract void onReceivedSBCContactAddress(Address contactAddress);

	/**
	 * Create a new Node
	 * 	
	 * @param file configuration file for node
	 */
	public Node(String file){

		this.sipProvider = new SipProvider(file);
		this.nodeConfig = new NodeConfig(file);

		//listen SIP Request
		startReceiveMessage();
	}

	/**
	 * Create a new Node
	 * 
	 * @param file configuration file for node
	 * @param name peer name
	 * @param port peer port (UDP for default communication)
	 */

	public Node(String file, String name, int port){
		if(file!=null)
			SipStack.init(file);
		else
			//for android compatibility
			SipStack.debug_level=0;


		this.sipProvider = new SipProvider("AUTO-CONFIGURATION", port);

		if(file!=null)
			this.nodeConfig = new NodeConfig(file);
		else
			this.nodeConfig = new NodeConfig();

		this.nodeConfig.peer_name = name;

		//listen SIP Request
		startReceiveMessage();
	}

	/**
	 * Enables to listen incoming message
	 */
	protected void startReceiveMessage(){

		sipProvider.addSelectiveListener(SipProvider.ANY, this);
	}


	/**
	 * Disable the listener for incoming message
	 */
	protected void stopReceiveMessage(){

		sipProvider.removeSelectiveListener(new MethodId(SipMethods.MESSAGE));  
	}


	/**
	 * Send peer message through SIP MESSAGE using <code>toAddress</code> as destination address 
	 * 
	 * @param toAddress destination address
	 * @param fromAddress sender address 
	 * @param msg message message to send
	 * @param contentType type of content (es. application/json or application/xml)
	 */
	protected void sendMessage(Address toAddress, Address fromAddress, String msg, String contentType){

		sendMessage(toAddress, null, fromAddress, msg, contentType);

	}


	/**
	 * Send peer message through SIP MESSAGE using <code>toContactAddress</code> as destination address 
	 * 
	 * @param toAddress local destination address
	 * @param toContactAddress remote destination address
	 * @param fromAddress sender address 
	 * @param msg message message to send
	 * @param contentType contentType type of content (es. application/json or application/xml)
	 */
	protected void sendMessage(Address toAddress,  Address toContactAddress, Address fromAddress, String msg, String contentType){

		NameAddress destAddress = new NameAddress(toAddress);
		NameAddress senderAddress = new NameAddress(fromAddress);

		if(destAddress!=null && senderAddress!=null){

			if(contentType==null || contentType.equals("")) contentType="application/text";

			Message sipMessage = MessageFactory.createMessageRequest(sipProvider, destAddress, senderAddress, null, contentType, msg);

			if(toContactAddress!=null && (!toAddress.equals(toContactAddress)))
				sipMessage.addRouteHeader(new RouteHeader(new NameAddress(toContactAddress+";lr")) );

			TransactionClient tClient = new TransactionClient(sipProvider, sipMessage, this);
			tClient.request();

		}

	}

	/**
	 * Get peer Address (es. alice@pcdomain.it:5070 or alice@192.168.1.100.it:5070)
	 * 
	 * @return Address the address of peer
	 */

	protected Address getAddress(){

		SipURL result = null;
		if(sipProvider!=null)
			result = sipProvider.getContactAddress(nodeConfig.peer_name);

		return new Address(result);
	}

	/**
	 * Send keep alive to <code>targetHost</code>. Useful for refresh the NAT port
	 * @param targetHost
	 */
	public void startKeepAlive(SocketAddress targetHost) {

		if(nodeConfig.keepalive_time>0){
			if (keepAlive!=null && keepAlive.isRunning()) keepAlive.halt();
			keepAlive=new SipKeepAlive(sipProvider, targetHost, null, nodeConfig.keepalive_time);
		}
	}

	/**
	 * Stop keep alive
	 */
	public void stopKeepAlive(){

		if (keepAlive!=null && keepAlive.isRunning()) keepAlive.halt();

	}


	/**
	 * 
	 * Check for the presence of the NAT. Send TEST_NAT message to Session Border Controller
	 */
	public void checkNAT(){
		/*
		 * part of SBC Handshaking 
		 */
		if(nodeConfig.sbc!=null)
			sendMessage(new Address(nodeConfig.sbc), getAddress(), "TEST_NAT", null);		

	}

	/**
	 * Request public address from Session Border Controller. Send REQUEST_PORT message to Session Border Controller
	 */

	public void requestPublicAddress(){
		/*
		 * part of SBC Handshaking 
		 */
		if(nodeConfig.sbc!=null){
			this.requestPublicAddress=true;
			sendMessage(new Address(nodeConfig.sbc), getAddress(), "REQUEST_PORT", null);
		}


	}


	/**
	 * Send REMOVE_PORT message to Session Border Controller to eliminate public address associated to peer
	 */

	public void closePublicAddress(){

		/*
		 * part of SBC Handshaking 
		 */

		if(nodeConfig.sbc!=null){
			this.requestPublicAddress=false;
			sendMessage(new Address(nodeConfig.sbc), getAddress(), "REMOVE_PORT", null);
		}
		stopKeepAlive();

	}

	/**
	 * From SipProviderListener
	 */

	@Override
	public void onReceivedMessage(SipProvider sipPvd, Message sipMsg) {

		TransactionServer tServer=new TransactionServer(sipPvd, sipMsg, null);
		tServer.respondWith(MessageFactory.createResponse(sipMsg, 200, SipResponses.reasonOf(200), null));

		//For SIP Request with method MESSAGE
		if(sipMsg.isMessage())
		{
			String remoteAddress = sipMsg.getRemoteAddress();
			int remotePort = sipMsg.getRemotePort();
			String fullAddress = new String(remoteAddress+":"+remotePort);

			//the Address of SBC is public 
			//therefore the remoteAddress+remotePort is equals to sbc address saved to NodeConfig
			if(nodeConfig.sbc!=null && fullAddress.equals(nodeConfig.sbc)){
				/*
				 * part of SBC Handshaking 
				 */
				onReceivedSBCMsg(sipMsg.getBody());

			}
			else {

				String contentType = sipMsg.getContentTypeHeader().getContentType();
				onReceivedMsg(sipMsg.getBody(), new Address(sipMsg.getFromHeader().getNameAddress().getAddress()),  contentType);
			}
		}


	}

	/**
	 * From TransactionClientListener
	 */
	@Override
	public void onTransFailureResponse(TransactionClient tClient, Message sipMsg) {
		Message reqMessage = tClient.getRequestMessage();
		onDeliveryMsgFailure(reqMessage.getBody(), new Address(reqMessage.getToHeader().getNameAddress().getAddress()),reqMessage.getContentTypeHeader().getContentType());
	}

	/**
	 * From TransactionClientListener
	 */
	@Override
	public void onTransProvisionalResponse(TransactionClient tClient, Message sipMsg) {
		// TODO Auto-generated method stub

	}


	/**
	 * From TransactionClientListener. If <code>sipMsg</code> is a SIP message response 
	 * from Session Border Controller call <code>onReceivedSBCContactAddress()<code>
	 */

	@Override
	public void onTransSuccessResponse(TransactionClient tClient, Message sipMsg) {

		String remoteAddress = sipMsg.getRemoteAddress();
		int remotePort = sipMsg.getRemotePort();
		String fullAddress = new String(remoteAddress+":"+remotePort);

		//for SBC response

		if(nodeConfig.sbc!=null && fullAddress.equals(nodeConfig.sbc) && requestPublicAddress){

			/*
			 * part of SBC Handshaking 
			 */
			if(sipMsg.hasContactHeader() ){
				Vector<?> contacts=sipMsg.getContacts().getHeaders();
				if (!contacts.isEmpty()){

					ContactHeader contactHeader=new ContactHeader((Header)contacts.elementAt(0));
					SipURL contactAddress = contactHeader.getNameAddress().getAddress();     

					onReceivedSBCContactAddress(new Address(contactAddress));

					//refresh NAT port
					startKeepAlive(new SocketAddress(contactAddress.getHost(), contactAddress.getPort()));	

				}
				//else
				//no contact, port not available
			}
		}
		//for Peer response
		else{

			Message reqMessage = tClient.getRequestMessage();
			onDeliveryMsgSuccess(reqMessage.getBody(), new Address(reqMessage.getToHeader().getNameAddress().getAddress()), reqMessage.getContentTypeHeader().getContentType());

		}
	}

	/**
	 * From TransactionClientListener
	 */

	@Override
	public void onTransTimeout(TransactionClient tClient) {
		Message reqMessage = tClient.getRequestMessage();
		onDeliveryMsgFailure(reqMessage.getBody(), new Address(tClient.getRequestMessage().getToHeader().getNameAddress().getAddress()), reqMessage.getContentTypeHeader().getContentType());
	}


}
