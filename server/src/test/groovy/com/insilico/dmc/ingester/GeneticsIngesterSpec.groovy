package com.insilico.dmc.ingester

import com.insilico.dmc.publication.Publication
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Publication])
class GeneticsIngesterSpec extends Specification {

    GeneticsIngester geneticsIngester = new GeneticsIngester()

    def setup() {
    }

    def cleanup() {
    }

    void "test ingestion with spaces"() {
        given: "a set of words"
        String testWord = "the rain in spain falls mainly in the plain"
        TreeSet<String> words = new TreeSet<>()

        when: "we consume it"
        geneticsIngester.consume(words, testWord)


        then: "we should get 7 unique words"
        assert words.size() == 7
        assert words[0] == "falls"
        assert words[1] == "in"
        assert words[2] == "mainly"
        assert words[3] == "plain"
        assert words[4] == "rain"
        assert words[5] == "spain"
        assert words[6] == "the"
    }

    void "test ingestion with tags"() {
        given: "a set of words"
        String testWord = "<i>the</i> <i>rain</i> <i>in</i> spain <i>falls</i> mainly <i>in</i> the plain"
        TreeSet<String> words = new TreeSet<>()

        when: "we consume it"
        geneticsIngester.consume(words, testWord)
        println words.join(",")


        then: "we should get 7 unique words"
        assert words.size() == 7
        assert words[0] == "falls"
        assert words[1] == "in"
        assert words[2] == "mainly"
        assert words[3] == "plain"
        assert words[4] == "rain"
        assert words[5] == "spain"
        assert words[6] == "the"
    }

    void "test ingestion with multiple tags"() {
        given: "a set of words"
        String testWord = "<i>the</i> <b><a><i>rain</i></a></b> <i>in</i> spain <italics href='asdf'><a href='http://asadf.com/asdf'>falls</a></italics> mainly <i>in</i> the plain"
        TreeSet<String> words = new TreeSet<>()

        when: "we consume it"
        geneticsIngester.consume(words, testWord)
        println words.join(",")


        then: "we should get 7 unique words"
        assert words.size() == 7
        assert words[0] == "falls"
        assert words[1] == "in"
        assert words[2] == "mainly"
        assert words[3] == "plain"
        assert words[4] == "rain"
        assert words[5] == "spain"
        assert words[6] == "the"
    }

    void "other real tests"(){
        given: "some real words"
        String testWord = "id=\"italic-294\">cct-1</italic>; id=\"italic-294\">cct-1</italic>;"
        TreeSet<String> words = new TreeSet<>()

        when: "we consume it"
        geneticsIngester.consume(words, testWord)
        println words.join(",")

        then: "we should get 1 unique word"
        assert words.size() == 1
        assert words[0] == "cct-1"
    }

    void "process publication input"(){

        given: "some type of input"
        String fileLocation = "./src/test/resources/publication/genetics/200592.xml"
        File publicationFile = new File(fileLocation)
        TreeSet<String> words = new TreeSet<>()

        when: "we process  the full text"
        geneticsIngester.consume(words,publicationFile.text)
        println "words: ${words.size()}"

        then: "we should find a cct-1 somewhere"
        assert words.contains("cct-1")

    }

    void "process complicated sentence"(){
        given: "a real string"
        String aRealString = "<p>SKN-1 has been shown to be negatively regulated by a variety of upstream signals that include insulin/IGF-1 signaling (IIS), target of rapamycin (TOR) pathway, glycogen synthase kinase-3 (GSK-3), and the proteasome; disruption of these pathways results in different degrees of constitutive activation of SKN-1-dependent genes (<xref ref-type=\"bibr\" rid=\"bib6\">Blackwell <italic>et al.</italic> 2015</xref>). We used RNAi to knockdown different SKN-1 upstream regulators that have previously been shown to result in the activation <italic>gst-4p</italic>::<italic>GFP</italic> (<xref ref-type=\"bibr\" rid=\"bib2\">An <italic>et al.</italic> 2005</xref>; <xref ref-type=\"bibr\" rid=\"bib24\">Kahn <italic>et al.</italic> 2008</xref>; <xref ref-type=\"bibr\" rid=\"bib30\">Li <italic>et al.</italic> 2011</xref>; <xref ref-type=\"bibr\" rid=\"bib37\">Robida-Stubbs <italic>et al.</italic> 2012</xref>). RNAi knockdown of the proteasome alpha subunits (<italic>pas-5</italic> and <italic>pas-6</italic>; components of the 20S proteasome complex), chaperonin-containing TCP-1 (<italic>cct-1</italic>; cytosolic chaperonin), glycogen synthase kinase-3 (<italic>gsk-3</italic>), and Ras-related GTP-binding protein C-1 (<italic>ragc-1</italic>; TORC1 pathway activator) all induced <italic>gst-4p</italic>::<italic>GFP</italic>, and this response was largely suppressed in the <italic>xrep-4(zj26)</italic> mutant with the exception of <italic>pas-5</italic> and <italic>pas-6</italic> (<xref ref-type=\"fig\" rid=\"fig5\">Figure 5A</xref>). SKN-1 has also been shown to be directly negatively regulated by the IIS pathway, and mRNA of <italic>gst-4</italic> is overexpressed in the <italic>daf-2(e1370)</italic> mutant in a <italic>skn-1</italic>-dependent manner (<xref ref-type=\"bibr\" rid=\"bib42\">Tullet <italic>et al.</italic> 2008</xref>). In our experiments, the <italic>daf-2(e1370)</italic> mutant also had significantly elevated <italic>gst-4</italic> mRNA compared to the wild-type, and this was dependent on <italic>xrep-4</italic> (<xref ref-type=\"fig\" rid=\"fig5\">Figure 5B</xref>). Interestingly, knockdown of <italic>xrep-4</italic> had no effect on the long-lived phenotype of the <italic>daf-2(e1370)</italic> mutant (<xref ref-type=\"fig\" rid=\"fig5\">Figure 5C</xref> and Table S4 in <ext-link ext-link-type=\"uri\" xlink:href=\"http://www.genetics.org/lookup/suppl/doi:10.1534/genetics.117.200592/-/DC1/FileS1.pdf\">File S1</ext-link>). Taken together, these results suggest that <italic>xrep-4</italic> is epistatic to <italic>daf-2</italic>, <italic>ragc-1</italic>, and <italic>gsk-3</italic> in regulation of <italic>gst-4</italic>.</p>"
        TreeSet<String> words = new TreeSet<>()


        when: "we process the full text"
        geneticsIngester.consume(words,aRealString)
        println "OUTPUT words: ${words.size()}"
        println words.join("::")

        then: "we should find a cct-1 and other key words"
        assert words.contains("cct-1")
        assert words.contains("ragc-1")
        assert words.contains("daf-2")
        assert words.contains("gsk-3")
        assert words.contains("xrep-4")
        assert words.contains("gst-4")
    }

    void "clean ingest handle punctuation inner"(){

        given: "handling punctuation"
        String string1 = "(cct-1;"

        when: "process string 1"
        def outputString = geneticsIngester.cleanPunctuation(string1)

        then: "output is"
        assert outputString.size()==1
        assert outputString[0]=="cct-1"
    }

    void "clean ingest handle punctuation inner with trailing comma"(){

        given: "handling punctuation"
        String string1 = "daf-2,"

        when: "process string 1"
        def outputString = geneticsIngester.cleanPunctuation(string1)

        then: "output is"
        assert outputString.size()==1
        assert outputString[0]=="daf-2"
    }

    void "handle inner parenthesis"(){

        given: "handling punctuation"
        String string1 = "daf-6(su1006)"

        when: "process string 1"
        def outputString = geneticsIngester.cleanPunctuation(string1)

        then: "output is"
        assert outputString.size()==2
        assert outputString[0]=="daf-6"  // not sure how it does this
        assert outputString[1]=="su1006"  // not sure how it does this
    }

    void "handle inner-inner parenthesis"(){

        given: "handling punctuation"
        String string1 = "pRF4(rol-6(su1006))],"

        when: "process string 1"
        def outputString = geneticsIngester.cleanPunctuation(string1)

        then: "output is"
        assert outputString.size()==3
        assert outputString[0]=="pRF4"  // not sure how it does this
        assert outputString[1]=="rol-6"  // not sure how it does this
        assert outputString[2]=="su1006"  // not sure how it does this
    }

    void "handle a dpy-20"(){

        given: "handle dpy-20"
        String dpy20String = "pMH86(dpy-20(+))]"

        when: "process string 1"
        def outputString = geneticsIngester.cleanPunctuation(dpy20String)
        println "output string is [${outputString}]"

        then: "output is"
        assert outputString.size()==2
        assert outputString[0]=="pMH86"
        assert outputString[1]=="dpy-20"

    }

    void "make sure that the start is correct"(){

        when: "a bad word"
        def matches = GeneticsIngester.badStartPattern.matcher(");").matches()
        then: "matches"
        assert matches
        when: "a good word"
        matches = GeneticsIngester.badStartPattern.matcher("a);").matches()
        then: "matches"
        assert !matches
        when: "a bad word"
        matches = GeneticsIngester.badStartPattern.matcher("*").matches()
        then: "matches"
        assert matches
        when: "a bad word"
        matches = GeneticsIngester.badStartPattern.matcher(")").matches()
        then: "matches"
        assert matches
        when: "a bad word"
        matches = GeneticsIngester.badStartPattern.matcher("-").matches()
        then: "matches"
        assert matches
        when: "a bad word"
        matches = GeneticsIngester.badStartPattern.matcher(";").matches()
        then: "matches"
        assert matches
    }

//    void convertSuperScriptToOtherSuperScript
}
