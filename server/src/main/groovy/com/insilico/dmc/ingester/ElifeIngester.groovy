package com.insilico.dmc.ingester

import com.insilico.dmc.publication.*
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild

/**
 * Created by nathandunn on 4/7/17.
 *
 * Use the XmlSlurper as reference:
 * http://groovy-lang.org/processing-xml.html
 */
class ElifeIngester extends Ingester {


    @Override
    Publication extractMetaData(Publication publication){
        def parser = new XmlSlurper(false, true, true)
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        def article = parser.parseText(publication.originalData.value)
        def front = article.front
        def articleMeta = front."article-meta"
        def articleIds = articleMeta."article-id"
        articleIds.each { NodeChild  nodeChild->
            if(nodeChild.attributes().get('pub-id-type')=='doi'){
                publication.doi = nodeChild
            }
        }
        GPathResult history = articleMeta['history']
        history['date'].each {
            Calendar calendar = GregorianCalendar.instance
            calendar.set(Calendar.MONTH, Integer.valueOf(it.month.text()))
            calendar.set(Calendar.DATE, Integer.valueOf(it.day.text()))
            calendar.set(Calendar.YEAR, Integer.valueOf(it.year.text()))
            Date date = calendar.getTime()
            println "interpreted: ${it} as date ${date}"

            String dataType = it.@"date-type"
            println "dataType found ${dataType}"
            println "elife date ${it}"
            println "dataType found ${dataType}"
            println "date ${date}"
//            Date date = it
//            String dataType = it.@['date-type']
            if(dataType=='accepted'){
                publication.accepted = date
            }
            else
            if(dataType=='received'){
                publication.received = date
            }
            else{
                println "unrecognized date-type: ${dataType}"
            }
        }
        publication.ingested = new Date()

        def title = articleMeta['title-group']['article-title'].text()
        println "stting title to ${title}"
        publication.title  = title
        println "pub title is not ${publication.title}"
        publication.save(flush:true)
        println "pub is valid ${publication.validate()}"
        return publication
    }

//    Publication ingestFile(Publication publication, String rawData) {
//
////        println "gene ingested pub"
////        PreBody preBody = new PreBody(
////                publication: publication
////        )
////        Body body = new Body(
////                publication: publication
////        )
////
////        def parser = new XmlSlurper(false, true, true)
////        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
////        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
////
////        def article = parser.parseText(rawData)
////        def front = article.front
////        def articleMeta = front."article-meta"
////        def articleIds = articleMeta."article-id"
////        articleIds.each { NodeChild  nodeChild->
////            if(nodeChild.attributes().get('pub-id-type')=='doi'){
////                publication.doi = nodeChild
////            }
////        }
////
////        def title = articleMeta['title-group']['article-title'].text()
////        def abstractText = articleMeta['abstract'].text()
////        preBody.titleText = title
////        preBody.abstractText = abstractText
////        preBody.save(failOnError: true)
////        // pull anything in the body starting with <p>
////        def bodyContent = article.body
////        int sectionOrder = 0
////
////        // for each p, prefixing the section section
////        // handle leading paragraph
////        def leadingParagraphs = bodyContent.p
////        if (leadingParagraphs) {
////            // this section does not have a title
////            Section section = new Section(
////                    body: body
////                    ,order: sectionOrder++
////            )
////
////            int index = 0
////            for (paragraph in leadingParagraphs) {
////                BodyLine bodyLine = new BodyLine(
////                        lineNumber: index
////                        , lineText: paragraph as String
////                        , section: section
////                ).save(failOnError: true)
////                section.addToLines(bodyLine)
////                ++index
////            }
////            section.save(flush:true)
////            body.addToSections(section)
////        }
////
////        def sectionContentList = bodyContent.sec
////        for (sectionContent in sectionContentList) {
////            Section section = new Section(
////                    title: sectionContent.fileName as String
////                    ,body: body
////                    ,order: sectionOrder++
////            ).save(failOnError: true)
////            int index = 0
////            for (paragraph in sectionContent.p) {
////                BodyLine bodyLine = new BodyLine(
////                        lineNumber: index,
////                        lineText: paragraph as String,
////                        section: section
////                ).save(faileOnError: true)
////                section.addToLines(bodyLine)
////                ++index
////            }
////            section.save(failOnError: true)
////            body.addToSections(section)
////        }
////
////        body.save(faileOnError: true)
////        publication.preBody = preBody
////        publication.body = body
//
//        publication.save(failOnError: true)
//
//
//        return publication
//
//    }

}
