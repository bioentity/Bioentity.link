package com.insilico.dmc

import com.insilico.dmc.user.User

class Event {

    String uuid
    User curator
    String description

    static constraints = {
        uuid nullable: true
        curator nullable: true
        description nullable: true
    }

    static mapping = {
//        uuid index: true
    }
}
