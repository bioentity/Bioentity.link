package com.insilico.dmc.user

import com.insilico.dmc.publication.Publisher

class PublisherContact extends User {

    Publisher publisher

    static constraints = {
        publisher nullable: false
    }

}

