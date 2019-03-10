package com.insilico.dmc

import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.lexicon.LexiconSourceClassEnum
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class LexiconSourceControllerSpec extends Specification {


    LexiconService lexiconService = new LexiconService()

    def setup() {
    }

    def cleanup() {
    }


    void "update a lexicon source"() {

        given: "a set of UUIDs"
        List<String> uuids = new ArrayList<>()
        for(int i = 0 ; i < 3 ; i++){
            uuids.add(UUID.randomUUID().toString())
        }

        when: "create a lexicon source"
        Species species = new Species(
                name: "dog"
                , taxonId: "NCBI:1231"
        ).save()
        LexiconSource lexiconSource = new LexiconSource(
                source: "TestMod"
                , prefix: "TM"
                , className: LexiconSourceClassEnum.GENE
                , urlConstructor: new URL("http://abc.com/gene/@@ID@@")
                , uuid: uuids[0]
                , species: species
        ).save(flush: true)
        int lexiconCount = Lexicon.countByLexiconSource(lexiconSource)

        then: "confirm that we have one filled out as we would expect"
        assert Species.countByName("dog")==1
        assert LexiconSource.countByUuid(uuids[0])==1
        assert LexiconSource.findByUuid(uuids[0]).prefix=='TM'
        assert lexiconCount == 0

        when: "we add lexica"
        new Lexicon(
                publicName: "Gene1"
                ,synonym: "abcd"
                ,curatorNotes: "blah blah"
                ,uuid: uuids[1]
                ,isActive: true
                ,lexiconSource: lexiconSource
        ).save()
        new Lexicon(
                publicName: "Gene2"
                ,curatorNotes: "blah blahl bah"
                ,uuid: uuids[2]
                ,isActive: true
                ,lexiconSource: lexiconSource
        ).save(flush: true)
        lexiconCount = Lexicon.countByLexiconSource(lexiconSource)

        then: "we should expect to see them association with the lexicon source"
        assert lexiconCount == 2

        when: "we update any part of it"
        lexiconSource.prefix = "TEST"
        lexiconSource = lexiconService.updateLexiconSource(lexiconSource)
        lexiconCount = Lexicon.countByLexiconSource(lexiconSource)

        then: "we expect to see the modifications to the source only and the same number of relatinoships"
        assert Species.countByName("dog")==1
        assert LexiconSource.countByUuid(uuids[0])==1
        assert LexiconSource.findByUuid(uuids[0]).prefix=='TEST'
        assert lexiconCount == 2
    }
}
