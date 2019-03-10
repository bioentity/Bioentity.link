package com.insilico.dmc

import com.insilico.dmc.lexicon.LexicaCapture
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.lexicon.LexiconSourceClassEnum
import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.KeyWordSet
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

//import grails.test.neo4j.Neo4jSpec
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
class KeyWordServiceSpec extends Specification {

//    @Shared @AutoCleanup Neo4jDatastore datastore = new Neo4jDatastore(getClass().getPackage())

    @Autowired
    LexicaCaptureService lexicaCaptureService
    @Autowired
    KeyWordService keyWordService


    def setup() {
//        cleanOutData()
    }

    def cleanup() {
    }

    void cleanOutData() {
//        KeyWord.deleteAll(KeyWord.all)
//        KeyWordSet.deleteAll(KeyWordSet.all)
//        Lexicon.deleteAll(Lexicon.all)
//        LexicaCapture.deleteAll(LexicaCapture.all)
//        Species.deleteAll(Species.all)
//        LexiconSource.deleteAll(LexiconSource.all)
    }

//    @Rollback
    @Ignore
    void "generate small keywordset for single lexicon"() {


        given: "A small lexica with a lexicon source"
        assert KeyWordSet.count == 0
        String fileLocation = "./src/test/resources/lexica/smallpom1.tsv"
        File testLexiconFile = new File(fileLocation)
        LexiconSource lexiconSource = new LexiconSource(
                source: "test mod"
                , className: LexiconSourceClassEnum.GENE
                , urlConstructor: new URL("http://zfin.org")
                , file: testLexiconFile
        ).save(flush:true)


        when: "We ingest it we should have a lexicon"
        LexicaCapture lexicaCapture = lexicaCaptureService.capture(lexiconSource)
        LexiconSource lexiconSource1 = LexiconSource.first()
        List<Lexicon> lexiconList = Lexicon.findAllByLexiconSource(lexiconSource1)

        then: "We should see the results of it"
        assert LexiconSource.count == 1
        assert LexicaCapture.count == 1


        when: "We generate a keywordset from that"
        KeyWordSet keyWordSet = keyWordService.generateKeyWordSet([lexiconSource])
        List<KeyWord> keyWordList = KeyWord.findAllByKeyWordSet(keyWordSet)
        println "# of keywords ${KeyWord.count}"


        then: "We should be able to see that they match and we have a valid KeyWordSet"
        assert KeyWordSet.count == 1
        assert lexiconList.size() == 9
        assert keyWordList.size() == lexiconList.size() + 5 // have to add the synonyms


    }
}
