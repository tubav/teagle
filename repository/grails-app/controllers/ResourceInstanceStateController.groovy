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
 *  This is the controller class for the ResourceInstanceState domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class ResourceInstanceStateController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render ResourceInstanceState.list() as XML
    }

    def rest_show = {
        def resourceInstanceStateInstance = ResourceInstanceState.get(params.id)
        if (resourceInstanceStateInstance) {
            response.setCharacterEncoding("utf-8")
            render resourceInstanceStateInstance as XML
        } else {
            SendNotFoundResponse()
        }
        
    }

    private def SendNotFoundResponse() {
        response.status = 404
        render contentType: "application/xml", {
            errors {
                message("item not found with id: " + params.id)
            }
        }
    }
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ resourceInstanceStateInstanceList: ResourceInstanceState.list( params ), resourceInstanceStateInstanceTotal: ResourceInstanceState.count() ]
    }

    def show = {
        def resourceInstanceStateInstance = ResourceInstanceState.get( params.id )

        if(!resourceInstanceStateInstance) {
            flash.message = "ResourceInstanceState not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ resourceInstanceStateInstance : resourceInstanceStateInstance ] }
    }

    def delete = {
        def resourceInstanceStateInstance = ResourceInstanceState.get( params.id )
        if(resourceInstanceStateInstance) {
            try {
                resourceInstanceStateInstance.delete(flush:true)
                flash.message = "ResourceInstanceState ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "ResourceInstanceState ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "ResourceInstanceState not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def resourceInstanceStateInstance = ResourceInstanceState.get( params.id )

        if(!resourceInstanceStateInstance) {
            flash.message = "ResourceInstanceState not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ resourceInstanceStateInstance : resourceInstanceStateInstance ]
        }
    }

    def update = {
        def resourceInstanceStateInstance = ResourceInstanceState.get( params.id )
        if(resourceInstanceStateInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(resourceInstanceStateInstance.version > version) {
                    
                    resourceInstanceStateInstance.errors.rejectValue("version", "resourceInstanceState.optimistic.locking.failure", "Another user has updated this ResourceInstanceState while you were editing.")
                    render(view:'edit',model:[resourceInstanceStateInstance:resourceInstanceStateInstance])
                    return
                }
            }
            resourceInstanceStateInstance.properties = params
            if(!resourceInstanceStateInstance.hasErrors() && resourceInstanceStateInstance.save()) {
                flash.message = "ResourceInstanceState ${params.id} updated"
                redirect(action:show,id:resourceInstanceStateInstance.id)
            }
            else {
                render(view:'edit',model:[resourceInstanceStateInstance:resourceInstanceStateInstance])
            }
        }
        else {
            flash.message = "ResourceInstanceState not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def resourceInstanceStateInstance = new ResourceInstanceState()
        resourceInstanceStateInstance.properties = params
        return ['resourceInstanceStateInstance':resourceInstanceStateInstance]
    }

    def save = {
        def resourceInstanceStateInstance = new ResourceInstanceState(params)
        if(!resourceInstanceStateInstance.hasErrors() && resourceInstanceStateInstance.save()) {
            flash.message = "ResourceInstanceState ${resourceInstanceStateInstance.id} created"
            redirect(action:show,id:resourceInstanceStateInstance.id)
        }
        else {
            render(view:'create',model:[resourceInstanceStateInstance:resourceInstanceStateInstance])
        }
    }
}
