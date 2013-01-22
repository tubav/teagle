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
 *  This is the controller class for the ConfigParamComposite domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class ConfigParamCompositeController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render ConfigParamComposite.list() as XML
    }

    def rest_show = {
        def configParamCompositeInstance = ConfigParamComposite.get(params.id)
        if (configParamCompositeInstance) {
            response.setCharacterEncoding("utf-8")
            render configParamCompositeInstance as XML
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
        ConfigParamComposite configParamCompositeInstance = ConfigParamComposite.get(params.id)
        if (!configParamCompositeInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        ConfigParamComposite.withTransaction{
            status ->
            try{
                //removeAssociations(configParamCompositeInstance)
                configParamCompositeInstance.delete(flush:true)
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()

                ConfigParamComposite.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("ConfigParamComposite ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.ObjectDeletedException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()

                ConfigParamComposite.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("ConfigParamComposite ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        ConfigParamComposite configParamCompositeInstance = ConfigParamComposite.get(params.id)
        if (!configParamCompositeInstance) {
            SendNotFoundResponse()
        }

        configParamCompositeInstance.properties = params.configParamCompositeInstance

        //Handling multiple configParam entries
        if(request.XML.configParams.size()>0 && request.XML.configParams!=""){
            configParamCompositeInstance.properties.configParams.clear()
            for(int i=0;i<request.XML.configParams.size();i++){
                configParamCompositeInstance.properties.configParams.add(ConfigParam.get(request.XML.configParams.getAt(i).toInteger()))
            }
        }

        if (configParamCompositeInstance.validate()) {
            configParamCompositeInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render configParamCompositeInstance as XML
        } else {
            sendValidationFailedResponse(configParamCompositeInstance, 403)
        }
    }

    private def sendValidationFailedResponse(configParamCompositeInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                configParamCompositeInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def configParamCompositeInstance = new ConfigParamComposite(params.configParamCompositeInstance)

        //Handling multiple configParam entries
        if(request.XML.configParams.size()>0 && request.XML.configParams!=""){
            configParamCompositeInstance.configParams.clear()
            for(int i=0;i<request.XML.configParams.size();i++){
                configParamCompositeInstance.configParams.add(i,ConfigParam.get(request.XML.configParams.getAt(i).toInteger()))
            }
        }

        if(configParamCompositeInstance.save()){
            response.setCharacterEncoding("utf-8")
            render configParamCompositeInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    configParamCompositeInstance?.errors?.fieldErrors?.each {err ->
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
        [ configParamCompositeInstanceList: ConfigParamComposite.list( params ), configParamCompositeInstanceTotal: ConfigParamComposite.count() ]
    }

    def show = {
        def configParamCompositeInstance = ConfigParamComposite.get( params.id )

        if(!configParamCompositeInstance) {
            flash.message = "ConfigParamComposite not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ configParamCompositeInstance : configParamCompositeInstance ] }
    }

    def delete = {
        def configParamCompositeInstance = ConfigParamComposite.get( params.id )
        if(configParamCompositeInstance) {
            // Allows control over Spring's Transactions
            ConfigParamComposite.withTransaction{
                status ->
                try {
                   // removeAssociations(configParamCompositeInstance)
                    configParamCompositeInstance.delete()
                    flash.message = "ConfigParamComposite ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()
                    
                    ConfigParamComposite.withSession { session ->
                        session.clear()
                    }
                    flash.message = "ConfigParamComposite ${params.id} could not be deleted"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    ConfigParamComposite.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete ConfigParamComposite ${params.id} due to an integrity constraint"
                }
            }
        }
        else {
            flash.message = "ConfigParamComposite not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def configParamCompositeInstance = ConfigParamComposite.get( params.id )

        if(!configParamCompositeInstance) {
            flash.message = "ConfigParamComposite not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ configParamCompositeInstance : configParamCompositeInstance ]
        }
    }

    def update = {
        def configParamCompositeInstance = ConfigParamComposite.get( params.id )
        if(configParamCompositeInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(configParamCompositeInstance.version > version) {

                    configParamCompositeInstance.errors.rejectValue("version", "configParamComposite.optimistic.locking.failure", "Another user has updated this ConfigParamComposite while you were editing.")
                    render(view:'edit',model:[configParamCompositeInstance:configParamCompositeInstance])
                    return
                }
            }
            configParamCompositeInstance.properties = params
            if(!configParamCompositeInstance.hasErrors() && configParamCompositeInstance.save()) {
                flash.message = "ConfigParamComposite ${params.id} updated"
                redirect(action:show,id:configParamCompositeInstance.id)
            }
            else {
                render(view:'edit',model:[configParamCompositeInstance:configParamCompositeInstance])
            }
        }
        else {
            flash.message = "ConfigParamComposite not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def configParamCompositeInstance = new ConfigParamComposite()
        configParamCompositeInstance.properties = params
        return ['configParamCompositeInstance':configParamCompositeInstance]
    }

    def save = {
        def configParamCompositeInstance = new ConfigParamComposite(params)
        if(!configParamCompositeInstance.hasErrors() && configParamCompositeInstance.save()) {
            flash.message = "ConfigParamComposite ${configParamCompositeInstance.id} created"
            redirect(action:show,id:configParamCompositeInstance.id)
        }
        else {
            render(view:'create',model:[configParamCompositeInstance:configParamCompositeInstance])
        }
    }

    def removeAssociations(configParamCompositeInstance){
            ConfigParamComposite.findAll().each {
                it.removeFromConfigParams(configParamCompositeInstance)
            }
    }
}
