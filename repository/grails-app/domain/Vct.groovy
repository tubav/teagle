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
 *  A Vct is composed of the testing resources and the interconnections between these resources.
 *  It is representive of the Product being sold.
 */

class Vct extends Product {

    static belongsTo = Person
    /**
     *  An associated user.
     */
    Person user
    /**
     *  Whether the VCT is public or private
     */
    boolean shared

    /**
     *  The current state of the vct, e.g. new, provisioned or unprovisioned, booked, unbooked, inprogress_wait, inprogress_sync, inprogress_async.
     */
    VctState state

    static hasMany = [hasBookings:Booking]

    static constraints = {
        hasBookings(nullable:true)
    }
}
