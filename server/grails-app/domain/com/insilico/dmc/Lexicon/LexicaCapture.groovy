package com.insilico.dmc.lexicon

class LexicaCapture {

    String fileName
    LexiconSource source

    static hasMany = [
            lexica: Lexicon
    ]

    static constraints = {
    }
    static mapping = {
//        data type: "text"
//        fileName index: true
    }
}
