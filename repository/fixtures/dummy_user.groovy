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
 * A fixture to load a sample Person and create Person Roles
 *
 */

fixture{

    PanlabCustomer(PersonRole){
        name = "Panlab Customer"
    }
    PanlabPartner(PersonRole){
        name = "Panlab Partner"
    }
    TeagleAdministrator(PersonRole){
        name = "Teagle Administrator"
    }
    VCTUser(PersonRole){
        name = "VCT User"
    }
    rootUser(Person){
        userName = "demo"
        password = "fe01ce2a7fbac8fafaed7c982a04e229"
        fullName = "Administrator"
        personRoles = [PanlabCustomer]
    }

}

