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
 *  This is the controller class for the Keyword domain.
 *  It manages the rest crud methods and the grails crud methods.
 */

import grails.converters.*

class KeywordController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Keyword.list() as XML
    }

    def rest_show = {
        def keywordInstance = Keyword.get(params.id)
        if (keywordInstance) {
            response.setCharacterEncoding("utf-8")
            render keywordInstance as XML
        } else {
            SendNotFoundResponse()
        }
        
    }

    def rest_save = {
        def keywordInstance = new Keyword(params.keywordInstance)

        if(keywordInstance.save()){
            response.setCharacterEncoding("utf-8")
            render keywordInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    keywordInstance?.errors?.fieldErrors?.each {err ->
                        field(err.field)
                        message(g.message(error: err))
                    }
                }
            }
        }
    }
   
    def rest_update = {
        Keyword keywordInstance = Keyword.get(params.id)
        if (!keywordInstance) {
            SendNotFoundResponse()
        }

        keywordInstance.properties = params.keywordInstance

        if (keywordInstance.validate()) {
            keywordInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render keywordInstance as XML
        } else {
            sendValidationFailedResponse(keywordInstance, 403)
        }
    }

    def rest_delete = {
        Keyword keywordInstance = Keyword.get(params.id)
        if (!keywordInstance) {
            SendNotFoundResponse()
        }
        Keyword.withTransaction{
            status ->
            try{
                keywordInstance.delete(flush:true);
                response.status = 204
                render ""
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                status.setRollbackOnly()
                Keyword.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Keyword ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
            }
            catch(org.hibernate.exception.ConstraintViolationException e){
                // Need to roll back the transaction so associations are not deleted
                status.setRollbackOnly()
                Keyword.withSession { session ->
                    session.clear()
                }
                response.status = 405
                render contentType: "text/xml", {
                    errors {
                        message("Keyword ${params.id} could not be deleted due to a foreign key constraint.")
                    }
                }
          }
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
    	//println "listing"
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
	//println "max: " + params.max
	//println "params: " + params
	//println "count: " + Keyword.count()
	//println "list: " + Keyword.list( params )
        return [ keywordInstanceList: Keyword.list( params ), keywordInstanceTotal: Keyword.count() ]
    }

    def show = {
        def keywordInstance = Keyword.get( params.id )

        if(!keywordInstance) {
            flash.message = "Keyword not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ keywordInstance : keywordInstance ] }
    }

    def delete = {
        def keywordInstance = Keyword.get( params.id )
        if(keywordInstance) {
            try {
                keywordInstance.delete(flush:true)
                flash.message = "Keyword ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "Keyword ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "Keyword not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def keywordInstance = Keyword.get( params.id )

        if(!keywordInstance) {
            flash.message = "Keyword not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ keywordInstance : keywordInstance ]
        }
    }

    def update = {
        def keywordInstance = Keyword.get( params.id )
        if(keywordInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(keywordInstance.version > version) {
                    
                    keywordInstance.errors.rejectValue("version", "keyword.optimistic.locking.failure", "Another user has updated this Keyword while you were editing.")
                    render(view:'edit',model:[keywordInstance:keywordInstance])
                    return
                }
            }
            keywordInstance.properties = params
            if(!keywordInstance.hasErrors() && keywordInstance.save()) {
                flash.message = "Keyword ${params.id} updated"
                redirect(action:show,id:keywordInstance.id)
            }
            else {
                render(view:'edit',model:[keywordInstance:keywordInstance])
            }
        }
        else {
            flash.message = "Keyword not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def keywordInstance = new Keyword()
        keywordInstance.properties = params
        return ['keywordInstance':keywordInstance]
    }

    def save = {
        def keywordInstance = new Keyword(params)
        if(!keywordInstance.hasErrors() && keywordInstance.save()) {
            flash.message = "Keyword ${keywordInstance.id} created"
            redirect(action:show,id:keywordInstance.id)
        }
        else {
            render(view:'create',model:[keywordInstance:keywordInstance])
        }
    }
}
