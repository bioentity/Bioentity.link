package com.insilico.dmc.markup

import com.insilico.dmc.lexicon.Lexicon
import groovy.transform.EqualsAndHashCode


/**
 * This is used in place of a hashmap for doing lookups.
 *
 *
 * Created by nathandunn on 4/27/17.
 */
@EqualsAndHashCode(includes = ["uuid"])
class KeyWord {

    String value
    KeyWordSet keyWordSet
    String uuid
    Integer markupCount

    static belongsTo = [KeyWordSet,Lexicon]

    static hasMany = [
            lexica: Lexicon,
            markups: Markup
    ]

    static constraints = {
        markupCount nullable: true
        value nullable: true
        uuid nullable: false, unique: true
    }

    static mapping = {
        value type: "text"
//        uuid index: true
//        value index: true
    }
}
