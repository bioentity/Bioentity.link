package com.insilico.dmc.lexicon

/**
 * TODO: Should be LexiconSourceClassEnum
 */
class LexicaType {

    static constraints = {
        typeName nullable: false
        typeGroup nullable: true
    }

    String typeName
    LexicaTypeGroup typeGroup

    static mapping = {
//        typeName index: true
//        typeGroup index: true
    }
}
