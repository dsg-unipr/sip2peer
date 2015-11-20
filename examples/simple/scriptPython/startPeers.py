#script to run more FullPeer from Sip2Peer examples
import subprocess
import hashlib
import sys
import os
import math
import time

if(len(sys.argv)<3 or sys.argv[1]=="-h"):
    print("usage: python startPeers.py fileconfig.cfg -option")
    print("option:\n-j join to bootstrap \n-jp join to bootstrap and send ping message to (random) peer taken from its list")
    print("-jr join to bootstrap and send ping message to (random) peer recursively")
    print("-a contact SBC, join to bootstrap and send ping message to (random) peer")
    #print("-p send ping message to peer. NOTE: after '-option' insert the peer address")
    print("-sd contatct SBC to request public address and after disconnect it")
    sys.exit(0)

fileConfig = sys.argv[1]
option = sys.argv[2]

datainput = raw_input('insert min port: ')
minPort = int(datainput)
datainput = raw_input('insert the number of peers: ')
numPort = int(datainput)
if(numPort>1):
    interval = int(raw_input('insert the interval time (in seconds) to create each new peer: '))
heap = raw_input('insert max heap memory: ')
memheap = '-Xmx'+heap+'m'

listPeersProcess = list()
 
for n in range(numPort):
    key = hashlib.md5(repr(time.time())).hexdigest()
    namePeer = "peer"+str(n)
    javacmnd = ['java', '-cp', 'lib/sip.jar:bin/', memheap, 'it.unipr.ce.dsg.s2p.example.peer.FullPeer', fileConfig, key, namePeer, str(minPort+n), option]
    #run command
    procPeer = subprocess.Popen(javacmnd)
    listPeersProcess.append(procPeer)
    if(numPort>1):
        time.sleep(interval)
    
while(True):
    data = raw_input('type "q" to terminate peers\n')
    if(data=="q"):
        #subprocess.call(['killall', 'java'])
        for proc in listPeersProcess:
            proc.terminate()
        break
