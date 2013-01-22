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
 *  This is the controller class for the Connection domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class ConfigurationController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Configuration.list() as XML
    }

    def rest_show = {
        def configurationInstance = Configuration.get(params.id)
        if (configurationInstance) {
            response.setCharacterEncoding("utf-8")
            render configurationInstance as XML
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
        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            SendNotFoundResponse()
        }

        // Allows control over Spring's Transactions
        Configuration.withTransaction{
            status ->
            try{
                configurationInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Configuration.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Configuration ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Configuration.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Configuration ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }
    
    def rest_update = {
        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            SendNotFoundResponse()
        }

        configurationInstance.properties = params.configurationInstance

        if (configurationInstance.validate()) {
            configurationInstance.save();
            response.status = 200
            render ""
        } else {
            sendValidationFailedResponse(configurationInstance, 403)
        }
    }

    private def sendValidationFailedResponse(configurationInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                configurationInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def configurationInstance = new Configuration(params.configurationInstance)

        if(configurationInstance.save()){
            response.setCharacterEncoding("utf-8")
            render configurationInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    configurationInstance?.errors?.fieldErrors?.each {err ->
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
        [ configurationInstanceList: Configuration.list( params ), configurationInstanceTotal: Configuration.count() ]
    }

    def show = {
        def configurationInstance = Configuration.get( params.id )

        if(!configurationInstance) {
            flash.message = "Configuration not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ configurationInstance : configurationInstance ] }
    }

    def delete = {
        def configurationInstance = Configuration.get( params.id )
        if(configurationInstance) {
            // Allows control over Spring's Transactions
            Configuration.withTransaction{
                status ->
                try {
                        configurationInstance.delete()
                        flash.message = "Configuration ${params.id} deleted"
                        redirect(action:list)
                    }
                    catch(org.springframework.dao.DataIntegrityViolationException e) {
                        // Need to roll back the transaction so associations are not deleted
                        status.setRollbackOnly()
                        Configuration.withSession { session ->
                            session.clear()
                        }
                        flash.message = "Could not delete Configuration ${params.id} due to an integrity constraint"
                    }
                    catch(org.hibernate.exception.ConstraintViolationException e){
                        // Roll back the transaction so associations are not deleted
                        status.setRollbackOnly()
                        Configuration.withSession { session ->
                            session.clear()
                        }
                        flash.message = "Could not delete Configuration ${params.id} due to an integrity constraint"
                    }
             }
        }
        else {
            flash.message = "Configuration not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def configurationInstance = Configuration.get( params.id )

        if(!configurationInstance) {
            flash.message = "Configuration not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ configurationInstance : configurationInstance ]
        }
    }

    def update = {
        def configurationInstance = Configuration.get( params.id )
        if(configurationInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(configurationInstance.version > version) {
                    
                    configurationInstance.errors.rejectValue("version", "configuration.optimistic.locking.failure", "Another user has updated this Configuration while you were editing.")
                    render(view:'edit',model:[configurationInstance:configurationInstance])
                    return
                }
            }
            configurationInstance.properties = params
            if(!configurationInstance.hasErrors() && configurationInstance.save()) {
                flash.message = "Configuration ${params.id} updated"
                redirect(action:show,id:configurationInstance.id)
            }
            else {
                render(view:'edit',model:[configurationInstance:configurationInstance])
            }
        }
        else {
            flash.message = "Configuration not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def configurationInstance = new Configuration()
        configurationInstance.properties = params
        return ['configurationInstance':configurationInstance]
    }

    def save = {
        def configurationInstance = new Configuration(params)
        if(!configurationInstance.hasErrors() && configurationInstance.save()) {
            flash.message = "Configuration ${configurationInstance.id} created"
            redirect(action:show,id:configurationInstance.id)
        }
        else {
            render(view:'create',model:[configurationInstance:configurationInstance])
        }
    }
}
