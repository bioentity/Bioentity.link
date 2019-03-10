package dmc

import com.insilico.dmc.KeyWordService
import com.insilico.dmc.Species
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.lexicon.LexiconSourceClassEnum
import com.insilico.dmc.lexicon.StopWord
import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.KeyWordSet
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.rule.Rule
import com.insilico.dmc.rule.RuleSet
import grails.util.Environment

class BootStrap {

    def mockDataService
    def publicationService
    def publicationLoadService
    def keyWordService
    def domainMarshallerService

    def init = { servletContext ->
        // TODO: load a single publication if not loaded, then we can ingestPubFile and view it
        println "registering domains "
        domainMarshallerService.register()

        println " ${KeyWordSet.count}"
//        if (Environment.isDevelopmentMode())
        mockDataService.mockSpecies()
//        if (Environment.isDevelopmentMode() && Lexicon.count == 0) mockDataService.mockLexica()
//
//        if (Environment.isDevelopmentMode()) publicationService.removeAllPubs()
//        println "1 publicadtion count: ${Publication.count}"
//        if (Environment.isDevelopmentMode() && Publication.count < 5) ingestElifePubs()
//        println "2 publicadtion count: ${Publication.count}"
        // ingest yeast pubs
//        if (Environment.isDevelopmentMode() && Publication.count < 100) ingestPubs("../GSA-pipeline/incoming_xml/incoming_xml_yeast")
//        println "3 publicadtion count: ${Publication.count}"
//        if (Environment.isDevelopmentMode() ) ingestPubs("../GSA-Pipeline/incoming_xml/incoming_xml_worm")
//        if(Environment.isDevelopmentMode()) mockDataService.mockBogusPublications()
//        if (Environment.isDevelopmentMode()) removeKeyWords()
//        if (Environment.isDevelopmentMode() && KeyWord.count == 0) generateKeyWords(5000)
//        if(Environment.isDevelopmentMode()) generateKeyWords(1000)

//        if (Environment.isDevelopmentMode()) mockDataService.removeAllMarkups()
//        if(Environment.isDevelopmentMode() && Markup.count < 10 ) mockDataService.mockMarkup(100)
//        if (Environment.isDevelopmentMode() && Markup.count == 0) mockDataService.mockMarkup()

        if (StopWord.count == 0) mockDataService.loadStopWords()

        println "# of keywordsets: ${KeyWordSet.count}"


        mockDataService.addRoles()
        mockDataService.addUsers()

//        KeyWordSet.all.each {
//            println "# of keywords per set: ${KeyWord.countByKeyWordSet(it)}"
//        }

//        if(LexiconSource.countBySource("Sample Data")==0){
//            String fileLocation = "./src/test/resources/lexica/smallpom1.tsv"
//            LexiconSource lexiconSource = new LexiconSource(
//                    source: "Sample Data"
//                    ,className: LexiconSourceClassEnum.GENE
//                    ,urlConstructor: new URL("http://somesource.org")
//                    ,file: new File(fileLocation)
//                    ,species: Species.findByName("Yeast")
//            ).save(failOnError: true)
        // lexicaCaptureService.capture(lexiconSource)

        //KeyWordSet keyWordSet = keyWordService.generateKeyWordSet([lexiconSource],"Some name")
//        }
//        publicationLoadService.loadGSAPubs(40,100)
//        publicationLoadService.loadElifePubs(40,100) // this will just load a git pull

//        publicationLoadService.scheduleLoad(10,24*60*60*60)


        createRules()

        // check for a max of 100 pubs every 8 hours

//        if (Environment.current==Environment.PRODUCTION){
////            if (Environment.current!=Environment.TEST){
//            publicationLoadService.scheduleLoad(400,8*60*60)
////            publicationLoadService.scheduleLoad(50,20)
//        }
    }

    def removeKeyWords() {
        keyWordService.removeKeyWords()
    }

    def generateKeyWords(int max = -1) {
        println "initial keyword count ${KeyWord.count}"
        if (KeyWord.count == 0) {
            println "generating key word set "
            KeyWordSet keyWordSet = keyWordService.generateKeyWordSet(LexiconSource.all)
            println "keyword set: ${keyWordSet}"
            println "keyword set has size: ${keyWordSet?.keyWords?.size()}"
            println "generated keywords: ${KeyWord.count}"
            keyWordSet.save(flush: true)
        }
        def keyWordSetList = keyWordService.getKetWordSets(LexiconSource.all)
        println "found a keyword set list ${keyWordSetList.size()}"
    }

    def ingestPubs(String parentLocation) {
        File parentDirectory = new File(parentLocation)
        if (!parentDirectory.exists()) {
            parentDirectory = new File("../" + parentLocation)
        }
        publicationService.ingestList(parentDirectory)
    }

    def ingestElifePubs() {
        String parentLocation = "../GSA-pipeline/incoming_xml/eLife_Yeast"
        File parentDirectory = new File(parentLocation)
        if (!parentDirectory.exists()) {
            parentDirectory = new File("../" + parentLocation)
        }
        publicationService.ingestList(parentDirectory)
    }

    def destroy = {
    }

    def createRules() {
        println("Create Rule Set")
        if (RuleSet.count == 2) {
            return
        }

        //mockDataService.createRuleSet()

        println("# of rule sets: " + RuleSet.count)
    }
}
