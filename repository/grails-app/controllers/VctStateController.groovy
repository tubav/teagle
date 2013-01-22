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
 *  This is the controller class for the VctState domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class VctStateController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render VctState.list() as XML
    }

    def rest_show = {
        def vctStateInstance = VctState.get(params.id)
        if (vctStateInstance) {
            response.setCharacterEncoding("utf-8")
            render vctStateInstance as XML
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
        [ vctStateInstanceList: VctState.list( params ), vctStateInstanceTotal: VctState.count() ]
    }

    def show = {
        def vctStateInstance = VctState.get( params.id )

        if(!vctStateInstance) {
            flash.message = "VctState not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ vctStateInstance : vctStateInstance ] }
    }

    def delete = {
        def vctStateInstance = VctState.get( params.id )
        if(vctStateInstance) {
            try {
                vctStateInstance.delete(flush:true)
                flash.message = "VctState ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "VctState ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "VctState not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def vctStateInstance = VctState.get( params.id )

        if(!vctStateInstance) {
            flash.message = "VctState not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ vctStateInstance : vctStateInstance ]
        }
    }

    def update = {
        def vctStateInstance = VctState.get( params.id )
        if(vctStateInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(vctStateInstance.version > version) {
                    
                    vctStateInstance.errors.rejectValue("version", "vctState.optimistic.locking.failure", "Another user has updated this VctState while you were editing.")
                    render(view:'edit',model:[vctStateInstance:vctStateInstance])
                    return
                }
            }
            vctStateInstance.properties = params
            if(!vctStateInstance.hasErrors() && vctStateInstance.save()) {
                flash.message = "VctState ${params.id} updated"
                redirect(action:show,id:vctStateInstance.id)
            }
            else {
                render(view:'edit',model:[vctStateInstance:vctStateInstance])
            }
        }
        else {
            flash.message = "VctState not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def vctStateInstance = new VctState()
        vctStateInstance.properties = params
        return ['vctStateInstance':vctStateInstance]
    }

    def save = {
        def vctStateInstance = new VctState(params)
        if(!vctStateInstance.hasErrors() && vctStateInstance.save()) {
            flash.message = "VctState ${vctStateInstance.id} created"
            redirect(action:show,id:vctStateInstance.id)
        }
        else {
            render(view:'create',model:[vctStateInstance:vctStateInstance])
        }
    }
}
