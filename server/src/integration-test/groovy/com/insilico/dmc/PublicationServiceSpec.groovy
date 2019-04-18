package com.insilico.dmc

import com.insilico.dmc.ingester.GeneticsIngester
import com.insilico.dmc.publication.Publication
import grails.test.mixin.integration.Integration
import grails.transaction.*
import org.apache.commons.io.FileUtils
import spock.lang.*

@Integration
@Rollback
class PublicationServiceSpec extends Specification {

    GeneticsIngester geneticsIngester = new GeneticsIngester()
    PublicationService publicationService = new PublicationService()

    def setup() {
    }

    def cleanup() {
    }

    void "extract indexable content 200592"(){

        given: "publication"
        String fileLocation = "./src/test/resources/publication/genetics/200592.xml"
        File publicationFile = new File(fileLocation)

        when: "we index that content"
        Publication publication = publicationService.ingestPubFile(publicationFile)
        println "publiction ${publication}"
        Set<String> words = geneticsIngester.extractIndexableContent(publication)
        println "words: ${words.size()}"
        println "contains daf-2: " + publication.originalData.value.contains("daf-2")
        String testOutput = words.findAll(){
            it.contains("daf-2")
        }.join(":-:")
        println "testOutput: ${testOutput}"

        then: "extracted words"
        assert  words.contains("skn-1")
        assert  words.contains("prdx-2")
        assert  words.contains("gst-4")
        assert  words.contains("rpl-2")
        assert  words.contains("skr-1")
        assert  words.contains("pas-6")
        assert  words.contains("cct-1")
        assert  words.contains("gsk-3")
        assert  words.contains("pas-5")
        assert  words.contains("ragc-1")
        assert  words.contains("daf-16")
        assert  words.contains("daf-2")
        assert  words.contains("xrep-4")
        assert  words.contains("unc-8")
        assert  words.contains("unc-119")

        // new not found
        // handling pRF4(rol-6(su1006))],
//        pRF4(rol-6(su1006))], -> 0-17 :: 21
        assert  words.contains("rol-6")
    }


    void "extract indexable content 037747"(){

        given: "publication"
        String fileLocation = "./src/test/resources/publication/genetics/037747.xml"
        File publicationFile = new File(fileLocation)

        when: "we index that content"
        Publication publication = publicationService.ingestPubFile(publicationFile)
        println "found pub: ${publication}"
        Set<String> words = geneticsIngester.extractIndexableContent(publication)
        println "words: ${words.size()}"
//        println "contains daf-2: " + publication.originalData.value.contains("daf-2")

        assert  words.contains("act-1")
        assert  words.contains("math-33")
        assert  words.contains("zhit-1")

        String testOutput = words.findAll(){
            it.contains("daf-12")
        }.join(":-:")
        println "testOutput: ${testOutput}"

        then: "extracted words"
        assert  words.contains("daf-12")
        assert  words.contains("dbl-1")
        assert  words.contains("mes-2")
        assert  words.contains("hlh-8")
        assert  words.contains("dct-3")
        assert  words.contains("hcf-1")
        assert  words.contains("lex-1")
        assert  words.contains("dpy-20")
    }

    void "extract indexable content 200568"(){

        given: "publication"
        String fileLocation = "./src/test/resources/publication/genetics/200568.xml"
        File publicationFile = new File(fileLocation)

        when: "we index that content"
        Publication publication = publicationService.ingestPubFile(publicationFile)
        Set<String> words = geneticsIngester.extractIndexableContent(publication)
        println "words: ${words.size()}"
//        println "contains daf-2: " + publication.originalData.value.contains("daf-2")

        assert  words.contains("daf-28")
        assert  words.contains("gpa-4")
        assert  words.contains("unc-22")

        String testOutput = words.findAll(){
            it.contains("atgl-1")
        }.join(":-:")
        println "testOutput: ${testOutput}"

        then: "extracted words"
        assert  words.contains("atgl-1")
        assert  words.contains("pek-1")
    }

    void "extract indexable content 185322"(){

        given: "publication"
        String fileLocation = "./src/test/resources/publication/genetics/185322.xml"
        File publicationFile = new File(fileLocation)

        when: "we index that content"
        Publication publication = publicationService.ingestPubFile(publicationFile)
        Set<String> words = geneticsIngester.extractIndexableContent(publication)
        println "words: ${words.size()}"
//        println "contains daf-2: " + publication.originalData.value.contains("daf-2")


        String testOutput = words.findAll(){
            it.contains("atgl-1")
        }.join(":-:")
        println "testOutput: ${testOutput}"

        then: "extracted words"
        assert  words.contains("cdc-42")
        assert  words.contains("pmp-3")
        assert  words.contains("ret-1")
        assert  words.contains("rgef-1")
        assert  words.contains("vab-1")
        assert  words.contains("vab-2")
    }

    void "fix suffix"(){
        given: "various xml suffixes"
        String suffix1 = "aFileName"
        String suffix2 = "aFileName.xml"
        String suffix3 = "aFileName.XML"
        String result

        when: "we try each suffix"
        result = publicationService.fixFileName(suffix1)

        then:
        assert result==suffix2

        when: "we try each suffix"
        result = publicationService.fixFileName(suffix2)

        then:
        assert result==suffix2

        when: "we try each suffix"
        result = publicationService.fixFileName(suffix3)

        then:
        assert result==suffix2
    }

    void "detect pub types"(){

        given: "a set of g3 and elife pubs"
        File inputDirectory = new File("./src/test/resources/publication/2017_worm_xml/input")
//        File elifeDirectory = new File("./src/test/resources/publication/2017_worm_xml/input")
        def gsaPubTypes = []
        def elifePubTypes = []

        when: "we ingest all of the pubs in gsa"
        inputDirectory.eachFile {
            println "it: ${it}"
            if(!it.name.startsWith("elife")){
                Publication publication = publicationService.ingestPubFile(it)
                if(publication){
                    String pubType = publicationService.getPubType(publication)
                    gsaPubTypes.add(pubType)
                }
            }
        }

        then: "we confirm they were all gsa"
        assert gsaPubTypes
        assert gsaPubTypes.size() >0
        gsaPubTypes.each {
            assert it == "GSA"
        }

        when: "we ingest all of the pubs in elife"
        inputDirectory.eachFile {
            println "it: ${it}"
            if(it.name.startsWith("elife")) {
                Publication publication = publicationService.ingestPubFile(it)
                if(publication){
                    String pubType = publicationService.getPubType(publication)
                    elifePubTypes.add(pubType)
                }
            }
        }

        then: "we confirm they were all elife"
        assert elifePubTypes
        assert elifePubTypes.size() >0
        elifePubTypes.each {
            assert it == "elife"
        }

    }

}
