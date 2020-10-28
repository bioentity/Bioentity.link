package com.insilico.dmc.markup

import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.publication.Publication

/**
 * A set of key words generated for a set of LexiconSources
 */
class KeyWordSet {

    String name
    String description
    String uuid
    boolean isHidden

    static constraints = {
        name nullable: false, unique: true
        uuid nullable: false, unique: true
    }

    static hasMany = [
            sources : LexiconSource
            ,publications: Publication
            ,keyWords : KeyWord
    ]

    static mapping = {
//        id generat or: 'snowflake'
        description type: "text"
//        name index: true
//        uuid index: true
        isHidden defaultValue: false
    }

//    String getIdString(){
//        return String.valueOf(id)
//    }
}
