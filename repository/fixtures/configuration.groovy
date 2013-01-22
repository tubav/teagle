/**
 * </copyright>
 *
 * 2008-2010 © Waterford Institute of Technology (WIT),
 *              TSSG, EU FP7 ICT Panlab.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * author: Shane Fox
 *
 * </copyright>
 *
 */

include "configParams"

fixture{

   AsteriskConfig(Configuration){
        commonName = "Asterisk Config"
        description= "Asterisk"
        configurationParamComposite = [AsteriskParams]
    }

//    AsteriskSpec(ResourceSpec){
//        commonName = "Asterisk Spec2"
//        description= "Asterisk spec"
//        configurationParameters = [PortParams]
//        //cost = [Cost1]
//    }
//
//    AsteriskInst(ResourceInstance){
//        commonName = "Asterisk Instance"
//        description= "bleh"
//        shared = true
//        resourceSpec = [AsteriskSpec]
//        configurationData = [AsteriskParams]
//        //state.id = 4
//    }

}

