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

    email1(Email){
        address = "shane@somewhere.com"
    }
    email2(Email){
        address = "shane@somewhere_else.com"
    }
    email3(Email){
        address = "epow@anemailprovider.com"
    }
    admin(PersonRole){
        name = "Administrator"
    }
    user(PersonRole){
        name = "user"
    }
    shane(Person){
        userName = "sfox"
        password = "test"
        fullName = "Shane Fox, Eamonn Power"
        emails = [email1, email2]
        personRoles = [user]
    }
 /*   eamonn(Person){
        userName = "epower"
        password = "epower"
        fullName = "Eamonn Power"
        emails = [email3]
        personRoles = [user]
    }
   */
}