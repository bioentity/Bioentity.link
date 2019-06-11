package com.insilico.dmc.ingester

import com.insilico.dmc.publication.Content
import com.insilico.dmc.publication.Publication
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import groovy.util.slurpersupport.NodeChildren

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by nathandunn on 4/7/17.
 */
abstract class Ingester {

    abstract Publication extractMetaData(Publication publication)

    static Pattern startsWithAlphanumPattern = Pattern.compile("^\\p{Alnum}")
    static Pattern forwardPattern = Pattern.compile("\\p{Punct}\\p{Alnum}")
    static Pattern strainPattern = Pattern.compile("^(.*)\\((.*).*")
//    static Pattern genoTypePattern = Pattern.compile("^(.*)\\((.*)\\(+\\)\\)")
    static Pattern startsNumberPattern = Pattern.compile("^\\p{Digit}")
    static Pattern badStartPattern = Pattern.compile("^[\\*\\)\\-;].*")


    static Collection<String> cleanPunctuation(String input) {
        // find first and last text / number
        Matcher firstMatch = forwardPattern.matcher(input)
        Matcher startsWithAlnum = startsWithAlphanumPattern.matcher(input)
        Matcher lastMatch = forwardPattern.matcher(input.reverse())
        Matcher endsWithAlnum = startsWithAlphanumPattern.matcher(input.reverse())

        boolean openFound = firstMatch.find()
        boolean lastFound = lastMatch.find()

        Integer firstIndex = startsWithAlnum ? 0 : (openFound ? firstMatch.start() + 1 : 0)
        // count forwards until I don't hit punctuation
        Integer lastIndex = endsWithAlnum ? input.length() : (lastFound ? input.length() - lastMatch.start() - 1 : input.length())

        String outputString = firstIndex < lastIndex ? input.substring(firstIndex, lastIndex) : input


        Matcher strainMatch = strainPattern.matcher(outputString)
        if (strainMatch.find()) {
            def outputArray = []
            for (int i = 1; i <= strainMatch.groupCount(); i++) {
                String item = strainMatch.group(i)
                if (!badStartPattern.matcher(item).matches()) {
                    if (item.contains("(")) {
                        def itemSplit = item.split("\\(")
                        itemSplit.each { itemIter -> outputArray.add(itemIter) }
                    } else {
                        outputArray.add(item)
                    }
                }
            }
            return outputArray
        }
        if (outputString.contains("/")) {
            return outputString.split("/")
        }
        if (outputString.contains("::")) {
            return outputString.split("::")
        }

        return !badStartPattern.matcher(outputString).matches() ? [outputString] : []
    }

    static def consume(Set<String> wordBuffer, Object object) {

        if (object instanceof String) {
//            println "consuming string from object ${object.toString()}"
            consumeString(wordBuffer, (String) object)
        } else if (object instanceof groovy.util.slurpersupport.NodeChild) {
            groovy.util.slurpersupport.NodeChild nodeChild = (NodeChild) object
            nodeChild.childNodes().each { childChildNode ->
                consume(wordBuffer, childChildNode)
            }
            consumeString(wordBuffer, nodeChild.localText())
        } else if (object instanceof groovy.util.slurpersupport.NodeChildren) {
            groovy.util.slurpersupport.NodeChildren nodeChildren = (NodeChildren) object
            nodeChildren.childNodes().each { childChildNode ->
                consume(wordBuffer, childChildNode)
            }
            consumeString(wordBuffer, nodeChildren.text())
        } else if (object instanceof groovy.util.slurpersupport.Node) {
            groovy.util.slurpersupport.Node node = (groovy.util.slurpersupport.Node) object
            node.childNodes().each { def nodeChild ->
                consume(wordBuffer, nodeChild)
            }
            consumeString(wordBuffer, node.localText())
        } else {
            println "Not sure how to handle object of type so coercing to string ${object.getClass().toString()}"
            consumeString(wordBuffer, object.toString())
        }
    }

    static def consumeString(Set<String> wordBuffer, List<String> textArray) {
        textArray.each { text ->
            consumeString(wordBuffer, text)
        }
    }

    static def consumeString(Set<String> wordBuffer, String text) {

//        def interestedList = ["myo-2","tph-1"]

        text.split(' ').each { splitWord ->

            // ignore if it starts with a number
            if (startsNumberPattern.matcher(splitWord).matches()) {
                return
            }
            if (splitWord.endsWith("%")) {
                return
            }

            // find the last "> and the first "</
//            int openTag = it.in(">")
//            def debugString = interestedList.findAll() { inner ->
//                splitWord.contains(inner)
//            }
//            debugString.each { innerDebug ->
//                println "handled 1 ${innerDebug}"
//                println "handled 2 ${cleanPunctuation(innerDebug)}"
//                println "handled 2a ${debugString}"
//                println "handled 2b ${splitWord}"
//                println "handled 2c ${cleanPunctuation(splitWord)}"
//            }
            int openTag = splitWord.indexOf(">")
            if (openTag >= 0) {
                int closeTag = splitWord.indexOf("</")
                int currentIndex = 0
                while (splitWord.indexOf(">", currentIndex + 1) < closeTag) {
                    openTag = splitWord.indexOf(">", currentIndex + 1)
                    currentIndex = openTag + 1
                }
                if (openTag > 0 && openTag < closeTag) {
                    String substring = splitWord.substring(openTag + 1, closeTag)

//                    debugString.each {
//                        println "handled 3: ${substring}"
//                        println "handled 4: ${cleanPunctuation(substring)}"
//                        println "handled 4a: ${debugString}"
//                    }

                    wordBuffer.addAll(cleanPunctuation(substring))
                }
                return
            }
            if (splitWord.contains("<") || splitWord.contains(">")) {
                println "returns the / for ${splitWord}"
                return
            }
//            debugString.each { innerDebug ->
//                println "handled 5: ${innerDebug}"
//                println "handled 5a: ${innerDebug.toString()}"
//                println "handled 6: ${cleanPunctuation(innerDebug)}"
//                println "handled 6a: ${debugString}"
//                println "handled 6b: ${cleanPunctuation(innerDebug).toString()}"
//            }
            cleanPunctuation(splitWord).each { word ->
                String wordToAdd = word.toString()
                if (wordToAdd.contains("myo-2")) {
                    println "class: ${word.getClass().toString()}"
                    println "adding word ${word} -> ${wordToAdd}"
                }
                wordBuffer.add(wordToAdd)
            }
//            wordBuffer.addAll(cleanPunctuation(it))
//            debugString.each { innerDebug ->
//                println "contains 5: ${wordBuffer.contains(innerDebug)}"
//                def found = wordBuffer.findAll() {
//                    innerDebug.contains("myo-2")
//                }
//                println "contains 6 / found: ${found}"
//            }
        }

        return wordBuffer
    }

    Set<String> extractIndexableContent(Publication publication) {
        def parser = new XmlSlurper(false, true, true)
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Content content = publication.originalData

        // convert "geneterm<sup id=''>superscriptterm</sup>::otherterm -> geneterm^superscriptterm::otherterm
        GPathResult article = parser.parseText(content.value)

        Set<String> wordBuffer = new HashSet<>()

        // process from matter
        def front = article.front
        def articleMeta = front."article-meta"
        def title = articleMeta['title-group']['article-title'].text()
        consume(wordBuffer, title)
        def abstractText = articleMeta['abstract'].text()
        consume(wordBuffer, abstractText)

        // process body
        def body = article.body

        // process leading paragraph
        body.p.each { NodeChild nodeChild ->
            consume(wordBuffer, nodeChild)
        }

        evaluateSection(body.sec, wordBuffer)
//        wordBuffer.contains("myo-2")

        wordBuffer.remove(null)
        def finalWordBuffer = wordBuffer.findAll() {
            it && it.count("(") == it.count(")")
        }

        return wordBuffer
    }

    def evaluateSection(def sec, HashSet<String> wordBuffer) {
        sec.each { NodeChild section ->
            if (section.title) {
                consume(wordBuffer, section.title)
            }
            section.p.each { NodeChild p ->
                consume(wordBuffer, p)
            }
            section.sec.each { NodeChild subSection ->
                if (subSection.title) {
                    consume(wordBuffer, subSection.title)
                }
                subSection.p.each { NodeChild p ->
                    consume(wordBuffer, p)
                }
                subSection.sec.each { NodeChild subSubSection ->
                    evaluateSection(subSubSection, wordBuffer)
                }
            }
        }
    }

//        xmlData = xmlData.findAll("<sec id=\"(.+?)\" sec-type=\"(.+?)\">")
//    final Pattern pattern = Pattern.compile("<sec id=\"(.+?)\" sec-type=\"(.+?)\">", Pattern.DOTALL)
    static String convertAllSec(String input) {
        Pattern p = Pattern.compile("<sec id=\"(.+?)\" sec-type=\"(.+?)\">")
        Matcher m = p.matcher(input)
        return m.find() ? m.replaceAll($/<sec id=\"$1\" sec-type=\"$2\"><sec-comment id=\"$1\" sec-type=\"$2\"/>/$) : input

    }

    static String revertAllSec(String input) {
        Pattern p = Pattern.compile("<sec id=\"(.+?)\"><sec-comment id=\"(.+?)\" sec-type=\"(.+?)\"/>")
        Matcher m = p.matcher(input)
        return m.find() ? m.replaceAll($/<sec id=\"$2\" sec-type=\"$3\">/$) : input
    }
}