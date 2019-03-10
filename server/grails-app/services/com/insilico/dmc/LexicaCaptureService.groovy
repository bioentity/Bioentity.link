package com.insilico.dmc

import com.insilico.dmc.lexicon.LexicaCapture
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import grails.converters.JSON
import grails.transaction.Transactional
import org.grails.web.json.JSONObject
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session

@Transactional
class LexicaCaptureService {

    def scheduledJobService

    /**
     * @deprecated
     * @param lexiconSource
     * @param file
     * @return
     */
    def capture(LexiconSource lexiconSource, File file = null) {
        file = file ?: new File(lexiconSource.file)
        String name = file.absolutePath


        LexicaCapture lexicaCapture = new LexicaCapture(
                fileName: name,
                source: lexiconSource
        ).save(insert: true, flush: true)

        Map<String, JSONObject> wordMap = new HashMap<>()

        Long start, stop
        start = System.currentTimeMillis()
        int wordCount = 0
        file.splitEachLine('\t') { fields ->
            String lookupField = fields[0]
            if (lookupField && !lookupField.startsWith("#")) {
                JSONObject wordObject = wordMap.containsKey(lookupField) ? wordMap.get(lookupField) : new JSONObject()
                wordObject.externalModId = lookupField
                wordObject.publicName = fields[1]
                wordObject.synonym = fields[2]
                wordMap.put(lookupField, wordObject)
                ++wordCount
            }
        }


        Integer wordMapListSize = 1000
        List<Map<String, JSONObject>> wordMapList = []

        Map<String, JSONObject> internalWordMap = null
        int i = 0
        for (def entry in wordMap.entrySet()) {
            if (i % wordMapListSize == 0) {
                if(internalWordMap!=null){
                    wordMapList.add(internalWordMap)
                }
                internalWordMap = new HashMap<>()
            }
            internalWordMap.put(entry.key,entry.value)
            ++i
        }
        wordMapList.add(internalWordMap)
        stop = System.currentTimeMillis()
        println "parsing time ${stop - start} of size: ${wordMap.size()}"

        start = System.currentTimeMillis()
        // TODO: change to dump out to CSV file and then re-import

        wordMapList.each {
            storeLexiconSourceHibernate(lexicaCapture, it)
        }
        //storeLexiconSourceBolt(lexicaCapture,wordMap)

        start = System.currentTimeMillis()
        session.flush()
        lexicaCapture.save(insert: false, flush: true)
        stop = System.currentTimeMillis()
        println "flushing time ${stop - start} of size: ${wordMap.size()}"
        return lexicaCapture

    }

    def storeLexiconSourceBolt(List<List<String>> lexicaList) {
        long startTime, saveTime, start, stop
        int FLUSH_SIZE = 1000
        saveTime = 0l
        println lexicaList
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic(EnvironmentUtil.username, EnvironmentUtil.password))
        Session session = driver.session()
        startTime = System.currentTimeMillis()
        // should look something closer to this:
        // CREATE (n:Lexicon { publicName:'sox9b' , externalModId:'ZDB-1111',synonym:'abcd|123'})-[lexiconSource:LexiconSource]->(p:LexiconSource {id:'1244678131040796673'})
        session.run("UNWIND {lexicaList} AS i CREATE (n:Lexicon {externalModId: i.externalModId, publicName: i.publicName, synonym: i.synonym, sourceId: i.sourceId})", [list: [lexicaList]])
        session.close()

        stop = System.currentTimeMillis()
        println "${FLUSH_SIZE} storing time ${saveTime} of size: ${lexicaList.size()} for ${(float) lexicaList.size() / saveTime}"
    }

    def storeLexiconSourceBolt(LexicaCapture lexicaCapture, HashMap<String, JSONObject> wordMap, int maxLexica = -1) {
        int count = 0
        List<List<String>> lexicaList = []
        List<String> header = ['externalModId', 'publicName', 'synonym', 'sourceId']
        lexicaList.add(header)
        wordMap.each { k, v ->
            if (maxLexica < 0 || count < maxLexica) {
                List<String> lexicon = [k, v.publicName, v.synonym as String, lexicaCapture.sourceId as String]
                lexicaList.add(lexicon)
                ++count
            }
        }
        // add the prefix
        storeLexiconSourceBolt(lexicaList)
    }

    def storeLexiconSourceHibernate(LexicaCapture lexicaCapture, Map<String, JSONObject> wordMap) {
        int FLUSH_SIZE = 1000
        int count = 0
        long start, stop
        long saveTime = 0l
        LexiconSource lexiconSource = lexicaCapture.source

        start = System.currentTimeMillis()
        wordMap.each { k, v ->
            Lexicon lexicon = new Lexicon(
                    externalModId: k
                    , publicName: v.publicName
                    , synonym: v.synonym
                    , lexiconSource: lexiconSource
                    , uuid: UUID.randomUUID().toString()
            ).save(failOnError: true, insert: true)
//            if (!lexicon.validate()) {
//                println "invalid ${lexicon as JSON}"
//            }
            lexicon.save(failOnError: true, insert: true)
            lexiconSource.addToLexica(lexicon)
            lexicaCapture.addToLexica(lexicon)
            if (count % FLUSH_SIZE == 0) {
                stop = System.currentTimeMillis()
//                println "flushing at ${count} time is ${stop - start} speed: ${FLUSH_SIZE / ((stop - start) / 1000)}"
                println "flushing at ${count} time is ${stop - start} "
                saveTime += stop - start
                start = stop
                lexicaCapture.save(flush: true)
            }
            log.debug("Importing domainObject  ${lexicon.toString()}")
            ++count
        }
        stop = System.currentTimeMillis()
        println "${FLUSH_SIZE} storing time ${saveTime} of size: ${wordMap.size()} for ${(float) wordMap.size() / saveTime}"
    }

    def clearLexica(LexiconSource lexiconSource) {
        println "clearLexica deleteing source for name ${lexiconSource as JSON}"
        int deleted = LexiconSource.executeUpdate("MATCH (l:Lexicon)-[ls:LEXICA]-(s:LexiconSource),(l:Lexicon)-[r]-(x) where s.uuid={uuid}  delete l,ls,r",[uuid:lexiconSource.uuid])
        // need to delete any without the additional associations
        deleted += LexiconSource.executeUpdate("MATCH (l:Lexicon)-[ls:LEXICA]-(s:LexiconSource) where s.uuid={uuid} delete l,ls",[uuid:lexiconSource.uuid])
        return deleted
    }
}
