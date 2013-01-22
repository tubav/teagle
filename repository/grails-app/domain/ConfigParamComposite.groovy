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
 * This class is the composite side of the ConfigParam composition.
 *
 */

class ConfigParamComposite extends ConfigParam {

    static mapping = {
        configParams cascade: "all-delete-orphan"
    }

    static belongsTo = [ConfigParam]
    
    static hasMany = [configParams:ConfigParam]
    
    static constraints = {
    }
}
