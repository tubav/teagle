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
 *  ResourceSpec defines the configuration parameters for a resource. It does this via the
 *  configurationParameters association. Essentially, this association defines the parameters
 *  needed to configure the resource.
 */

class ResourceSpec extends ManagementInfo {

    static mapping = {
        configurationParameters cascade: "all-delete-orphan"
    }
    
    static belongsTo = Organisation

    ConfigParam configurationParameters
    ResourceSpec inherits

    Boolean isInstantiable

    static constraints = {
	isInstantiable(nullable: true, defaultValue: true)
	inherits(nullable: true)
    }

    static hasMany = [keywords:Keyword]
}
