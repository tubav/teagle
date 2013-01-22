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
 *  This is the controller class for the Configlet domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class ConfigletController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Configlet.list() as XML
    }

    def rest_show = {
        def configletInstance = Configlet.get(params.id)
        if (configletInstance) {
            response.setCharacterEncoding("utf-8")
            render configletInstance as XML
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
        Configlet configletInstance = Configlet.get(params.id)
        if (!configletInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        Configlet.withTransaction{
            status ->
            try{
                configletInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Configlet.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Configlet ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Configlet.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Configlet ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        Configlet configletInstance = Configlet.get(params.id)
        if (!configletInstance) {
            SendNotFoundResponse()
        }

        configletInstance.properties = params.configletInstance

        if (configletInstance.validate()) {
            configletInstance.save();
            response.status = 200
            render ""
        } else {
            sendValidationFailedResponse(configletInstance, 403)
        }
    }

    private def sendValidationFailedResponse(configletInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                configletInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def configletInstance = new Configlet(params.configletInstance)

        if(configletInstance.save()){
            response.setCharacterEncoding("utf-8")
            render configletInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    configletInstance?.errors?.fieldErrors?.each {err ->
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
        [ configletInstanceList: Configlet.list( params ), configletInstanceTotal: Configlet.count() ]
    }

    def show = {
        def configletInstance = Configlet.get( params.id )

        if(!configletInstance) {
            flash.message = "Configlet not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ configletInstance : configletInstance ] }
    }

    def delete = {
        def configletInstance = Configlet.get( params.id )
        if(configletInstance) {
            // Allows control over Spring's Transactions
            Configlet.withTransaction{
                status ->
                try {
                        configletInstance.delete()
                        flash.message = "Configlet ${params.id} deleted"
                        redirect(action:list)
                    }
                    catch(org.springframework.dao.DataIntegrityViolationException e) {
                        // Need to roll back the transaction so associations are not deleted
                        status.setRollbackOnly()
                        Configlet.withSession { session ->
                            session.clear()
                        }
                        flash.message = "Could not delete Configlet ${params.id} due to an integrity constraint"
                    }
                    catch(org.hibernate.exception.ConstraintViolationException e){
                        // Roll back the transaction so associations are not deleted
                        status.setRollbackOnly()
                        Configlet.withSession { session ->
                            session.clear()
                        }
                        flash.message = "Could not delete Configlet ${params.id} due to an integrity constraint"
                    }
             }
        }
        else {
            flash.message = "Configlet not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def configletInstance = Configlet.get( params.id )

        if(!configletInstance) {
            flash.message = "Configlet not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ configletInstance : configletInstance ]
        }
    }

    def update = {
        def configletInstance = Configlet.get( params.id )
        if(configletInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(configletInstance.version > version) {
                    
                    configletInstance.errors.rejectValue("version", "configlet.optimistic.locking.failure", "Another user has updated this Configlet while you were editing.")
                    render(view:'edit',model:[configletInstance:configletInstance])
                    return
                }
            }
            configletInstance.properties = params
            if(!configletInstance.hasErrors() && configletInstance.save()) {
                flash.message = "Configlet ${params.id} updated"
                redirect(action:show,id:configletInstance.id)
            }
            else {
                render(view:'edit',model:[configletInstance:configletInstance])
            }
        }
        else {
            flash.message = "Configlet not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def configletInstance = new Configlet()
        configletInstance.properties = params
        return ['configletInstance':configletInstance]
    }

    def save = {
        def configletInstance = new Configlet(params)
        if(!configletInstance.hasErrors() && configletInstance.save()) {
            flash.message = "Configlet ${configletInstance.id} created"
            redirect(action:show,id:configletInstance.id)
        }
        else {
            render(view:'create',model:[configletInstance:configletInstance])
        }
    }
}
