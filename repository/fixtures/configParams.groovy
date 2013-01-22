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

fixture{
    IPParams(ConfigParamAtomic){
        commonName = "Internet Address"
        description= "bleh"
        configParamType = "IP"
        defaultParamValue = "127.0.0.1"
    }
    PortParams(ConfigParamAtomic){
        commonName = "Port"
        description= "TCP"
        configParamType = "string"
        defaultParamValue = "8081"
    }

    AsteriskParams(ConfigParamComposite){
        commonName = "Asterisk"
        description= "Asterisk"
        this.configParams = [IPParams]
        this.configParams = [PortParams]
    }
//    AsteriskParams2(ConfigParamComposite){
//        commonName = "Asterisk2"
//        description= "Asterisk"
//        configParams = [IPParams]
//        configParams = [PortParams]
//    }
}
