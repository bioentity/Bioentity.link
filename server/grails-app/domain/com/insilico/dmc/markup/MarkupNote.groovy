package com.insilico.dmc.markup

import com.insilico.dmc.Note
import grails.neo4j.Relationship

class MarkupNote  implements Relationship<Markup,Note> {

    static constraints = {
    }

}
