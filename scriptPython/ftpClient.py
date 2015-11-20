#client FTP
import os
import sys
from ftplib import FTP

def directoryExists(iftp, directoryName):
    filelist = []
    iftp.retrlines('LIST',filelist.append)
    for f in filelist:
        if f.split()[-1] == directoryName:
            return True
    return False


#download file
def download(filen, iftp):
        #ftp.cwd(directory)
        filedown = open(filen, 'wb')
        try:
                iftp.retrbinary('RETR ' + filen, filedown.write)
                print "file downloaded!"
        except:
                print "download error"
        filedown.close()
        return

#upload file
def upload(filename, iftp):
        if os.path.isdir(filename):
                # check if directory exists on remote host
                baseDir = os.path.basename(filename)
                if not directoryExists(iftp, baseDir):
                        #create directory
                        iftp.mkd(baseDir)
                #move to filename directory system
                os.chdir(filename)
                #move to filename directory FTP
                iftp.cwd(baseDir)
                for file in os.listdir(os.curdir):
                        fileup = open(file, 'rb')
                        try:
                                iftp.storbinary('STOR '+ file, fileup)
                                #print "file uploaded!"
                        except:
                              print "upload error"
                        fileup.close() 
        return
##        else:
##                print "file"
##        fileup = open(filename, 'rb')
##        try:
##                iftp.storbinary('STOR '+ filename, fileup)
##                print "file uploaded!"
##        except:
##                print "upload error"
##        fileup.close()


print "FTP client"
host = sys.argv[1]
user = sys.argv[2]
password = sys.argv[3]

try:
        ftp = FTP(host)
except:
        #print "Host could not be resolved."
        sys.exit()
else: pass
try:        
        ftp.login(user, password)
except:
        #print "username or password incorrect"
        sys.exit()
else:
	pass
        #print "*** connection established ***"

#ftp.retrlines('LIST')

#print "1 - download\n2 - upload"
#num = raw_input("insert number: ")
#directory = raw_input("insert directory: ")
filename= sys.argv[4]

##if(num=="1"):
##        download(filename, ftp)
##elif(num=="2"):
upload(filename, ftp)
#close connection
ftp.quit()
