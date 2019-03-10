package com.insilico.dmc.rule

import com.insilico.dmc.lexicon.LexiconSource

class RuleSet {
	String name
	LexiconSource lexiconSource

    static constraints = {
		name nullable: false, unique: true
    }

	static hasMany = [
		rules: Rule
	]
	static mapping = {
//		name index: true
	}
	
}
