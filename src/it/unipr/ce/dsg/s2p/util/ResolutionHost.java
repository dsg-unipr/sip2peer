package it.unipr.ce.dsg.s2p.util;

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


import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.zoolu.sip.address.SipURL;

import it.unipr.ce.dsg.s2p.sip.Address;


/**
 * Class <code>ResolutionHost</code> verifies if a node is reached.
 * 
 * @author Fabrizio Caramia
 *
 */

public class ResolutionHost {
	
	private String addressHost;
	
	/**
	 * Create a new ResolutionHost
	 * 
	 * @param address the address must be verified
	 */
	
	public ResolutionHost(Address address){
		
		this.addressHost = address.getHost();
	}
	
	/**
	 * Create a new ResolutionHost
	 * 
	 * @param address the address must be verified
	 */
	public ResolutionHost(SipURL address){
		
		this.addressHost = address.getHost();
	}
	
	/**
	 *  Create a new ResolutionHost
	 * 
	 * @param address the address must be verified
	 */
	public ResolutionHost(String address){
		
		this.addressHost = address;
	}
	
	/**
	 * Check if the address is reached
	 * 
	 * @param timeout max interval time in milliseconds to verify the reachability
	 * @return return true if the node is reached
	 */
	synchronized public boolean isReachable(int timeout){
		
		boolean reachability=false;
		
		try {
			InetAddress address = InetAddress.getByName(addressHost);
			
			reachability =address.isReachable(timeout);
			
			
		} catch (UnknownHostException e) {
			reachability=false;
		} catch (IOException e) {
			reachability=false;
		}
		
		return reachability;
		
		
	}
	


}
