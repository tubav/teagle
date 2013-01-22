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
 *  This is the controller class for the PhysicalResource domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class PhysicalResourceController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render PhysicalResource.list() as XML
    }

    def rest_show = {
        def physicalResourceInstance = PhysicalResource.get(params.id)
        if (physicalResourceInstance) {
            response.setCharacterEncoding("utf-8")
            render physicalResourceInstance as XML
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

    def rest_delete = {
        PhysicalResource physicalResourceInstance = PhysicalResource.get(params.id)
        if (!physicalResourceInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        PhysicalResource.withTransaction{
            status ->
            try{
                physicalResourceInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                PhysicalResource.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("PhysicalResource ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                PhysicalResource.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("PhysicalResource ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        PhysicalResource physicalResourceInstance = PhysicalResource.get(params.id)
        if (!physicalResourceInstance) {
            SendNotFoundResponse()
        }

        physicalResourceInstance.properties = params.physicalResourceInstance

        if (physicalResourceInstance.validate()) {
            physicalResourceInstance.save();
            response.status = 200
            render ""
        } else {
            sendValidationFailedResponse(physicalResourceInstance, 403)
        }
    }

    private def sendValidationFailedResponse(physicalResourceInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                physicalResourceInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def physicalResourceInstance = new PhysicalResource(params.physicalResourceInstance)

        if(physicalResourceInstance.save()){
            response.setCharacterEncoding("utf-8")
            render physicalResourceInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    physicalResourceInstance?.errors?.fieldErrors?.each {err ->
                        field(err.field)
                        message(g.message(error: err))
                    }
                }
            }
        }
    }
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ physicalResourceInstanceList: PhysicalResource.list( params ), physicalResourceInstanceTotal: PhysicalResource.count() ]
    }

    def show = {
        def physicalResourceInstance = PhysicalResource.get( params.id )

        if(!physicalResourceInstance) {
            flash.message = "PhysicalResource not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ physicalResourceInstance : physicalResourceInstance ] }
    }

    def delete = {
        def physicalResourceInstance = PhysicalResource.get( params.id )
        if(physicalResourceInstance) {
            // Allows control over Spring's Transactions
            PhysicalResource.withTransaction{
                status ->
                try{
                    physicalResourceInstance.delete()
                    flash.message = "PhysicalResource ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    PhysicalResource.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete PhysicalResource ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    PhysicalResource.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete PhysicalResource ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "PhysicalResource not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def physicalResourceInstance = PhysicalResource.get( params.id )

        if(!physicalResourceInstance) {
            flash.message = "PhysicalResource not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ physicalResourceInstance : physicalResourceInstance ]
        }
    }

    def update = {
        def physicalResourceInstance = PhysicalResource.get( params.id )
        if(physicalResourceInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(physicalResourceInstance.version > version) {
                    
                    physicalResourceInstance.errors.rejectValue("version", "physicalResource.optimistic.locking.failure", "Another user has updated this PhysicalResource while you were editing.")
                    render(view:'edit',model:[physicalResourceInstance:physicalResourceInstance])
                    return
                }
            }
            physicalResourceInstance.properties = params
            if(!physicalResourceInstance.hasErrors() && physicalResourceInstance.save()) {
                flash.message = "PhysicalResource ${params.id} updated"
                redirect(action:show,id:physicalResourceInstance.id)
            }
            else {
                render(view:'edit',model:[physicalResourceInstance:physicalResourceInstance])
            }
        }
        else {
            flash.message = "PhysicalResource not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def physicalResourceInstance = new PhysicalResource()
        physicalResourceInstance.properties = params
        return ['physicalResourceInstance':physicalResourceInstance]
    }

    def save = {
        def physicalResourceInstance = new PhysicalResource(params)
        if(!physicalResourceInstance.hasErrors() && physicalResourceInstance.save()) {
            flash.message = "PhysicalResource ${physicalResourceInstance.id} created"
            redirect(action:show,id:physicalResourceInstance.id)
        }
        else {
            render(view:'create',model:[physicalResourceInstance:physicalResourceInstance])
        }
    }
}
