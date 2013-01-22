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
 *  This is the controller class for the ConfigParamAtomic domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class ConfigParamAtomicController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render ConfigParamAtomic.list() as XML
        
    }

    def rest_show = {
        def configParamAtomicInstance = ConfigParamAtomic.get(params.id)
        if (configParamAtomicInstance) {
            response.setCharacterEncoding("utf-8")
            render configParamAtomicInstance as XML
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
        ConfigParamAtomic configParamAtomicInstance = ConfigParamAtomic.get(params.id)
        if (!configParamAtomicInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        ConfigParamAtomic.withTransaction{
            status ->
            try{
                configParamAtomicInstance.delete(flush:true)
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()

                ConfigParamAtomic.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("ConfigParamAtomic ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.ObjectDeletedException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()

                ConfigParamAtomic.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("ConfigParamAtomic ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        ConfigParamAtomic configParamAtomicInstance = ConfigParamAtomic.get(params.id)
        if (!configParamAtomicInstance) {
            SendNotFoundResponse()
        }

        configParamAtomicInstance.properties = params.configParamAtomicInstance

        if (configParamAtomicInstance.validate()) {
            configParamAtomicInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render configParamAtomicInstance as XML
        } else {
            sendValidationFailedResponse(configParamAtomicInstance, 403)
        }
    }

    private def sendValidationFailedResponse(configParamAtomicInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                configParamAtomicInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def configParamAtomicInstance = new ConfigParamAtomic(params.configParamAtomicInstance)
        if(configParamAtomicInstance.save()){
            response.setCharacterEncoding("utf-8")
            render configParamAtomicInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    configParamAtomicInstance?.errors?.fieldErrors?.each {err ->
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
        [ configParamAtomicInstanceList: ConfigParamAtomic.list( params ), configParamAtomicInstanceTotal: ConfigParamAtomic.count() ]
    }

    def show = {
        def configParamAtomicInstance = ConfigParamAtomic.get( params.id )
        if(!configParamAtomicInstance) {
            flash.message = "ConfigParamAtomic not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ configParamAtomicInstance : configParamAtomicInstance ] }
    }

    def delete = {
        def configParamAtomicInstance = ConfigParamAtomic.get( params.id )
        if(configParamAtomicInstance) {
            // Allows control over Spring's Transactions
            ConfigParamComposite.withTransaction{
                status ->
                try{
                    configParamAtomicInstance.delete()
                    flash.message = "ConfigParamAtomic ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    ConfigParamAtomic.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete ConfigParamAtomic ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    ConfigParamAtomic.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete ConfigParamAtomic ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "ConfigParamAtomic not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def configParamAtomicInstance = ConfigParamAtomic.get( params.id )

        if(!configParamAtomicInstance) {
            flash.message = "ConfigParamAtomic not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ configParamAtomicInstance : configParamAtomicInstance ]
        }
    }

    def update = {
        def configParamAtomicInstance = ConfigParamAtomic.get( params.id )
        if(configParamAtomicInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(configParamAtomicInstance.version > version) {

                    configParamAtomicInstance.errors.rejectValue("version", "configParamAtomic.optimistic.locking.failure", "Another user has updated this ConfigParamAtomic while you were editing.")
                    render(view:'edit',model:[configParamAtomicInstance:configParamAtomicInstance])
                    return
                }
            }
            configParamAtomicInstance.properties = params
            if(!configParamAtomicInstance.hasErrors() && configParamAtomicInstance.save()) {
                flash.message = "ConfigParamAtomic ${params.id} updated"
                redirect(action:show,id:configParamAtomicInstance.id)
            }
            else {
                render(view:'edit',model:[configParamAtomicInstance:configParamAtomicInstance])
            }
        }
        else {
            flash.message = "ConfigParamAtomic not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def configParamAtomicInstance = new ConfigParamAtomic()
        configParamAtomicInstance.properties = params
        return ['configParamAtomicInstance':configParamAtomicInstance]
    }

    def save = {
        def configParamAtomicInstance = new ConfigParamAtomic(params)
        if(!configParamAtomicInstance.hasErrors() && configParamAtomicInstance.save()) {
            flash.message = "ConfigParamAtomic ${configParamAtomicInstance.id} created"
            redirect(action:show,id:configParamAtomicInstance.id)
        }
        else {
            render(view:'create',model:[configParamAtomicInstance:configParamAtomicInstance])
        }
    }

}
