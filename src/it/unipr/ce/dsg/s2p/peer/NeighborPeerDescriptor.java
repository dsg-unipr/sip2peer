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


/**
 * Class <code>NeighborPeerDescriptor</code> extends PeerDescriptor
 * adding the reachability information about the known peers. The reachability
 * is verified into <code>Peer</code> class.
 * 
 * @author Fabrizio Caramia
 *
 */


public class NeighborPeerDescriptor extends PeerDescriptor {
	
	private String localReachability;
	
	/**
	 * Create a new NeighborPeerDescriptor
	 */
	public NeighborPeerDescriptor() {
		super();
	}

	/**
	 * Create a new NeighborPeerDescriptor from a PeerDescriptor
	 * 
	 * @param peerDesc PeerDescriptor
	 */
	public NeighborPeerDescriptor(PeerDescriptor peerDesc) {
		super(peerDesc.getName(), peerDesc.getAddress(), peerDesc.getKey(), peerDesc.getContactAddress());
		setLocalReachability(null);
	}

	/**
	 * Get local reachability information
	 * @return local reachability information
	 */
	public String localReachability() {
		return localReachability;
	}

	/**
	 * Set local reachability information
	 * @param localReachability local reachability information
	 */
	public void setLocalReachability(String localReachability) {
		this.localReachability = localReachability;
	}

}
