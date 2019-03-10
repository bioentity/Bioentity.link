package com.insilico.dmc.lexicon

import com.insilico.dmc.Species
import com.insilico.dmc.lexicon.LexiconSourceClassEnum

class LexiconSource {

    String source
    String prefix
    LexiconSourceClassEnum className
    URL urlConstructor
    Species species
    String file
	String notes

	String url
	String protocol
	String username
	String password
    String uuid
	int timer


    static constraints = {
        source nullable: false
        className nullable: false
        urlConstructor nullable: false
        species nullable: true
        prefix nullable: true
        file nullable: true
        uuid nullable: false, unique: true
    }

    static mapping = {
        source defaultValue: ''
        className defaultValue: ''
        urlConstructor type: 'url'
//        source index: true
    }

    static hasMany = [
            lexica: Lexicon
    ]
}
