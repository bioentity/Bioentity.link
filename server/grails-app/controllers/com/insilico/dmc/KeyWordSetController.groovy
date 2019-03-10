package com.insilico.dmc

import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.KeyWordSet
import com.insilico.dmc.publication.Publication
import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import org.apache.commons.collections.BidiMap
import org.apache.commons.collections.bidimap.DualHashBidiMap
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session


class KeyWordSetController extends RestfulController<KeyWordSet> {
    static responseFormats = ['json', 'xml']

    def scheduledJobService


    KeyWordSetController() {
        super(KeyWordSet)
    }

    def keyWordService

    /**
     * This is the uuid
     * @param id
     * @return
     */
    def all(String id) {
        def query = "MATCH (k:KeyWord)--(kws:KeyWordSet) where kws.uuid={uuid} RETURN k.value order by k.value"
        def paramValues = [uuid: id]
        List<String> keywords = KeyWord.executeQuery(query, paramValues)
        render(text: keywords.join("\n"))
    }

    def synonym(String id) {
        def query = "MATCH (l:Lexicon)--(k:KeyWord)--(kws:KeyWordSet) where kws.uuid= {uuid} and k.value <> l.publicName RETURN k.value order by k.value"
        def paramValues = [uuid: id]
        List<String> keywords = KeyWord.executeQuery(query, paramValues)
        render(text: keywords.join("\n"))
    }

    def primary(String id) {
        def query = "MATCH (l:Lexicon)--(k:KeyWord)--(kws:KeyWordSet) where kws.uuid= {uuid} and k.value = l.publicName RETURN k.value order by k.value"
        def paramValues = [uuid: id]
        List<String> keywords = KeyWord.executeQuery(query, paramValues)
        render(text: keywords.join("\n"))
    }

    def findMarkups(String id) {
        String query = "MATCH (k:KeyWord)--(l:Lexicon),(k:KeyWord)<--(q:KeyWordSet)-->(s:LexiconSource), (k:KeyWord)--(m:Markup)--(p:Publication) where k.uuid = {uuid}  return { keyWord: k, markupCount: count(m), publication:p}"
        def nodeList = KeyWord.executeQuery(query, [uuid: id])
        println nodeList
        JSONArray returnArray = new JSONArray()
        nodeList.each {
            JSONObject jsonObject = new JSONObject()
            KeyWord keyWord = (it.keyWord as KeyWord)
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
            returnArray.add(jsonObject)
        }
        render returnArray as JSON
    }

    def show(KeyWordSet keyWordSet) {
        def returnArray = new JSONArray()
        println "keywordset ${keyWordSet}"
        Map<String, KeyWord> keyWords = [:]
        def filter = params.filter ?: "All"
        println "filter: ${filter}"
        def query

        String returnNoLinks = " RETURN n,p,0 as c order by n.value "
        String returnLinks = "  RETURN n,p,count(m) as c order by n.value "
        String returnCount = "  RETURN count(n) "
        if (filter != "Linked") {
            query = "MATCH (n:KeyWord)--(p:Lexicon),(n:KeyWord)<--(q:KeyWordSet)-->(s:LexiconSource) where q.name = {kwsName} "
        } else {
            query = "MATCH (n:KeyWord)--(p:Lexicon),(n:KeyWord)<--(q:KeyWordSet)-->(s:LexiconSource),(n:KeyWord)--(m:Markup) where q.name = {kwsName}  "
        }
        def queryValues = [kwsName: keyWordSet.name]
        if (params.search && params.search.trim().length() >= 1) {
            println "processing search parameter?"
            if (filter != "Linked") {
                query = "MATCH (n:KeyWord)--(p:Lexicon),(n:KeyWord)<--(q:KeyWordSet)-->(s:LexiconSource) where q.name = {kwsName} and lower(n.value) contains lower({search}) "
            } else {
                query = "MATCH (n:KeyWord)--(p:Lexicon),(n:KeyWord)<--(q:KeyWordSet)-->(s:LexiconSource),(n:KeyWord)--(m:Markup) where q.name = {kwsName} and lower(n.value) contains lower({search}) "
            }
            queryValues.put("search", params.search)
        }
        Integer count = KeyWord.executeQuery(query + returnCount, queryValues)[0] as Integer
        query += (filter != "Linked") ? returnNoLinks : returnLinks
        if (params.max && params.offset) {
            query += " SKIP " + params.offset + " LIMIT " + params.max
        }

        println "final query: ${query}"
        List<Node> nodeList = KeyWord.executeQuery(query, queryValues)
        println "node list :${nodeList.size()}"
        nodeList.each {
            def keyWord = it["n"] as KeyWord
            def lexicon = it["p"] as Lexicon
            def markupCount = it["c"] as Integer
            println "markup count ${markupCount}"

            keyWord = keyWords.get(keyWord.value) ?: keyWord
            lexicon.link = lexicon.findLink()
            keyWord.lexica = keyWord.lexica ?: []
            keyWord.lexica.add(lexicon)
            keyWord.markupCount = markupCount

            keyWords.put(keyWord.value, keyWord)
        }

        println "generanting uuids for collected keywords ${keyWords.size()}"
        def uuidArray = keyWords.values().uuid

        println "retrieving markup arrays for ${uuidArray}"
        Map<String, JSONObject> markupMaps = keyWordService.findMarkups(uuidArray)
        println "retrieved markup arrays ${markupMaps as JSON}"

        // TODO: merge markup arrays
        println "RETURNED size: ${keyWords.size()}"

        for (KeyWord keyWord in keyWords.values()) {
            JSONObject keyWordObject = new JSONObject()
            keyWordObject.id = keyWord.id
            keyWordObject.value = keyWord.value
            keyWordObject.lexica = keyWord.lexica
            keyWordObject.uuid = keyWord.uuid
            keyWordObject.markups = markupMaps.get(keyWord.uuid)
            // TODO: make a separate query, instead of re-using it a bunch of times
            keyWordObject.markupCount = keyWordObject.markups?.markupCount
            keyWordObject.sources = keyWordSet.sources
            returnArray.add(keyWordObject)
        }
        JSONObject kwsObject = new JSONObject()
        kwsObject.count = count
        kwsObject.keyWords = returnArray
        println (kwsObject as JSON)
        render kwsObject as JSON
    }

    def index() {
        JSONArray returnArray = new JSONArray()

        for (KeyWordSet keyWordSet in KeyWordSet.all) {
            JSONObject keyWordSetObject = new JSONObject()
            keyWordSetObject.id = keyWordSet.id
            keyWordSetObject.sources = keyWordSet.sources
            keyWordSetObject.name = keyWordSet.name
            keyWordSetObject.uuid = keyWordSet.uuid
            keyWordSetObject.keyWordCount = KeyWord.countByKeyWordSet(keyWordSet)
//            keyWordSetObject.keyWordCount = keyWordSet.keyWords.size()
            returnArray.add(keyWordSetObject)
            keyWordSet.save(flush:true)
        }

        render returnArray as JSON
    }

    @Transactional
    def createKeyWordSet() {


        def sourceIds = request.JSON as JSONArray
        String keywordSetName = params.name
        String keywordSetUUID = UUID.randomUUID().toString()

        if(!scheduledJobService.canRunJob()){
            throw new RuntimeException("Running too many jobs")
        }

        def listOfKeywordSets = []

        BidiMap uniqueKeywords = new DualHashBidiMap()
        //each ls needs to retrieve all lexicon uuids and make keywords
        //do this all in a bulk query for speed.

        long startTime, stopTime

        for (ls in sourceIds) {
            println("here is the ls: " + ls)
            //when matching by internal node id, can't use quoted strings which are returned when using "params" pattern
            def query = "match (l:Lexicon)-[]-(ls:LexiconSource) where l.publicName is not null and id(ls) = " + ls + " return l.publicName as name, l.uuid as lexiconUUID, ls.uuid as lexiconSourceUUID union match (l:Lexicon)-[]-(ls:LexiconSource) where l.synonym is not null and id(ls) = " + ls + " return l.synonym as name, l.uuid as lexiconUUID, ls.uuid as lexiconSourceUUID"
            List<Node> nodeList = KeyWord.executeQuery(query)
            println "query results ${nodeList.size()} for ${query}"
            println "input kws size: ${listOfKeywordSets.size()}"
            startTime = System.currentTimeMillis()
            for (node in nodeList) {
                def keywordName = node["name"]
                if (keywordName != null && keywordName != '') {
                    def rowMap2 = [:]
                    rowMap2['lexiconUUID'] = node["lexiconUUID"]
                    rowMap2['keywordValue'] = node["name"]
                    rowMap2['lexiconSourceUUID'] = node["lexiconSourceUUID"]
                    rowMap2['keywordSetUUID'] = keywordSetUUID
//                    def keywordUUID = uniqueKeywords.find{it.value == keywordName }?.key
                    // Gets the key that is currently mapped to the specified value.
                    def keywordUUID = uniqueKeywords.getKey(keywordName)
                    keywordUUID = keywordUUID ?: UUID.randomUUID().toString()
                    rowMap2['keywordUUID'] = keywordUUID
                    rowMap2['keywordSetName'] = keywordSetName
                    def value = node["name"]
                    uniqueKeywords[keywordUUID] = value
                    listOfKeywordSets.add(rowMap2)
                }
            }
            stopTime = System.currentTimeMillis()
            println "finsihsed procesisng query results ${nodeList.size()} for ${query}"
            println "output kws size: ${listOfKeywordSets.size()}"
            println "time ${ (stopTime - startTime) / 1000.0 }"
        }

        JSONObject jobData = new JSONObject()
        jobData.sourceIds = sourceIds
        jobData.params = params

        if(!scheduledJobService.generateAndStartJob( uniqueKeywords.size() )){
            throw new RuntimeException("Running too many jobs or job size too large ${uniqueKeywords.size()}")
        }

        startTime = System.currentTimeMillis()
        //use retrieved map of lexicon and lexicon sources to bulk add keywords and keyword sets and relations
        println "opening DB connection"
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic(EnvironmentUtil.username, EnvironmentUtil.password))
        Session session = driver.session()
        stopTime = System.currentTimeMillis()
        println "time ${ (stopTime - startTime) / 1000.0 }"
        startTime = System.currentTimeMillis()
        println "OPENED DB connection "
        Map<String, Object> params = new HashMap<>()

        println "adding KWS to props: ${listOfKeywordSets.size()}"
        params.put("props", listOfKeywordSets)
        stopTime = System.currentTimeMillis()
        println "time ${ (stopTime - startTime) / 1000.0 }"

        startTime = System.currentTimeMillis()
        println "ADDED KWS to props: ${listOfKeywordSets.size()}"
        println("listofkeywordsets size: " + listOfKeywordSets.size())

        println "Loading keywords into Neo4j"

        session.run("""
             UNWIND {props} as row    
             
                MATCH (ls:LexiconSource {uuid:row.lexiconSourceUUID})
                MATCH (lexicon:Lexicon {uuid:row.lexiconUUID})
                
                MERGE (keywordSet:KeyWordSet {uuid:row.keywordSetUUID,name:row.keywordSetName, version: 0})
             
                MERGE (keywordSet)-[kwsls:SOURCES]->(ls)

                MERGE (keyword:KeyWord {uuid:row.keywordUUID,version: 0})
                    SET keyword.value = row.keywordValue

                MERGE (keywordSet)-[kwkr:KEYWORDS]->(keyword)
                
                MERGE (keyword)-[kwl:KEYWORDS]->(lexicon)
              
        """, params)
        stopTime = System.currentTimeMillis()
        println "finished session run ${ (stopTime - startTime) / 1000.0 }"

        startTime = System.currentTimeMillis()
        session.close()
        driver.close()
        stopTime = System.currentTimeMillis()
        println "flush / closed session session run ${ (stopTime - startTime) / 1000.0 }"
        scheduledJobService.finishJob()

        println "session closed."
    }

    def save() {
        createKeyWordSet()
        return index()
    }

    def sampleKeyWordSet() {
        JSONArray returnArray = new JSONArray()
        KeyWordSet keyWordSet = KeyWordSet.first()
        println "has a Keyword set count: ${KeyWordSet.count}"
        println "has a Keyword set: ${keyWordSet}"
//        println "has a Keyword set length: ${keyWordSet?.keyWords?.size()}"
        if (keyWordSet) {
            for (keyWord in KeyWord.all) {
//                for(keyWord in keyWordSet.keywords){
                returnArray.add(keyWord as JSON)
            }
        }
        render returnArray
    }

    @Transactional
    def addKeyWord(KeyWordSet kws) {
        println "kws: ${kws}"
        println "params: ${params}"
        KeyWord keyWord = new KeyWord(
                value: params.value
                ,uuid: params.uuid
        )

        println "saving kw"
        keyWord.save(flush:true,failOnError: true)
        println "SAVED kw"


        Lexicon lex = Lexicon.findById(params.lexicon)
        println "lex: ${lex}"

        println "adding relationships"
        KeyWord.executeUpdate("MATCH (k:KeyWord),(kws:KeyWordSet),(l:Lexicon) where k.uuid={keyWordUUID} and kws.uuid={keyWordSetUUID} and l.uuid={lexiconUUID} create (k)<-[:KEYWORDS]-(kws),(l)<-[:KEYWORDS]-(k)",
        [ keyWordUUID:keyWord.uuid , keyWordSetUUID:kws.uuid , lexiconUUID:lex.uuid ] )
        println "ADDED relationships"

        render keyWord as JSON
    }

    @Transactional
    def deleteKeyWord() {
        KeyWord keyWord = KeyWord.findById(params.id)
        keyWord.delete();
        render keyWord as JSON
    }
}
