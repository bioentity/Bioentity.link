package com.insilico.dmc.ingester

import com.insilico.dmc.publication.Content
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.user.Author
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild

/**
 * Created by nathandunn on 4/7/17.
 *
 * Use the XmlSlurper as reference:
 * http://groovy-lang.org/processing-xml.html
 */
class GeneticsIngester extends Ingester {

    Publication extractMetaData(Publication publication) {
        def parser = new XmlSlurper(false, true, true)
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        GPathResult article = parser.parseText(publication.originalData.value)
        def front = article.front
        def articleMeta = front."article-meta"
        def articleIds = articleMeta."article-id"
        articleIds.each { NodeChild nodeChild ->
            if (nodeChild.attributes().get('pub-id-type') == 'doi') {
                publication.doi = nodeChild
            }
        }
//        println "history ${history}"
//        println "history date ${history['date']}"
        GPathResult history = articleMeta['history']
        history['date'].each {
//            println "it: ${it}"
//            println "it month: ${it.month}"
//            println "it day: ${it.day}"
//            println "it year: ${it.year}"
            Calendar calendar = GregorianCalendar.instance
            calendar.set(Calendar.MONTH, Integer.valueOf(it.month.text()))
            calendar.set(Calendar.DATE, Integer.valueOf(it.day.text()))
            calendar.set(Calendar.YEAR, Integer.valueOf(it.year.text()))
            Date date = calendar.getTime()
            println "interpreted: ${it} as date ${date}"
            String dataType = it.@"date-type"
            println "dataType found ${dataType}"
            if (dataType == 'accepted') {
                publication.accepted = date
                println "publicaiton accepted ${publication.accepted}"
            } else if (dataType == 'received') {
                publication.received = date
                println "publicaiton received ${publication.received}"
            } else {
                println "unrecognized date-type: ${dataType}"
            }
        }
        publication.ingested = new Date()

        String title = articleMeta['title-group']['article-title'].text()
        publication.title = title

        publication.save(flush: true)

        GPathResult contribGroup = articleMeta['contrib-group']
        contribGroup['contrib'].each {
            if (it.@'contrib-type' == 'author') {
                def author = new Author()
                author.firstName = it.name.'given-names'.text()
                author.lastName = it.name.surname.text()
                println it.name.'given-names'.text()
                println it.name.surname.text()
                author.save(flush: true)
                try {
                    publication.addToAuthors(author)
                } catch(e) {
                    println "Cannot add author ${author.firstName} ${author.lastName} to ${title}"
                }
            }
        }

        publication.save(flush: true)
        return publication
    }

//    Publication ingestFile(Publication publication, String rawData) {
//
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
////
////        Integer sectionOrder = 0
////        // for each p, prefixing the section section
////        // handle leading paragraph
////        def leadingParagraphs = bodyContent.p
////        if (leadingParagraphs) {
////            // this section does not have a title
////            Section section = new Section(
////                    body: body
////                    ,order: sectionOrder
////            ).save(failOnError: true)
////            ++sectionOrder
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
////            section.save()
////            body.addToSections(section)
////        }
////
////        def sectionContentList = bodyContent.sec
////        for (sectionContent in sectionContentList) {
////            // the section title is under sectionContent.node.children(name==title).children[0]
////            Section section = new Section(
////                    title: sectionContent.fileName as String,
////                    body: body,
////                    order: sectionOrder
////            ).save(failOnError: true)
////            ++sectionOrder
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
