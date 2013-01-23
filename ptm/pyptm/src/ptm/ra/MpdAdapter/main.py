'''
Created on 21.06.2011

@author: kca
'''

if __name__ == '__main__':    
    from ptm import RARunner
    #RARunner.RARunner("SoftwareAdapter.SoftwareAdapter", parent = "/pnode-0", engine = "sqlite:////tmp/swa.db", port = 8889)
    RARunner.RARunner("MpdAdapter.MpdAdapter", parent = "/pnode-0", port = 9991)
