Universtita' degli Studi di Parma
DSG - Distributed Systems Group 

Sip2Peer content:
-config: peer configuration files 
-doc: sip2peer java doc 
-lib: MjSIP and local server
-scriptPython: Python scripts to run example code
-src: source code

In order to run example peers is it possible to use Phyton scripts (Phyton version >= 2.6 ).  

Example:
-Start Bootstrap Peer
python scriptPython/startBootstrap.py

-Run a simple peer that send only a message to the Bootstrap Peer
python scriptPython/startPeer.py config/k.cfg -j

-Run a peer that send a Join message to the Bootstrap peer, receive the list of 
active peers and then send to those nodes a PING message recursively
python scriptPython/startPeer.py config/k.cfg -jr

- Start multiple peer instance
python scriptPython/startPeers.py config/default.cfg -j
or
python scriptPython/startPeers.py config/default.cfg -jr
or contacting the SBC
python scriptPython/startPeers.py config/default.cfg -a