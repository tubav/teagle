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
 *  This is the controller class for the Vct domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class VctController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Vct.list() as XML
    }

    def rest_show = {
        def vctInstance = Vct.get(params.id)
        if (vctInstance) {
            response.setCharacterEncoding("utf-8")
            render vctInstance as XML
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
        Vct vctInstance = Vct.get(params.id)
        if (!vctInstance) {
            SendNotFoundResponse()
        }
        // Allows control over Spring's Transactions
        Vct.withTransaction{
            status ->
            try{
                vctInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Vct.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Vct ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Vct.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Vct ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
        }
    }

    def rest_update = {
        def person = Person.get(params.vctInstance.user)
        params.vctInstance.user = person

        Vct vctInstance = Vct.get(params.id)
        if (!vctInstance) {
            SendNotFoundResponse()
        }

        vctInstance.properties = params.vctInstance

        if(request.XML.hasConnections!=""){
            vctInstance.hasConnections = null
            // Iterate through multiple elements and add to the instance
            request.XML.hasConnections.each{
                def connection = Connection.get(it.toString())
                if (!connection) {
                    SendNotFoundResponse()
                }else{
                    vctInstance.addToHasConnections(connection)
                }
            }
        }

        if(request.XML.providesResources!=""){
            vctInstance.providesResources = null
            // Iterate through multiple elements and add to the instance
            request.XML.providesResources.each{
                def resource = ResourceInstance.get(it.toString())
                if (!resource) {
                    SendNotFoundResponse()
                }else{
                    vctInstance.addToProvidesResources(resource)
                }
            }
        }

        if (vctInstance.validate()) {
            vctInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render vctInstance as XML
        } else {
            sendValidationFailedResponse(vctInstance, 403)
        }
    }

    private def sendValidationFailedResponse(vctInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                vctInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def person = Person.get(params.vctInstance.user)

        params.vctInstance.user = person

        def vctInstance = new Vct(params.vctInstance)

        // Iterate through multiple elements and add to the instance
        if(request.XML.hasConnections!=""){
            request.XML.hasConnections.each{
                def connection = Connection.get(it.toString())
                if (!connection) {
                    SendNotFoundResponse()
                }else{
                    vctInstance.addToHasConnections(connection)
                }
            }
        }

        if(request.XML.providesResources!=""){
            request.XML.providesResources.each{
                def resource = ResourceInstance.get(it.toString())
                if (!resource) {
                    SendNotFoundResponse()
                }else{
                    vctInstance.addToProvidesResources(resource)
                }
            }
        }

        if(vctInstance.save()){
            response.setCharacterEncoding("utf-8")
            render vctInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    vctInstance?.errors?.fieldErrors?.each {err ->
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
        [ vctInstanceList: Vct.list( params ), vctInstanceTotal: Vct.count() ]
    }

    def show = {
        def vctInstance = Vct.get( params.id )

        if(!vctInstance) {
            flash.message = "Vct not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ vctInstance : vctInstance ] }
    }

    def delete = {
        def vctInstance = Vct.get( params.id )
        if(vctInstance) {
            // Allows control over Spring's Transactions
            Vct.withTransaction{
                status ->
                try{
                    vctInstance.delete()
                    flash.message = "Vct ${params.id} deleted"
                    redirect(action:list)
                }
                catch(org.springframework.dao.DataIntegrityViolationException e) {
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Vct.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Vct ${params.id} due to an integrity constraint"
                }
                catch(org.hibernate.exception.ConstraintViolationException e){
                    // Need to roll back the transaction so associations are not deleted
                    status.setRollbackOnly()

                    Vct.withSession { session ->
                        session.clear()
                    }
                    flash.message = "Could not delete Vct ${params.id} due to an integrity constraint"
                }
            }          
        }
        else {
            flash.message = "Vct not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def vctInstance = Vct.get( params.id )

        if(!vctInstance) {
            flash.message = "Vct not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ vctInstance : vctInstance ]
        }
    }

    def update = {
        def vctInstance = Vct.get( params.id )
        if(vctInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(vctInstance.version > version) {

                    vctInstance.errors.rejectValue("version", "vct.optimistic.locking.failure", "Another user has updated this Vct while you were editing.")
                    render(view:'edit',model:[vctInstance:vctInstance])
                    return
                }
            }
            vctInstance.providesResources.clear()
            vctInstance.hasConnections.clear()
            vctInstance.properties = params
            if(!vctInstance.hasErrors() && vctInstance.save()) {
                flash.message = "Vct ${params.id} updated"
                redirect(action:show,id:vctInstance.id)
            }
            else {
                render(view:'edit',model:[vctInstance:vctInstance])
            }
        }
        else {
            flash.message = "Vct not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def vctInstance = new Vct()
        vctInstance.properties = params
        return ['vctInstance':vctInstance]
    }

    def save = {
        def vctInstance = new Vct(params)
        if(!vctInstance.hasErrors() && vctInstance.save()) {
            flash.message = "Vct ${vctInstance.id} created"
            redirect(action:show,id:vctInstance.id)
        }
        else {
            render(view:'create',model:[vctInstance:vctInstance])
        }
    }

}
