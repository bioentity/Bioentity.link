package com.insilico.dmc.publication

import com.insilico.dmc.user.JournalContact

class Journal {

    static constraints = {
        name nullable: false, unique: true
        publisher nullable: true
        active nullable: false
    }

    Boolean active
    String name
    Publisher publisher

    static hasMany = [
            contacts: JournalContact
    ]
    static mapping = {
//        name index: true
    }
}
