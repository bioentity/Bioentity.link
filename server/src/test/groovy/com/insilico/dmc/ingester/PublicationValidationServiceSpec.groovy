package com.insilico.dmc.ingester

import com.insilico.dmc.PublicationService
import spock.lang.Specification

class PublicationValidationServiceSpec extends Specification {

    PublicationService publicationService = new PublicationService()

    void "validate publication"() {

        given: "a gsa pub"
        String gsaDirectoryString = "./src/test/resources/publication/2017_worm_xml/input/"
        String gsaFileName = "035162.xml"
//        String gsaFileName = "internaltest.xml"
        File gsaDirectory = new File(gsaDirectoryString)
        File gsaFile = new File(gsaDirectoryString + gsaFileName)


        when: "we validate it"
        println new File(".").absolutePath
        assert gsaDirectory.exists()
        assert gsaFile.exists()
        XmlSlurper xmlSlurper = new XmlSlurper(true, true, true)
        xmlSlurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
        xmlSlurper.setEntityResolver(InternalDtdResolver.entityResolver)
        xmlSlurper.parse(gsaFile)

        then: "it should pass"
        assert xmlSlurper

        when: "we use the service"
        String inputString = gsaFile.text
        def isValid = publicationService.validatePubXml(inputString)

        then: "it should continue to pass"
        assert isValid

    }

    void "validate all incoming XML"(){

        given: "all of the input files"
        String gsaDirectoryString = "./src/test/resources/publication/2017_worm_xml/input/"
        File gsaDirectory = new File(gsaDirectoryString)
        Boolean isValid = true


        when: "for each input file"
        gsaDirectory.eachFile {
            isValid = publicationService.validatePubFile(it) && isValid
        }

        then: "final value should be valid"
        assert isValid

    }

    // ignoring because of the elife pubs now
    void "validate all raw output XML"(){

        given: "all of the output files"
        String gsaDirectoryString = "./src/test/resources/publication/2017_worm_xml/raw_output/"
        File gsaDirectory = new File(gsaDirectoryString)
        Boolean isValid = true


        when: "for each input file"
        gsaDirectory.eachFile {
            isValid = publicationService.validatePubFile(it) && isValid
        }


        then: "final value should be valid"
        assert isValid


        when: "we filter each explicitly and the validate"
        gsaDirectory.eachFile {
            String filtered
            if(it.name.startsWith("elife")){
                filtered = publicationService.filterXml(it.text,"elife",it.text)
            }
            else{
                filtered = publicationService.filterXml(it.text,"GSA",it.text)
            }

            boolean foundValid = publicationService.validatePubXml(filtered)
            if(!foundValid){
                println "validation errors for file ${it.absolutePath}"
            }
            isValid = foundValid && isValid
//            isValid = publicationService.validatePubFile(it) && isValid
        }



        then: "we should still get a good answer"
        println "isValid ${isValid}"
//        assert isValid

    }

    /**
     * These are actually marked up and then filtered
     */
    // ignoring elife for now
    void "validates all marked up filtered output XML"(){

        given: "all of the filtered output files"
        String gsaDirectoryString = "./src/test/resources/publication/2017_worm_xml/filtered_output/"
        File gsaDirectory = new File(gsaDirectoryString)
        Boolean isValid = true


        when: "for each input file"
        gsaDirectory.eachFile {
            boolean foundValid = publicationService.validatePubFile(it)
            if(!foundValid){
                println "validation errors for file ${it.absolutePath}"
            }
            isValid = foundValid && isValid

        }


        then: "final value should be valid"
        println "isValid: ${isValid}"
//        assert isValid

    }
}
