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


import org.zoolu.sip.address.SipURL;

/**
 * Class <code>Address</code> is used to represent a 
 * valid address for peer in the form:
 * <br> 
 * <code>peername@pcdomain:port</code>
 * <p>
 * It forms a SIP address.
 * 
 * @author Fabrizio Caramia
 *
 */

public class Address extends SipURL{
	
	/**
	 * Create a new address from a SIP URL
	 * 
	 * @param sipURL
	 */

	public Address(SipURL sipURL) {
		super(sipURL);
		
	}
	
	/**
	 * Create a new address from a URL string
	 * 
	 * @param url URL string
	 */

	public Address(String url) {
		super(url);

	}
	
	/**
	 * Create a new address from an host name and a port number
	 * 
	 * @param hostname the host name 
	 * @param portnumber the port number
	 */
	
	public Address(String hostname, int portnumber) {
		super(hostname, portnumber);
		
	}

	/**
	 * Create a new address from an peer name, an host name and a port number
	 * 
	 * @param peername the peer name
	 * @param hostname the host name 
	 * @param portnumber the port number
	 */
	public Address(String peername, String hostname, int portnumber) {
		super(peername, hostname, portnumber);
		
	}


	/**
	 * Create a new address from an user name and the host name
	 * 
	 * @param peername the peer name
	 * @param hostname the host name 
	 */
	public Address(String peername, String hostname) {
		super(peername, hostname);
		
	}
	
	
	/**
	 * Get URL String in the format <code>peername@pcdomain:port</code>
	 * 
	 * @return URL String
	 */
	public String getURL() {
		return toString().substring(4);
	}
	
	

}
