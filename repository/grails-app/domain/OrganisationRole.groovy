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
 *  This class represents the part played by an Organisation in a given context.
 */

class OrganisationRole {
    /**
     *  The name of the organisation role.
     */
    String name

    static mapping = {
        tablePerHierarchy false
    }

    static belongsTo = Organisation

    // This aggregation defines the set of PersonRoles that this OrganisationRole has associated with it.
    static hasMany = [orgRoleHasPersonRoles:PersonRole, organisations:Organisation]
        
    static constraints = {
        name (blank:false)
    }
}
