package com.insilico.dmc


import grails.rest.*
import grails.converters.*
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

class SpeciesController extends RestfulController {
    static responseFormats = ['json', 'xml']
    SpeciesController() {
        super(Species)
    }

    def index(){
        def species = Species.listOrderByName()
        JSONArray returnArray = new JSONArray()

        species.each {
            JSONObject object = new JSONObject(
                   name: it.name,
                    taxonId: it.taxonId
            )
            returnArray.add(object)
        }

        render returnArray as JSON
    }

    def viewAll(){
        def species = Species.listOrderByName()

        // lexicon count
        // MATCH (s:Species)--(ls:LexiconSource)--(l:Lexicon) RETURN s,ls.className,ls.source,count(l) LIMIT 25

        // keyword count
        // MATCH (s:Species)--(ls:LexiconSource)--(l:Lexicon)--(k:KeyWord) RETURN s,count(k) LIMIT 25

        // get the keyword set
//        MATCH (s:Species)--(ls:LexiconSource)--(l:Lexicon)--(k:KeyWord)--(kws:KeyWordSet) RETURN s,kws LIMIT 25

        // find all publicaitons
//        MATCH (s:Species)--(ls:LexiconSource)--(l:Lexicon)--(k:KeyWord)--(m:Markup)--(p:Publication) RETURN s,p

//        JSONArray allSpecies = new JSONArray()
//        for(animal in species){
//
//        }

        render species as JSON
    }
}
