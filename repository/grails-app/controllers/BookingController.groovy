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
 */

import grails.converters.*

class BookingController {

    def rest_index = { redirect(action:list,params:params) }

    def rest_list = {
        response.setCharacterEncoding("utf-8")
        render Booking.list() as XML
    }

    def rest_show = {
        def bookingInstance = Booking.get(params.id)
        if (bookingInstance) {
            response.setCharacterEncoding("utf-8")
            render bookingInstance as XML
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
        Booking bookingInstance = Booking.get(params.id)
        if (!bookingInstance) {
            SendNotFoundResponse()
        }
        bookingInstance.delete();
        response.status = 204
        render ""
    }

    def rest_update = {
        Booking bookingInstance = Booking.get(params.id)
        if (!bookingInstance) {
            SendNotFoundResponse()
        }

        bookingInstance.properties = params.bookingInstance

        if (bookingInstance.validate()) {
            bookingInstance.save();
            response.status = 200
            response.setCharacterEncoding("utf-8")
            render bookingInstance as XML
        } else {
            sendValidationFailedResponse(bookingInstance, 403)
        }
    }

    private def sendValidationFailedResponse(bookingInstance, status) {
        response.status = status
        render contentType: "application/xml", {
            errors {
                bookingInstance?.errors?.fieldErrors?.each {err ->
                    field(err.field)
                    message(g.message(error: err))
                }
            }
        }
    }

    def rest_save = {
        def bookingInstance = new Booking(params.bookingInstance)

        if(bookingInstance.save()){
            response.setCharacterEncoding("utf-8")
            render bookingInstance as XML
        }else{
            response.status = 406
            render contentType: "text/xml", {
                errors {
                    bookingInstance?.errors?.fieldErrors?.each {err ->
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
        [ bookingInstanceList: Booking.list( params ), bookingInstanceTotal: Booking.count() ]
    }

    def show = {
        def bookingInstance = Booking.get( params.id )

        if(!bookingInstance) {
            flash.message = "Booking not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ bookingInstance : bookingInstance ] }
    }

    def delete = {
        def bookingInstance = Booking.get( params.id )
        if(bookingInstance) {
            try {
                bookingInstance.delete(flush:true)
                flash.message = "Booking ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "Booking ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "Booking not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def bookingInstance = Booking.get( params.id )

        if(!bookingInstance) {
            flash.message = "Booking not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ bookingInstance : bookingInstance ]
        }
    }

    def update = {
        def bookingInstance = Booking.get( params.id )
        if(bookingInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(bookingInstance.version > version) {
                    
                    bookingInstance.errors.rejectValue("version", "booking.optimistic.locking.failure", "Another user has updated this Booking while you were editing.")
                    render(view:'edit',model:[bookingInstance:bookingInstance])
                    return
                }
            }
            bookingInstance.properties = params
            if(!bookingInstance.hasErrors() && bookingInstance.save()) {
                flash.message = "Booking ${params.id} updated"
                redirect(action:show,id:bookingInstance.id)
            }
            else {
                render(view:'edit',model:[bookingInstance:bookingInstance])
            }
        }
        else {
            flash.message = "Booking not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def create = {
        def bookingInstance = new Booking()
        bookingInstance.properties = params
        return ['bookingInstance':bookingInstance]
    }

    def save = {
        def bookingInstance = new Booking(params)
        if(!bookingInstance.hasErrors() && bookingInstance.save()) {
            flash.message = "Booking ${bookingInstance.id} created"
            redirect(action:show,id:bookingInstance.id)
        }
        else {
            render(view:'create',model:[bookingInstance:bookingInstance])
        }
    }
}
