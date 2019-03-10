package com.insilico.dmc.markup

import com.insilico.dmc.Event
import grails.neo4j.Relationship

class MarkupEvent implements Relationship<Markup,Event>{

    String uuid
    MarkupEventEnum eventType

    static constraints = {
        uuid unique: true
        eventType nullable: true// for now, should be false later
    }

    static mapping = {
//        uuid index: true
    }
}
