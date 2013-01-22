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
 * ConfigParamAtomic is the leaf node of the ConfigParam composite pattern, which represents configuration parameters
 * that can be managed. ConfigParamAtomic is a concrete base class for representing configuration parameters that can
 * be managed as their own atomic entities.
 * 
 */

class ConfigParamAtomic extends ConfigParam {

    static belongsTo = ConfigParam
    
    String configParamType
    String defaultParamValue
    
    static constraints = {
    }
}
