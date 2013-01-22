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
 */

 class Product {

//     static mapping = {
//        providesResources cascade: "all-delete-orphan"
//    }

    /**
     *  The name of the product 
     */
    String commonName

    /**
     *  A description of the product.
     */
    String description

    static hasMany = [providesResources:ResourceInstance]
        
    static constraints = {
        commonName(blank:false)
    }
}
