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
 * author: Shane Fox
 *
 * </copyright>
 *
 */
import java.util.Date

class Booking{

    static belongsTo = Vct

    /**
     *  The time period for the booking
     */
    //TimePeriod booking

    Date startDate
    Date endDate
    /**
     *  Indicates whether the vct has been rated
     */
    boolean rated

    Vct vct

    static constraints = {
    }
}
