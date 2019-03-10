package com.insilico.dmc.publication

import com.insilico.dmc.user.PublisherContact

class Publisher {

    String name
    Boolean active

    static constraints = {
        name nullable: false,unique: true
        active nullable: false
    }

    static hasMany = [
            journals: Journal
            ,contacts: PublisherContact
    ]
    static mapping = {
//        name index: true
    }
}
