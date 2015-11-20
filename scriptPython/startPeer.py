#script to run FullPeer from Sip2Peer examples
import subprocess
import hashlib
import sys
import os
import math
import time
import signal

#signal handler
##def receive_signal(signum, stack):
##    print 'Received:', signum
##    procPeer.kill()
##
##signal.signal(signal.SIGINT, receive_signal)

if len(sys.argv)<2 or len(sys.argv)==4:
    print "usage: python startPeer.py peerexample.cfg -j"
    print "or: python startPeer.py peerexample.cfg name_peer peer_port -j"
    #print "for more info type: -h"
else:
    fileConfig = sys.argv[1]
    if os.path.exists(fileConfig): 
        #create command
        if len(sys.argv)==3:
            #create key from istant time
            key = hashlib.md5(repr(time.time())).hexdigest()
            option = sys.argv[2]
            javacmnd = ['java', '-cp', 'lib/sip.jar:bin/', 'it.unipr.ce.dsg.s2p.example.peer.FullPeer', fileConfig, key, option]
        elif len(sys.argv)==5:
            name = sys.argv[2]
            port = sys.argv[3]
            option = sys.argv[4]
            #create key from name + port + istant time
            key = hashlib.md5()
            key.update(name)
            key.update(port)
            key.update(repr(time.time()))
            javacmnd = ['java', '-cp', 'lib/sip.jar:bin/', 'it.unipr.ce.dsg.s2p.example.peer.FullPeer', fileConfig, key.hexdigest(), name, port, option]
        #run command
        procPeer = subprocess.Popen(javacmnd)
        while(True):
            data = raw_input('type "q" to terminate peer\n')
            if(data=="q"):
                procPeer.terminate()
                break
 
        
        
        


