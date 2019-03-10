package com.insilico.dmc

import com.insilico.dmc.lexicon.LexicaCapture
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.lexicon.LexiconSourceClassEnum
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.grails.datastore.gorm.neo4j.Neo4jDatastore
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Integration
@Rollback
class LexicaCaptureServiceSpec extends Specification {

//    @Shared
//    @AutoCleanup
//    Neo4jDatastore datastore = new Neo4jDatastore(getClass().getPackage())

    @Autowired
    LexicaCaptureService lexicaCaptureService


    def setup() {
//        Lexicon.deleteAll(Lexicon.all)
//        LexicaCapture.deleteAll(LexicaCapture.all)
//        Species.deleteAll(Species.all)
//        LexiconSource.deleteAll(LexiconSource.all)
//        KeyWord.deleteAll(KeyWord.all)
//        KeyWordSet.deleteAll(KeyWordSet.all)
    }

    def cleanup() {
    }

//    @Rollback
//    void "grab a small capture"() {
//
//        given: "a lexicon file location"
//        println "runing setup - adding species ${Species.count}"
//        Species species = Species.count == 0 ? new Species(
//                name: "Yeast"
//                , taxonId: "4932"
//        ).save(failOnError: true, flush: true, insert: true) : Species.first()
//        println "runing setup - ADDED species ${Species.count} for ${species}"
//        def speciesId = species?.id
//        println "retrieved a species ${Species.first()}"
//        println "retrieved a directly ${Species.findById(speciesId)}"
//        URL url = new URL("https://somejournal.com/abcd/")
//        new LexiconSource(
//                source: "Some Journal"
//                , className: LexiconSourceClassEnum.GENE // can't remember what this was
//                , urlConstructor: url
//                , species: species
//        ).save(failOnError: true, insert: true, flush: true)
////        println "runing lexicon source ${source} - count ${LexiconSource.count}"
//
//        String fileLocation = "./src/test/resources/lexica/pombase1.tsv"
////        String fileLocation = "./src/test/resources/lexica/smallpom1.tsv"
//
//        when: "we call the service"
//        println "Species count ${Species.count}"
//        File lexicaFile = new File(fileLocation)
//
//        lexicaCaptureService.capture(LexiconSource.first(), lexicaFile)
//        LexicaCapture capture = LexicaCapture.first()
//        LexiconSource source = LexiconSource.first()
//
//
//        then: "we expect to see a lot of lexica imported and an capture"
//        assert Lexicon.count == 154
//        assert LexicaCapture.count == 1
//        assert Species.count == 1
//        assert LexiconSource.count == 1
//
//        assert capture.source == source
//        assert capture.fileName == lexicaFile.absolutePath
//        assert capture.lexica.size() == 154
//        assert source.source == "Some Journal"
//        assert source.urlConstructor == url
//        assert source.className == LexiconSourceClassEnum.GENE
//        // TODO: assert that the LexicaCapture and LexiconSource are correct
//
//        when: "we check row 1 with synonyms and no"
//        Lexicon l1 = Lexicon.findByExternalModId("SPAC1002.01")
//        println "SYNONYM: " + l1.synonym
//
//        then: "we should see a row of synymoms and verify the species, ids, public names"
//        assert l1.externalModId == "SPAC1002.01"
//        assert l1.publicName == null
//        assert l1.synonyms == ["SPAC1610.05"]
//
//
//        when: "we check a row without synonyms"
//        Lexicon l2 = Lexicon.findByExternalModId("SPAC1002.07c")
//
//        then: "we should see a row of synymoms and verify the species, ids, public names"
//        assert l2.externalModId == "SPAC1002.07c"
//        assert l2.publicName == "ats1"
//        assert l2.synonyms == []
//
//        when: "we check a row with multiple synonyms"
//        Lexicon l3 = Lexicon.findByExternalModId("SPAC1071.08")
//
//        then: "we should see a row of synymoms and verify the species, ids, public names"
//        assert l3.externalModId == "SPAC1071.08"
//        assert l3.publicName == "rpp203"
//        assert l3.synonyms == ["rla6", "rpa2", "rpp2-3"]
//
//
//    }

    @Ignore
//    @Rollback
    void "grab a large capture"() {

        given: "a lexicon file location"
//        String fileLocation = "./src/test/resources/lexica/gene1.tsv"
        URL url = new URL("https://somejournal.com/abcd/")
        String fileLocation = "./src/test/resources/lexica/smallpom1.tsv"

        when: "we call the service"
        File lexicaFile = new File(fileLocation)

        Long start, stop
        start = System.currentTimeMillis()
        lexicaCaptureService.capture(LexiconSource.first(), lexicaFile)
        stop = System.currentTimeMillis()
        println "lexica postgresql capture time: ${stop - start}"
        LexicaCapture capture = LexicaCapture.first()
        LexiconSource source = LexiconSource.first()


        then: "we expect to see a lot of lexica imported and an capture"
        assert Lexicon.count == 6995
        assert LexicaCapture.count == 1
        assert Species.count == 1
        assert LexiconSource.count == 1

        assert capture.source == source
        assert capture.fileName == lexicaFile.absolutePath
        assert capture.lexica.size() == 6995
        assert source.source == "Some Journal"
        assert source.urlConstructor == url
        assert source.className == LexiconSourceClassEnum.GENE
        // TODO: assert that the LexicaCapture and LexiconSource are correct

        when: "we check row 1 with synonyms and no"
        Lexicon l1 = Lexicon.findByExternalModId("SPAC1002.01")
        println "SYNONYM: " + l1.synonym

        then: "we should see a row of synymoms and verify the species, ids, public names"
        assert l1.externalModId == "SPAC1002.01"
        assert l1.publicName == null
        assert l1.synonyms == ["SPAC1610.05"]


        when: "we check a row without synonyms"
        Lexicon l2 = Lexicon.findByExternalModId("SPAC1002.07c")

        then: "we should see a row of synymoms and verify the species, ids, public names"
        assert l2.externalModId == "SPAC1002.07c"
        assert l2.publicName == "ats1"
        assert l2.synonyms == []

        when: "we check a row with multiple synonyms"
        Lexicon l3 = Lexicon.findByExternalModId("SPAC1071.08")

        then: "we should see a row of synymoms and verify the species, ids, public names"
        assert l3.externalModId == "SPAC1071.08"
        assert l3.publicName == "rpp203"
        assert l3.synonyms == ["rla6", "rpa2", "rpp2-3"]
    }
}
