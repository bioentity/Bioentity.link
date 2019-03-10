package com.insilico.dmc.rule

class Rule {

	String name
	String italic
	String regEx

	static belongsTo = [RuleSet]

    static constraints = {
		name nullable: false
    }
	static mapping = {
//		name index: true
	}
}
