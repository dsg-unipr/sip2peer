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


import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.MaxForwardsHeader;
import org.zoolu.sip.header.RecordRouteHeader;
import org.zoolu.sip.header.RequestLine;
import org.zoolu.sip.header.ToHeader;
import org.zoolu.sip.header.ViaHeader;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

/**
 * Class <code>GatewayPeer</code> implements a Gateway Peer to forwards
 * messages to the local peer. For each local peer is associated a Gateway Peer
 * with a public address.
 * 
 * @author Fabrizio Caramia
 *
 */
public class GatewayPeer implements SipProviderListener, TransactionClientListener{

	private SipProvider sipGatewayPeer;
	//remote peer address (public address assigned by router/NAT)  
	private NameAddress remotePeerAddress;
	//gateway peer address 
	private NameAddress address;
	//local peer address (private address)
	private NameAddress localPeerAddress;
	//name of peer useful for the DisplayName of NameAddress  
	private String namePeer;
	

	/**
	 * Create a new GatewayPeer
	 * 
	 * @param namePeer the name of the peer associated to GatewayPeer
	 * @param listenPort the port (UDP) assigned, by SBC, to GatewayPeer
	 */
	public GatewayPeer(String namePeer, int listenPort){

		this.sipGatewayPeer = new SipProvider("AUTO-CONFIGURATION", listenPort);
		this.sipGatewayPeer.addSelectiveListener(SipProvider.ANY, this);
		
		this.setNamePeer(namePeer);
		this.setAddress(new NameAddress(namePeer, sipGatewayPeer.getContactAddress(namePeer)));

	}
	
	/**
	 * Stop GatewayPeer communication
	 * 
	 */
	public void halt(){
		
		sipGatewayPeer.halt();
	}
	
	/**
	 * Forward each message received to the local peer
	 * 
	 */
	@Override
	public void onReceivedMessage(SipProvider sipPeer, Message msgPeer) {

		
		if(msgPeer.getBody().equals("HELLO")){
		//if gatewayPeer receive SIP MESSAGE with HELLO mean that it is the first message
			
			//set local peer address to forward message
			setLocalPeerAddress(msgPeer.getViaHeader().getSipURL());
			//set remote peer address to forward message by NAT
			setRemotePeerAddress(msgPeer.getRemoteAddress(), msgPeer.getRemotePort());
			
			TransactionServer transactionServer = new TransactionServer(sipPeer, msgPeer, null);
			transactionServer.respondWith(MessageFactory.createResponse( msgPeer, 200, SipResponses.reasonOf(200), null));

		}
		
		else{
			
			//response with 200 OK to remote peer  
			TransactionServer transactionServer = new TransactionServer(sipPeer, msgPeer, null);
			transactionServer.respondWith(MessageFactory.createResponse( msgPeer, 200, SipResponses.reasonOf(200), null));
			
			
			//remove route header and add record-route header
			if(msgPeer.hasRouteHeader()){
				NameAddress nameAdd = msgPeer.getRouteHeader().getNameAddress();
				msgPeer.removeRouteHeader();
				msgPeer.addRecordRouteHeader( new RecordRouteHeader(nameAdd));
			}
			
			//update Header Via
			ViaHeader via=new ViaHeader(sipPeer.getDefaultTransport(), sipPeer.getViaAddress(), sipPeer.getPort());
			if (sipPeer.isRportSet()) via.setRport();
			
			String branch=sipPeer.pickBranch(msgPeer);
			via.setBranch(branch);
			
			msgPeer.addViaHeader(via);

			//decrement Max-Forwards
			MaxForwardsHeader maxfwd=msgPeer.getMaxForwardsHeader();
			if (maxfwd!=null) maxfwd.decrement();
			else maxfwd=new MaxForwardsHeader(SipStack.max_forwards);
			msgPeer.setMaxForwardsHeader(maxfwd);
			
			//update Request line with the remote sip address of peer, in this way the sip message will come forwarded to Router/NAT
			RequestLine reqLine = new RequestLine(msgPeer.getRequestLine().getMethod(), getRemotePeerAddress().getAddress());
			msgPeer.removeRequestLine();
			msgPeer.setRequestLine(reqLine);
			
			//forward sip request to local peer
			TransactionClient transactionClient=new TransactionClient(sipPeer, msgPeer, this);
			transactionClient.request();


		}

	}

	/**
	 *Sends SIP MESSAGE
	 * 
	 */
	public void sendMessageRequest(NameAddress to, String text)
	{  
	
		Message message=MessageFactory.createMessageRequest(sipGatewayPeer, to, getAddress(), null, "application/text", text);

		TransactionClient transactionClient=new TransactionClient(sipGatewayPeer, message, this);
		transactionClient.request();

	}
	
	/**
	 * Sends SIP MESSAGE
	 * 
	 */
	public void sendMessageRequest(NameAddress to, NameAddress remoteAdd, String text)
	{  
	
		Message message=MessageFactory.createMessageRequest(sipGatewayPeer, remoteAdd, getAddress(), null, "application/text", text);
		message.setToHeader(new ToHeader(to));
		
		TransactionClient tClient=new TransactionClient(sipGatewayPeer, message, this);
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
	public void onTransProvisionalResponse(TransactionClient arg0, Message arg1) {
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
	public void onTransTimeout(TransactionClient tc) {
		

	}

	/**
	 * Get the name of the peer associated to GatewayPeer
	 * 
	 * @return
	 */
	public String getNamePeer() {
		return this.namePeer;
	}

	/**
	 * Set the name of the peer associated to GatewayPeer
	 * 
	 * @param namePeer
	 */
	public void setNamePeer(String namePeer) {
		this.namePeer = namePeer;
	}

	/**
	 * Get the remote peer address, generally assigned by the NAT
	 * 
	 * @return
	 */
	public NameAddress getRemotePeerAddress() {
		return this.remotePeerAddress;
	}

	/**
	 * Set the remote peer address, generally assigned by the NAT
	 * 
	 * @param remoteAddress
	 * @param remotePort
	 */
	public void setRemotePeerAddress(String remoteAddress, int remotePort) {

		SipURL sipRemoteAddress = new SipURL(getNamePeer(), remoteAddress, remotePort);
		this.remotePeerAddress = new NameAddress(getNamePeer(), sipRemoteAddress);
	}

	/**
	 * Get the GatewayPeer address 
	 * 
	 * @return
	 */
	public NameAddress getAddress() {
		return this.address;
	}
	
	/**
	 * Set the GatewayPeer address 
	 * 
	 * @param address
	 */

	public void setAddress(NameAddress address) {

		this.address = address;
	}

	
	/**
	 * Get the local peer address 
	 * 	  
	 * @return
	 */
	public NameAddress getLocalPeerAddress() {
		return this.localPeerAddress;
	}

    /**
     * Set the local peer address 
     * 
     * @param viaAddress
     */
	public void setLocalPeerAddress(SipURL viaAddress) {
		SipURL sipLocalPeerAddress = new SipURL(getNamePeer(), viaAddress.getHost(), viaAddress.getPort());
		this.localPeerAddress = new NameAddress(getNamePeer(), sipLocalPeerAddress);
	}

    /**
     * Build a SIP NameAddress
     * 
     * @param namePeer
     * @param address
     * @param port
     * @return
     */
	public NameAddress makeNameAddress(String namePeer, String address, int port){
		
		SipURL sipAddress = new SipURL(namePeer, address, port);
		if(namePeer.equals(null))
			return new NameAddress(getNamePeer(), sipAddress);
		else
			return new NameAddress(namePeer, sipAddress);
		
	}



}
