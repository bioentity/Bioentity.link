package com.insilico.dmc.publication

import com.insilico.dmc.index.ContentWordIndex


/**
 * Created by nathandunn on 4/27/17.
 */
class Content {

    String value
    Publication publication

    static constraints = {
//        value nullable: false
    }

    static mapping = {
        value type: "text"
    }

    static hasMany = [
            indices: ContentWordIndex
    ]
}
