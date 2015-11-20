package it.unipr.ce.dsg.s2p.example.msg;

import it.unipr.ce.dsg.s2p.message.BasicMessage;
import it.unipr.ce.dsg.s2p.message.Payload;
import it.unipr.ce.dsg.s2p.peer.PeerDescriptor;


/**
 * Class {@code PongMessage} implements a simple message sent by the peer to other peer.
 * The payload of PingMessage contains the peer descriptor.
 * 
 * @author Michele Minelli
 *
 */
public class PongMessage extends BasicMessage {
	
	public static final String MSG_PEER_PONG="peer_pong";
	
	public PongMessage(PeerDescriptor peerDescriptor) {
		super(MSG_PEER_PONG, new Payload(peerDescriptor));
	}

}
