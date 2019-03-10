package com.insilico.dmc.organization

class Organization {

    String name

    static constraints = {
        name nullable: false, unique: true
    }
    static mapping = {
//        name index: true
    }
}
