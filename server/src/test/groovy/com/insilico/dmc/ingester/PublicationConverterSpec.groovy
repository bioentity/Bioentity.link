package com.insilico.dmc.ingester

import com.insilico.dmc.publication.Publication
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Publication])
class PublicationConverterSpec extends Specification {

    GeneticsIngester geneticsIngester = new GeneticsIngester()

    def setup() {
    }

    def cleanup() {
    }

    void "test conversion into"() {
        given: "a set of words"
        String inputWord = "<sec id=\"s1\" sec-type=\"materials|methods\">"
        String testWord = "<sec id=\"s1\" sec-type=\"materials|methods\"><sec-comment id=\"s1\" sec-type=\"materials|methods\"/>"
        String otherWord = "<sec id=\"aasdfasdfasdfasfadsf\"><sec-comment id=\"s1\" sec-type=\"materials|methods\"/>"

        when: "we convert it"
        String outputWord = Ingester.convertAllSec(inputWord)


        then: "convert it in"
        assert outputWord == testWord

        when: "we revert it"
        String revertWord = Ingester.revertAllSec(otherWord)

        then: "same"
        assert revertWord == inputWord

        when: "we revert it again"
        revertWord = Ingester.revertAllSec(testWord)

        then: "same"
        assert revertWord == inputWord
    }

    void "test conversion doc"() {
        given: "a set of words"
        String inputWord = "<node>ABC<sec id=\"s1\" sec-type=\"materials|methods\">ABC</node>"
        String testWord = "<node>ABC<sec id=\"s1\" sec-type=\"materials|methods\"><sec-comment id=\"s1\" sec-type=\"materials|methods\"/>ABC</node>"
        String otherWord = "<node>ABC<sec id=\"aasdfasdfasdfasfadsf\"><sec-comment id=\"s1\" sec-type=\"materials|methods\"/>ABC</node>"

        when: "we convert it"
        String outputWord = Ingester.convertAllSec(inputWord)


        then: "convert it in"
        assert outputWord == testWord

        when: "we revert it"
        String revertWord = Ingester.revertAllSec(otherWord)

        then: "same"
        assert revertWord == inputWord

        when: "we revert it again"
        revertWord = Ingester.revertAllSec(testWord)

        then: "same"
        assert revertWord == inputWord
    }

    void "test conversion over multiple in doc"() {
        given: "a set of words"
        String inputWord = "ABC<sec id=\"s1\" sec-type=\"materials|methods\"><node>CCC</node></sec>DDDDD<sec id=\"s3\" sec-type=\"materials|methods\"><node>FFF</node></sec>EEEE"
        String testWord = "ABC<sec id=\"s1\" sec-type=\"materials|methods\"><sec-comment id=\"s1\" sec-type=\"materials|methods\"/><node>CCC</node></sec>DDDDD<sec id=\"s3\" sec-type=\"materials|methods\"><sec-comment id=\"s3\" sec-type=\"materials|methods\"/><node>FFF</node></sec>EEEE"
        String otherWord = "ABC<sec id=\"aasdfasdfasdfasfadsf\"><sec-comment id=\"s1\" sec-type=\"materials|methods\"/><node>CCC</node></sec>DDDDD<sec id=\"aasdfasdfasdfasfadsf\" sec-type=\"materials|methods\"><sec-comment id=\"s3\" sec-type=\"materials|methods\"/><node>FFF</node></sec>EEEE"

        when: "we convert it"
        String outputWord = Ingester.convertAllSec(inputWord)


        then: "convert it in"
        assert outputWord == testWord

        when: "we revert it"
        String revertWord = Ingester.revertAllSec(otherWord)

        then: "same"
        assert revertWord == inputWord

        when: "we revert it again"
        revertWord = Ingester.revertAllSec(testWord)

        then: "same"
        assert revertWord == inputWord
    }
}
