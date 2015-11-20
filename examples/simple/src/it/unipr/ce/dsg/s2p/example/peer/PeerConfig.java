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


import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;

/**
 * Class for peer configuration
 * 
 * @author Fabrizio Caramia
 *
 */
public class PeerConfig extends Configure {


	/*
	 * Address of the bootstrap peer, for ex. bootstrap@192.168.1.2:8090
	 * 
	 */
	public String bootstrap_peer = null;
	
	/*
	 *Number of peers returned from BootstrapPeer from its list
	 *Default value: 0 (return all peer)
	 */
	public int req_npeer = 0;  



	public PeerConfig(String file){

		// load configuration
		loadFile(file);


	}


	/** Parses a single line (loaded from the config file) */
	protected void parseLine(String line)
	{  

		String attribute;
		Parser par;
		int index=line.indexOf("=");
		if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
		else {  attribute=line; par=new Parser("");  }


		if (attribute.equals("bootstrap_peer")) 	  {  bootstrap_peer=par.getString();  return;  } 
		if (attribute.equals("req_npeer")) 	  {  req_npeer=par.getInt();  return;  } 
	}




}
