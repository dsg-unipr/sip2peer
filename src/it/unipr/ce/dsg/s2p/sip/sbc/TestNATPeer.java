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


import org.zoolu.sip.message.Message;
import org.zoolu.sip.transaction.TransactionClient;

/**
 * Class <code>TestNATPeer</code> extends GatewayPeer.
 * The TestNATPeer check if a peer is behind NAT.
 * 
 * 
 * @author Fabrizio Caramia
 */

public class TestNATPeer extends GatewayPeer {

	private TestNATListener testListener; 

	/**
	 * Create a TestNATPeer
	 * 
	 * @param namePeer TestNATPeer name
	 * @param listenPort port associated
	 * @param testListener listener
	 */
	public TestNATPeer(String namePeer, int listenPort, TestNATListener testListener) {
		super(namePeer, listenPort);

		this.testListener=testListener;
	}

	public void onTransTimeout(TransactionClient tc) {
		//toHeader contain the local address
		//Request Line contain the remote address
		Message msg=tc.getRequestMessage();
		this.testListener.onFailureTest("Timeout", msg.getToHeader().getNameAddress().getAddress(), msg.getRequestLine().getAddress());
	}

	public void onTransFailureResponse(TransactionClient tc, Message msg) {
		
		Message msgReq=tc.getRequestMessage();
		this.testListener.onFailureTest("Failure", msgReq.getToHeader().getNameAddress().getAddress(), msgReq.getRequestLine().getAddress());
	}

	public void onTransSuccessResponse(TransactionClient tc, Message msg) {
		
		Message msgReq=tc.getRequestMessage();
		this.testListener.onSuccessTest("Success", msgReq.getToHeader().getNameAddress().getAddress(), msgReq.getRequestLine().getAddress());

	}

}
