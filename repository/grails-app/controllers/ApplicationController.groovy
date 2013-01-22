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
 *  This is the controller class for the Application domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class ApplicationController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Application.list() as XML
    }

    def rest_show = {
        def applicationInstance = Application.get(params.id)
        if (applicationInstance) {
            response.setCharacterEncoding("utf-8")
            render applicationInstance as XML
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
        Application applicationInstance = Application.get(params.id)
        if (!applicationInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        Application.withTransaction{
            status ->
            try{
                applicationInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Application.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Application ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Application.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Application ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        Organisation organisationInstance = Organisation.get(params.applicationInstance.provider)
        if (!organisationInstance) {
            SendNotFoundResponse()
        }
        params.applicationInstance.provider = organisationInstance

        Person personInstance = Person.get(params.applicationInstance.owner)
        if (!personInstance) {
            SendNotFoundResponse()
        }
        params.applicationInstance.owner = personInstance
        
        Application applicationInstance = Application.get(params.id)
        if (!applicationInstance) {
            SendNotFoundResponse()
        }

        applicationInstance.properties = params.applicationInstance

        if (applicationInstance.validate()) {
            applicationInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render applicationInstance as XML
        } else {
            sendValidationFailedResponse(applicationInstance, 403)
        }
    }

    private def sendValidationFailedResponse(applicationInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                applicationInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        Organisation organisationInstance = Organisation.get(params.applicationInstance.provider)
        if (!organisationInstance) {
            SendNotFoundResponse()
        }
        params.applicationInstance.provider = organisationInstance

        Person personInstance = Person.get(params.applicationInstance.owner)
        if (!personInstance) {
            SendNotFoundResponse()
        }
        params.applicationInstance.owner = personInstance

        def applicationInstance = new Application(params.applicationInstance)

        if(applicationInstance.save()){
            response.setCharacterEncoding("utf-8")
            render applicationInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    applicationInstance?.errors?.fieldErrors?.each {err ->
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
        [ applicationInstanceList: Application.list( params ), applicationInstanceTotal: Application.count() ]
    }

    def show = {
        def applicationInstance = Application.get( params.id )

        if(!applicationInstance) {
            flash.message = "Application not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ applicationInstance : applicationInstance ] }
    }

    def delete = {
        def applicationInstance = Application.get( params.id )
        if(applicationInstance) {
            // Allows control over Spring's Transactions
            Application.withTransaction{
                status ->
                try{
                    applicationInstance.delete()
                    flash.message = "Application ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Application.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Application ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Application.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Application ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "Application not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def applicationInstance = Application.get( params.id )

        if(!applicationInstance) {
            flash.message = "Application not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ applicationInstance : applicationInstance ]
        }
    }

    def update = {
        def applicationInstance = Application.get( params.id )
        if(applicationInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(applicationInstance.version > version) {
                    
                    applicationInstance.errors.rejectValue("version", "application.optimistic.locking.failure", "Another user has updated this Application while you were editing.")
                    render(view:'edit',model:[applicationInstance:applicationInstance])
                    return
                }
            }
            applicationInstance.properties = params
            if(!applicationInstance.hasErrors() && applicationInstance.save()) {
                flash.message = "Application ${params.id} updated"
                redirect(action:show,id:applicationInstance.id)
            }
            else {
                render(view:'edit',model:[applicationInstance:applicationInstance])
            }
        }
        else {
            flash.message = "Application not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def applicationInstance = new Application()
        applicationInstance.properties = params
        return ['applicationInstance':applicationInstance]
    }

    def save = {
        def applicationInstance = new Application(params)
        if(!applicationInstance.hasErrors() && applicationInstance.save()) {
            flash.message = "Application ${applicationInstance.id} created"
            redirect(action:show,id:applicationInstance.id)
        }
        else {
            render(view:'create',model:[applicationInstance:applicationInstance])
        }
    }
}
