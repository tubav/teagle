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
 * author: Shane Fox, Eamonn Power
 *
 * </copyright>
 *
 *  Person contains the information necessary to identify and contact a user of the system.
 */

class Person {

    /**
     *  The Organisation class owns the Person to Organisation relationship.
     *  So a person can be deleted without affecting the organisation but deleting
     *  the organisation also deletes the associated people.
     */
    static belongsTo = Organisation

//    static mapping = {
//        organisations cascade: "all"
//    }

    /**
     *  The username of the Person.
     */
    String userName
    
    /**
     *  The password for the Person.
     */
    String password

    /**
     *  The fullName of the Person.
     */
    String fullName

    String email

    /**
     *  emails - a person can have a number (1..*) of associated email addresses. <br>
     *  personRoles - a person can have a number of roles (1..*), these roles can be evaluated for permission to access functions.<br>
     *  organisations - a person is affiliated with one or more (1..*) organisations.
     */
    static hasMany = [
        personRoles:PersonRole,
        organisations:Organisation
    ]
        
    /**
     *  userName    -   The person's name must be unique and a mimimum of 4 characters long.<br>
     *  password    -   The minimum size for a password is 4.
     */
    static constraints = {
        userName(blank:false, unique:true, minSize:3)
        password(blank:false, minSize:4)
        fullName(nullable:true)
	email(email:true)
    }
}
