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
 * Organisation is used to group users together.
 *
 */

class Organisation {

    /**
    *   The name of the organisation.  
    */
    String name

    /**
     *  An Organisation has 1..* people
     *  An Organisation has 1..* organisation roles
     *  An Organisation has 1..* resources
     */
    static hasMany = [
        people:Person,
        hasOrgRoles: OrganisationRole,
    ]
    Set people

    /** To delete children in the bi-directional relationship */
    static mapping = {
        people cascade: "all"
    }
    
    static constraints = {
        name(blank:false, unique:true)
    }
}
