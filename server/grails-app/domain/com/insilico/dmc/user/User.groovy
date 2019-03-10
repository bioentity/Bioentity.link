package com.insilico.dmc.user

import com.insilico.dmc.markup.MarkupEvent
import com.insilico.dmc.publication.CurationActivity
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.OAuthID

class User {

    String username
    String firstName
    String lastName
    String email
    Boolean active
    Role defaultRole

    String phoneNumber
    // String physicalAddress // ?

    static constraints = {
        username nullable: false
        firstName nullable: false
        lastName nullable: false
        email blank: false, nullable: true
        phoneNumber nullable: true, blank: false, unique: false
        defaultRole nullable: true
    }

    static hasMany = [
            markupEvents: MarkupEvent
            ,publications: Publication // pubs
            ,roles: Role
			,oAuthIDs: OAuthID
            ,curators: CurationActivity
    ]
    static mapping = {
//        username index: true
    }
}

