package com.insilico.dmc


import grails.rest.*
import grails.converters.*

import com.insilico.dmc.rule.RuleSet
import com.insilico.dmc.rule.Rule

class RuleSetController extends RestfulController {
    static responseFormats = ['json', 'xml']
    RuleSetController() {
        super(RuleSet)
    }

	def index() {
    	respond RuleSet.list(), model:[ruleSetCount: RuleSet.count()]
	}

	def getRules(RuleSet ruleSet) {
		Rule [] rules = ruleSet.rules
		respond rules, model:[ruleCount: rules.count]
	}
}
