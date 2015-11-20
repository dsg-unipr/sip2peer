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


import org.zoolu.tools.Configure;
import org.zoolu.tools.Parser;

/**
 * Class <code>NodeConfig</code> is useful for node/peer configuration.
 * 
 * @author Fabrizio Caramia
 */

public class NodeConfig extends Configure {

	/* ********************** node/peer configuration ********************* */

	/** 
	 * peer name.
	 * Default value: null
	 */
	public String peer_name=null;

	/**
	 * Address of the Session Border Controller peer,
	 * Default value: null
	 */
	public String sbc=null;


	/** 
	 * Keep alive time
	 * Default value: 7000 ms 
	 * */
	public long keepalive_time=7000;

	/** 
	 * Format of the message, use text if the format is a simple string.
	 * Default value: json  */
	public String content_msg="json";


	/**
	 * Path folder for peer list
	 * Default value: null
	 * */
	public String list_path=null;

	/**
	 * Path folder for peer log
	 * Default value: null
	 */
	public String log_path=null;

	/**
	 * Test if a neighbor peer is reached by "address" value of the PeerDescriptor
	 * This test is processed when a new PeerDescriptor is added in the peer list
	 * Default value: yes
	 */

	public String test_address_reachability="yes";


	public NodeConfig(){
		
		init();
	}
	
	public NodeConfig(String file){

		// load configuration
		loadFile(file);
		// post-load manipulation     
		init();
	}
	

	private void init()
	{  
		if (peer_name!=null && peer_name.equalsIgnoreCase(Configure.NONE)) peer_name=null;

	}


	/** Parses a single line (loaded from the config file) */
	protected void parseLine(String line)
	{  

		String attribute;
		Parser par;
		int index=line.indexOf("=");
		if (index>0) {  attribute=line.substring(0,index).trim(); par=new Parser(line,index+1);  }
		else {  attribute=line; par=new Parser("");  }

		if (attribute.equals("peer_name"))      	 {  peer_name=par.getString();  return;  }

		if (attribute.equals("keepalive_time")) {  keepalive_time=par.getInt();  return;  } 
		if (attribute.equals("content_msg")) 	  {  content_msg=par.getString();  return;  } 

		if (attribute.equals("sbc")) 	  		{  sbc=par.getString();  return;  } 

		if (attribute.equals("list_path")) 	  {  list_path=par.getString();  return;  } 

		if (attribute.equals("log_path")) 	  {  log_path=par.getString();  return;  } 

		if (attribute.equals("test_address_reachability")) 	  { test_address_reachability =par.getString();  return;  } 
	}

}
