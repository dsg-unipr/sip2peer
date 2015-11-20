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


import org.zoolu.sip.address.SipURL;


/**
 * Class <code>TestNATListener</code> listen for message response from local peer
 * 
 * @author Fabrizio Caramia
 *
 */
public interface TestNATListener {
	/**
	 * When the TestNATPeer sends a SIP MESSAGE to local peer and receives a response
	 * 
	 * @param type the type of the test
	 * @param localAddress local peer address
	 * @param remoteAddress remote peer address with IP assigned by NAT
	 */
	public void onSuccessTest(String type, SipURL localAddress, SipURL remoteAddress);
	
	/** 
	 * When the TestNATPeer send a SIP MESSAGE to local peer and don't receives a response
	 *
	 * @param type the type of the test
	 * @param localAddress local peer address
	 * @param remoteAddress remote peer address with IP assigned by NAT
	 */
	  
	public void onFailureTest(String type, SipURL localAddress, SipURL remoteAddress);
}
