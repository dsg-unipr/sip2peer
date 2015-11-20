package it.unipr.ce.dsg.s2p.example.msg;

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
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;

/**
 * Class <code>JoinMessage</code> implements a simple message sent by the peer to Bootstrap Peer.
 * The payload of JoinMessage contains the peer descriptor.
 * 
 * @author Fabrizio Caramia
 *
 */

public class JoinMessage extends BasicMessage{
	
	private int numPeerList;
	
	
	public static final String MSG_PEER_JOIN="peer_join"; 

	public JoinMessage(PeerDescriptor peerDesc) {
		
		super(MSG_PEER_JOIN, new Payload(peerDesc));
		
		/**
		 * number of the neighbor peers in the list
		 * 
		 * If numPeerList=0 the BootstrapPeer sends full peer list
		 * If numPeerList=-1 the BootstrapPeer not sends the peer list
		 * Per default all peer are requested
		 * 
		 */
		setNumPeerList(0);
	}

	public int getNumPeerList() {
		return numPeerList;
	}

	public void setNumPeerList(int numPeerList) {
		this.numPeerList = numPeerList;
	}
	
	
	

}
