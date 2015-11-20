import subprocess

javacmnd = ['java', '-cp', 'lib\sip.jar;lib\server.jar;bin','it.unipr.ce.dsg.s2p.sip.sbc.SessionBorderController', 'sbc.cfg']
procSBC = subprocess.Popen(javacmnd)
while(True):
    data = raw_input('type "q" to terminate SBC\n')
    if(data=="q"):
        procSBC.terminate()
        break

