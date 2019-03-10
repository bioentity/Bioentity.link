package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class PublicationLoadServiceIntegrationSpec extends Specification {

    PublicationLoadService publicationLoadService = new PublicationLoadService()

    def setup() {
    }

    def cleanup() {
    }

    void "ingest a single GSA worm pub file"() {

        when: "we ingest the worm files"
        int initPub = Publication.count
        def publicationFiles = publicationLoadService.processGSAFilesForURL(
                publicationLoadService.GSA_MOST_RECENT_FILES_WORM
                , publicationLoadService.GSA_WORM_PUB_URL
                , 3)
        int finalPubs = Publication.count
        println "init pubs ${initPub}"
        println "final pubs ${finalPubs}"


        then: "it should process them"
        assert publicationFiles.size() >= finalPubs - initPub
        assert finalPubs >= initPub
        assert finalPubs > 0

    }

    void "ingest a single GSA yeast pub file"() {

        when: "we ingest the yeast files"
        int initPub = Publication.count
        def publicationFiles = publicationLoadService.processGSAFilesForURL(
                publicationLoadService.GSA_MOST_RECENT_FILES_YEAST
                , publicationLoadService.GSA_YEAST_PUB_URL
                , 3)
        int finalPubs = Publication.count
        println "init pubs ${initPub}"
        println "final pubs ${finalPubs}"


        then: "it should process them"
        assert publicationFiles.size() == finalPubs - initPub
        assert finalPubs >= initPub
        assert finalPubs > 0

    }

    void "ingest a single GSA fly pub file"() {

        when: "we ingest the fly files"
        int initPub = Publication.count
        def publicationFiles = publicationLoadService.processGSAFilesForURL(
                publicationLoadService.GSA_MOST_RECENT_FILES_FLY
                , publicationLoadService.GSA_FLY_PUB_URL
                , 3)
        int finalPubs = Publication.count
        println "init pubs ${initPub}"
        println "final pubs ${finalPubs}"


        then: "it should process them"
        assert publicationFiles.size() >= finalPubs - initPub
        assert finalPubs >= initPub
        assert finalPubs > 0

    }


}
