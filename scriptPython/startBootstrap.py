#script to run bootstrap peer from Sip2Peer examples
import subprocess
import hashlib

#create key
key = hashlib.md5("bootstrap@boostrap").hexdigest()

javacmnd = ['java', '-cp', 'lib/sip.jar:bin/', 'it.unipr.ce.dsg.s2p.example.peer.BootstrapPeer', 'config/bs.cfg', key]

procBoootstrap = subprocess.Popen(javacmnd)
while(True):
    data = raw_input('type "q" to terminate bootstrapPeer\n')
    if(data=="q"):
        procBoootstrap.terminate()
        break
        
