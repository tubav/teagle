'''
Created on 24.04.2011

@author: kca
'''

from teagle.repository.tssg import TSSGRepository
from teagle.repository.entities import *

URL="http://localhost:8080/CoreRepository/rest"

def main():
    repo = TSSGRepository(URL)   
    
    _person = repo.get_entity(Person, 1) 

    _orga = repo.get_entity(Organisation, 1)
    
    vct = repo.get_entity(Vct, 1)
    
    print vct.instances
    print vct.instances[0].vcts
    print vct.instances[0].vcts[0] == vct
    print vct.instances[0].vcts[0] is vct

    #p = Ptm(commonName = "myPtm", provider = orga)
    
    #p.url = "http://localhost:8000/rest" 
    #repo.persist(p)

if __name__ == "__main__":
    main()