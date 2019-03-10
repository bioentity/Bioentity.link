package com.insilico.dmc.index

import com.insilico.dmc.publication.Content

class ContentWordIndex {

    String word

    static constraints = {
    }

    static mapping = {
        word index:true, unique: true

    }

    static belongsTo = Content

    static hasMany = [
           contents: Content
    ]
}
