package com.insilico.dmc.publication

import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.KeyWordSet
import grails.gorm.services.Service
import grails.neo4j.services.Cypher

@Service(KeyWordSet)
interface KeyWordSetDataService {

    @Cypher("MATCH (n:KeyWordSet {id:'${keyWordSet.id}'}),(p:KeyWord { id:'${keyWord.id}'}) create (n)-[r:KEYWORDS]->(p) return n")
    KeyWordSet insertKeyWordSetAssociation(KeyWord keyWord, KeyWordSet keyWordSet)
}
