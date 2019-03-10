package com.insilico.dmc

import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import grails.converters.JSON
import grails.transaction.Transactional
import org.grails.web.json.JSONObject

@Transactional
class LexiconService {


    def updateLexicon(Lexicon lexicon) {

        lexicon.uuid = lexicon.uuid ?: UUID.randomUUID().toString()

        lexicon.save failOnError: true
    }

    def updateLexiconSource(LexiconSource lexSource) {

//        Species species = lexSource.species
//
//        JSONObject speciesObject = new JSONObject()
//        speciesObject.name = species.name
//        speciesObject.taxonId = species.taxonId
//        Species newSpecies = Species.findByTaxonId(species.taxonId)
//        // should not re-create one this way
//        lexSource.species = newSpecies
//
//        println "species object ${speciesObject as JSON}"

//        println "updatin lexicon source ${lexSource as JSON}"
//        lexSource.save flush: true
        if (lexSource.timer != null && lexSource.url != null) {
            println "create new lexicon job"
            IngestSourceJob.schedule(60000 * lexSource.timer, -1, [sourceType: "lexicon", sourceId: lexSource.id])
        }

        LexiconSource.executeUpdate("match (l:Lexicon)-[r]-(ls:LexiconSource) where ls.uuid = {uuid} set l.link = null  return l limit 10"
        ,[uuid:lexSource.uuid])

        return lexSource
    }
}
