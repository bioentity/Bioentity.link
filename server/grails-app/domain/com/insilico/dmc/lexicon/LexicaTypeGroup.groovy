package com.insilico.dmc.lexicon

class LexicaTypeGroup {

    String groupName

    static constraints = {
        groupName nullable: false,unique: true
    }

    static hasMany = [
            lexicaTypes: LexicaType
    ]
    static mapping = {
//        groupName index: true
    }

}
