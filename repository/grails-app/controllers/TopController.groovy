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
 * author: Eamonn Power
 *
 * </copyright>
 *
 *  This is the controller class for the Top domain.
 */

import grails.converters.*

class TopController{
    def rest_list = {
        render(contentType:"text/xml"){
            repository{
                for(c in grailsApplication.controllerClasses){
                    // sort out if the controller supports RESTful methods
                    if(c.hasProperty("rest_show")){
                        store(link:grailsApplication.config.grails.serverURL + "/rest/" + controllerName(c.getName())){
                            name(c.getName())
                        }
                    }
                }
            }
        }
    }
    
    /*
    *   Converts a given class name to the correct case for making a valid controller URI
    */
    def controllerName(className){
        String result = className
        
        // change the first character to lower case
        result = className[0].toLowerCase() + className.getAt(1..className.size()-1)
        
        return result
    }
}
