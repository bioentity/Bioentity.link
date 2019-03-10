package com.insilico.dmc

import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.KeyWordSet
import com.insilico.dmc.publication.KeyWordSetDataService
import com.insilico.dmc.publication.Publication
import grails.transaction.Transactional
import org.grails.datastore.gorm.neo4j.Neo4jDatastore
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired

class KeyWordService {

   // @Autowired
    //KeyWordSetDataService keyWordSetDataService
    //@Autowired
    //Neo4jDatastore neo4jDatastore

    List<KeyWordSet> getKetWordSets(List<LexiconSource> lexiconSources) {
        println "initial keyword set ${KeyWordSet.count}"
        List<KeyWordSet> keyWordSetList = []
        for (KeyWordSet keyWordSet in KeyWordSet.all) {
            if (overlapSources(lexiconSources, keyWordSet.sources)) {
                keyWordSetList.add(keyWordSet)
            }
        }
        return keyWordSetList
    }

    boolean overlapSources(List<LexiconSource> lexiconSources, Set<LexiconSource> lexiconSourcesB) {
        for (LexiconSource lexiconSource in lexiconSources) {
            if (lexiconSourcesB.contains(lexiconSource)) {
                return true
            }
        }
        return false
    }

    @Transactional
    void removeKeyWords() {
        KeyWordSet.deleteAll(KeyWordSet.all)
        KeyWord.deleteAll(KeyWord.all)
    }

    @Transactional
    void insertKeyWords(List listOfKeywordSets) {

        Map<String, Object> params = new HashMap<>()

        params.put("props", listOfKeywordSets)
        println("listofkeywordsets size" + listOfKeywordSets.size())

        println "Loading keywords into Neo4j"

        KeyWordSet.executeUpdate(" UNWIND {props} as row    MATCH (ls:LexiconSource {uuid:row.lexiconSourceUUID}) MATCH (lexicon:Lexicon {uuid:row.lexiconUUID}) MERGE (keywordSet:KeyWordSet {uuid:row.keywordSetUUID,name:row.keywordSetName,version:0}) MERGE (keywordSet)-[kwsls:SOURCES]->(ls) MERGE (keyword:KeyWord {uuid:row.keywordUUID, version: 0}) SET keyword.value = row.keywordValue MERGE (keywordSet)-[kwkr:KEYWORDS]->(keyword) MERGE (keyword)-[kwl:KEYWORDS]->(lexicon) ", params)

//        session.close()

        println "session closed."
    }

    Map<String, JSONArray> findMarkups(List<String> keyWordUuids) {

//        println(keyWordUuids)
        String query = "MATCH (k:KeyWord)--(l:Lexicon),(k:KeyWord)<--(q:KeyWordSet)-->(s:LexiconSource), (k:KeyWord)--(m:Markup)--(p:Publication) where k.uuid in  ({uuids})  return { keyWord: k, markupCount: count(m), publication:p}"
//        println(query)

        def nodeList = KeyWord.executeQuery(query, [uuids: keyWordUuids])
        println nodeList
        Map<String, JSONArray> markupMap = new HashMap<>()
        nodeList.each {
            JSONObject jsonObject = new JSONObject()
            println it
            KeyWord keyWord = (it.keyWord as KeyWord)
            JSONArray thisMap = markupMap.get(keyWord.uuid) ?: new JSONArray()

            jsonObject.keyWord = new JSONObject()
            jsonObject.keyWord.id = keyWord.id
            jsonObject.keyWord.value = keyWord.value
            jsonObject.keyWord.uuid = keyWord.uuid
            Publication publication = (it.publication as Publication)
            jsonObject.publication = new JSONObject()
            jsonObject.publication.id = publication.id
            jsonObject.publication.title = publication.title
            jsonObject.publication.fileName = publication.fileName
            jsonObject.publication.journal = publication.journal
            jsonObject.publication.doi = publication.doi
            jsonObject.markupCount = it.markupCount

            thisMap.add(jsonObject)
            markupMap.put(keyWord.uuid, thisMap)

            markupMap.put(keyWord.uuid, thisMap)

        }
        return markupMap
    }
}
