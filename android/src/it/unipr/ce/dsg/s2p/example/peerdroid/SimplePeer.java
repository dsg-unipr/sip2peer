package it.unipr.ce.dsg.s2p.example.peerdroid;

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

import java.util.ArrayList;
import java.util.Iterator;

import android.widget.Toast;

import it.unipr.ce.dsg.s2p.example.msg.JoinMessage;
import it.unipr.ce.dsg.s2p.example.msg.PeerListMessage;
import it.unipr.ce.dsg.s2p.example.msg.PingMessage;
import it.unipr.ce.dsg.s2p.org.json.JSONException;
import it.unipr.ce.dsg.s2p.org.json.JSONObject;
import it.unipr.ce.dsg.s2p.peer.NeighborPeerDescriptor;
import it.unipr.ce.dsg.s2p.peer.Peer;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;
import it.unipr.ce.dsg.s2p.sip.Address;

/**
 * Class <code>SimplePeer</code> implements many features of a peer.
 * SimplePeer extend the Peer class of sip2peer.
 * 
 * 
 * @author Fabrizio Caramia
 *
 */

public class SimplePeer extends Peer {
	
	private Address bootstrapPeer = null;
	private PeerActivity peerActivity=null;
	
	
	public SimplePeer(String pathConfig, String key, String peerName,
			int peerPort) {
		super(pathConfig, key, peerName, peerPort);
		
	}
	
	public String getAddressPeer(){
		
		
		return getAddress().getURL();
	}
	
	public String getContactAddressPeer(){
		
		return peerDescriptor.getContactAddress();
	}
	
	public ArrayList<String> getListAddressPeer(){

		
		ArrayList<String> addressList = new ArrayList<String>();
		
		Iterator<NeighborPeerDescriptor> iter = this.peerList.values().iterator();
		
		PeerDescriptor peerDesc = new PeerDescriptor();
		
		Integer sizeList = new Integer(this.peerList.size());
		
		
		while(iter.hasNext()){
			
			peerDesc = (PeerDescriptor) iter.next();	
			addressList.add(peerDesc.getContactAddress());
			
		}
		
		return addressList;
	}
	
	public void pingToPeer(String address){
		
		PingMessage newPingMsg = new PingMessage(peerDescriptor);

		//!!!!!!send to local address 
		send(new Address(address), newPingMsg);

	}
	
    public void joinToPeer(Address address){
		
		JoinMessage newJoinMsg = new JoinMessage(peerDescriptor);

		//!!!!!!send to local address 
		send(new Address(address), newJoinMsg);

	}
	
	public void setConfiguration(String sbc, String bootstrap, String reachability){
		
		nodeConfig.sbc=sbc;
		nodeConfig.test_address_reachability=reachability;
		setBootstrapPeer(new Address(bootstrap));
		
	}
	
	public void contactSBC(){
		
		requestPublicAddress();
		
	}

	public Address getBootstrapPeer() {
		return bootstrapPeer;
	}

	private void setBootstrapPeer(Address bootstrapPeer) {
		this.bootstrapPeer = bootstrapPeer;
	}
	
	public String getSBCAddress(){
		
		return nodeConfig.sbc;
	}
	

	@Override
	protected void onReceivedJSONMsg(JSONObject jsonMsg, Address sender) {
		
		try {
			
			JSONObject params = jsonMsg.getJSONObject("payload").getJSONObject("params");
			
			if(jsonMsg.get("type").equals(PeerListMessage.MSG_PEER_LIST)){
				
				  PeerActivity.handler.post(new Runnable() {
	                  public void run() {
	             		 	Toast toast = Toast.makeText(peerActivity.getBaseContext(),"Received: "+ PeerListMessage.MSG_PEER_LIST ,Toast.LENGTH_LONG);
	             		 	toast.show();
	                      }
	              });
				

				Iterator<String> iter = params.keys();
				
				

				while(iter.hasNext()){

					String key = (String) iter.next();

					JSONObject keyPeer = params.getJSONObject(key);
					PeerDescriptor neighborPeerDesc = new PeerDescriptor(keyPeer.get("name").toString(), keyPeer.get("address").toString(), keyPeer.get("key").toString());

					if(keyPeer.get("contactAddress").toString()!="null")
						neighborPeerDesc.setContactAddress(keyPeer.get("contactAddress").toString());
					
					
					addNeighborPeer(neighborPeerDesc);
					
					Integer size = new Integer(this.peerList.size());
					
				}

			}
			if(jsonMsg.get("type").equals(PingMessage.MSG_PEER_PING))
			{
				PeerActivity.handler.post(new Runnable() {
	                  public void run() {
	             		 	Toast toast = Toast.makeText(peerActivity.getBaseContext(),"Received: "+ PingMessage.MSG_PEER_PING ,Toast.LENGTH_LONG);
	             		 	toast.show();
	                      }
	              });
				
				PeerDescriptor neighborPeerDesc = new PeerDescriptor(params.get("name").toString(), params.get("address").toString(), params.get("key").toString(), params.get("contactAddress").toString());
				addNeighborPeer(neighborPeerDesc);
				
			}
			
			
		} catch (JSONException e) {
		
			e.printStackTrace();
		}
		
		
	}

	public void setPeerActivity(PeerActivity peerActivity) {
		this.peerActivity=peerActivity;
		
	}

	@Override
	protected void onDeliveryMsgFailure(String arg0, Address arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDeliveryMsgSuccess(String arg0, Address arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

		

}
