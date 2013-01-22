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

    ResourceInstanceStateNew(ResourceInstanceState){
        commonName = "NEW"
        description = "Please Describe"
    }
    ResourceInstanceStateProvisioned(ResourceInstanceState){
        commonName = "PROVISIONED"
        description = "Please Describe"
    }
    ResourceInstanceStateUnprovisioned(ResourceInstanceState){
        commonName = "UNPROVISIONED"
        description = "Please Describe"
    }
    VCTStateNew(VctState){
        commonName = "NEW"
        description = "Please Describe"
    }
    VCTStateProvisioned(VctState){
        commonName = "PROVISIONED"
        description = "Please Describe"
    }
    VCTStateUnProvisioned(VctState){
        commonName = "UNPROVISIONED"
        description = "Please Describe"
    }
    VCTStateBooked(VctState){
        commonName = "BOOKED"
        description = "Please Describe"
    }
    VCTStateUnbooked(VctState){
        commonName = "UNBOOKED"
        description = "Please Describe"
    }
    VCTStateInProgressWait(VctState){
        commonName = "INPROGRESS_WAIT"
        description = "Please Describe"
    }
    VCTStateInProgressSync(VctState){
        commonName = "INPROGRESS_SYNC"
        description = "Please Describe"
    }
    VCTStateInProgressASync(VctState){
        commonName = "INPROGRESS_ASYNC"
        description = "Please Describe"
    }

}

