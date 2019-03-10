package com.insilico.dmc.user

import com.insilico.dmc.publication.Publication

class Author extends User{


    static constraints = {
    }

    static hasMany = [
            publications: Publication // pubs
    ]


}

