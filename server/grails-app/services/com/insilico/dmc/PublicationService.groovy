package com.insilico.dmc

import com.insilico.dmc.index.ContentWordIndex
import com.insilico.dmc.ingester.ElifeIngester
import com.insilico.dmc.ingester.GeneticsIngester
import com.insilico.dmc.ingester.Ingester
import com.insilico.dmc.ingester.InternalDtdResolver


import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.lexicon.StopWord
import com.insilico.dmc.markup.KeyWordSet
import com.insilico.dmc.publication.Content
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.publication.PublicationStatusEnum
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import grails.util.Environment
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

@Transactional
class PublicationService {

    private Ingester elifeIngester = new ElifeIngester()
    private Ingester geneticsIngester = new GeneticsIngester()
//    private Ingester geneticsOldIngester = new GeneticsOldIngester()
    def githubService

    def ingestPublication(File file, Publication publication, Ingester ingester) {
        try {
            String text = file.text
            publication.originalData = new Content(value: text).save(flush: true)
            publication.exportedData = new Content(value: text).save(flush: true)
            ingester.extractMetaData(publication)
            publication.status = PublicationStatusEnum.INGESTED
        } catch (e) {
            println "fialed to ingestPubFile ${file.name} - ${e}"
            publication.status = PublicationStatusEnum.INGESTION_ERROR
        }
        githubService.synchronizeLabelStatus(publication)
        publication.save(flush: true, failOnError: true)
        return publication
    }


    Publication ingestPublicationContent(String fileName, String fileContent) {
        println "ingesting file ${fileName} ${fileContent?.size()}"

        String journal
        if (fileContent.indexOf("elife</journal-id>") != -1) {
            println "journal is elife, so USING: ${fileName}"
            journal = "elife"
        } else if (fileContent.toLowerCase().indexOf("genetics</journal-id>") != -1) {
            println "journal is genetics, so USING: ${fileName}"
            journal = "genetics"
            fileContent = fileContent.replaceAll(/(<\?RFR:.+\?>)/) { all, content -> "<!--${content}-->" }
            fileContent = fileContent.replaceAll(/(<\?RFL:.+\?>)/) { all, content -> "<!--${content}-->" }
            fileContent = fileContent.replaceAll("custom-meta-wrap", "custom-meta-group")

        } else if (fileContent.toLowerCase().indexOf("genetics</publisher>") != -1) {
            // Old Genetics format, return null for now
            println "publisher is old genetics: ${fileName}"
            journal = "genetics old"
            fileContent = fileContent.replaceAll('<document xmlns:html="HTML NS">', '<!--<document xmlns:html="HTML NS">--><document>')
//          return null
        } else if (fileContent.indexOf("&nbsp;") != -1) {
            println "journal is genetics and contains &nbsp; so ignoring: ${fileName}"
            journal = "genetics"
            // XML cannot contain nbsp
            return null
        } else if (fileContent.toLowerCase().indexOf("<journal-title>micropublication") != -1) {
            println "This is a micropublication, trying to see if elife ingester works"
            journal = "elife"
        } else {
            // TODO: Return error if unable to determine journal
            println "Unable to determine journal so ignoring: ${fileName}"
            return null
        }

        println "input count: ${Publication.count()}"

        Publication publication = new Publication(
                fileName: fileName,
                journal: journal,
                status: PublicationStatusEnum.INGESTED,
                lastEdited: new Date(),
                originalData: new Content(value: fileContent).save(failOnError: true),
                exportedData: new Content(value: fileContent).save(failOnError: true)
        )

        if (journal == "elife") {
            publication = elifeIngester.extractMetaData(publication)
        } else if (journal == "genetics") {
            publication = geneticsIngester.extractMetaData(publication)
        }// else if (journal == "genetics old") {
        //	publication = geneticsOldIngester.extractMetaData(publication)
        //}
        String doi = publication.doi

        if (Publication.countByDoi(doi) > 1) {
            throw new RuntimeException("Doi ${doi} already exists in database.")
        }

        println "pre-save has: ${Publication.countByDoi(doi)}"
        println "trying to save pub with title ${publication.title}"
        println "and doi ${publication.doi}"
        publication.save(flush: true, failOnError: true)
        println "final save ${publication?.originalData?.value?.size()}"
        println "is valid pub: ${publication.validate()}"


        println "output count: ${Publication.count()}"

        println "post-save has: ${Publication.countByDoi(doi)}"


        return publication
    }

    Publication ingestPubFile(File file) {
        if (file.name.toLowerCase().endsWith(".xml")) {
            println "ingesting: ${file.name}"
            try {
                return ingestPublicationContent(file.name, file.text)
            } catch (e) {
                println "error on ingestPubFile ${e} for pub ${file.name}"
            }
        } else {
            println "do not ingestPubFile: ${file.name}"
        }
        return null
    }

    void ingestList(File file) {
        System.out.println("ingesting list from: " + file.absolutePath);
        File[] children = file.listFiles()
        for (File child : children) {
            println "processing child: " + child.absolutePath
            if (child.isDirectory()) {
                ingestList(child)
            } else {
                try {
                    ingestPubFile(child)
                } catch (e) {
                    println "error ingesting file ${e} -> ${child.name}"
                }
            }
        }
    }

    def removeAllPubs() {
        Publication.deleteAll(Publication.all)
    }

    def deletePub(Publication publication) {
        // First, delete markups
        int pubDeleteCount = 0
        pubDeleteCount += Publication.executeUpdate("MATCH (p:Publication)-[r]-(m:Markup)-[s]-() WHERE p.doi = {doi} DELETE s,m,r", [doi: publication.doi])

        pubDeleteCount += Publication.executeUpdate("MATCH (p:Publication)-[r]-(kws:KeyWordSet) WHERE p.doi = {doi} DELETE r", [doi: publication.doi])

        pubDeleteCount += Publication.executeUpdate("MATCH (p:Publication)-[r]-(c:CurationActivity)-[q]-() WHERE p.doi = {doi} DELETE r,c,q", [doi: publication.doi])
        // Then, delete the pub
        pubDeleteCount += Publication.executeUpdate("MATCH (p:Publication)-[r]-(c:Content)-[q]-(cwi:ContentWordIndex) WHERE p.doi = {doi} DELETE r,c,q", [doi: publication.doi])

        pubDeleteCount += Publication.executeUpdate("MATCH (p:Publication)-[r]-(c:Content) WHERE p.doi = {doi} DELETE r,c", [doi: publication.doi])

        pubDeleteCount += Publication.executeUpdate("MATCH (p:Publication) WHERE p.doi = {doi} DELETE p", [doi: publication.doi])

        return pubDeleteCount
    }

    List<String> getIndices(Publication publication) {
        return ContentWordIndex.executeQuery("MATCH (p:Publication)--(c:Content)--(i:ContentWordIndex) where p.fileName = {fileName} return distinct(i.word) order by i.word asc", [fileName: publication.fileName])
    }

    int clearIndices(Publication publication) {
        return ContentWordIndex.executeUpdate("MATCH (p:Publication)--(c:Content)-[r]-(i:ContentWordIndex) where p.fileName = {fileName} delete r", [fileName: publication.fileName])
    }

    Integer getPubCountFromString(String search, String status, String species, max, offset) {


        if (search && search.isInteger()) {
            PublicationStatusEnum pubStatus = PublicationStatusEnum.getEnumForString(status)
            if (pubStatus) {
                return Publication.countByIdAndStatus(search, pubStatus)
            } else {
                return Publication.countById(search)
            }
        }
        Set<String> uniqueKeys = new HashSet()
        search = search ?: ""
        switch (status) {
            case PublicationStatusEnum.INGESTED.name():
            case PublicationStatusEnum.MARKED_UP.name():
            case PublicationStatusEnum.CURATING.name():
            case PublicationStatusEnum.CURATOR_FINISHED.name():
                PublicationStatusEnum pubStatus = PublicationStatusEnum.getEnumForString(status)
                uniqueKeys.addAll(Publication.executeQuery("MATCH (p:Publication) where lower(p.title) =~ {search} and p.status = {pubStatus} RETURN distinct p.fileName", [search: '.*' + search.toLowerCase() + '.*', pubStatus: status]))
                uniqueKeys.addAll(Publication.executeQuery("MATCH (p:Publication) where lower(p.title) =~ {search} and p.status = {pubStatus} RETURN distinct p.fileName", [search: '.*' + search.toLowerCase() + '.*', pubStatus: status]))
                uniqueKeys.addAll(Publication.executeQuery("MATCH (p:Publication) where lower(p.doi) =~ {search} and p.status = {pubStatus} RETURN distinct p.fileName", [search: '.*' + search.toLowerCase() + '.*', pubStatus: status]))
                break
            case 'OneCuratorStarted':
                uniqueKeys.addAll(Publication.executeQuery("MATCH (ca:CurationActivity)--(p:Publication) where ca.curationStatus='STARTED' RETURN distinct p.fileName ", [max: max, offset: offset, sort: "lastEdited", order: "desc"]))
                break
            case 'OneCuratorFinished':
                uniqueKeys.addAll(Publication.executeQuery("MATCH (ca:CurationActivity)--(p:Publication) where ca.curationStatus='FINISHED' RETURN distinct p.fileName ", [max: max, offset: offset, sort: "lastEdited", order: "desc"]))
                break
            default:
                uniqueKeys.addAll(Publication.executeQuery("MATCH (p:Publication) where lower(p.title) =~ {search} RETURN distinct p.fileName", [search: '.*' + search.toLowerCase() + '.*']))
                uniqueKeys.addAll(Publication.executeQuery("MATCH (p:Publication) where lower(p.title) =~ {search} RETURN distinct p.fileName", [search: '.*' + search.toLowerCase() + '.*']))
                uniqueKeys.addAll(Publication.executeQuery("MATCH (p:Publication) where lower(p.doi) =~ {search} RETURN distinct p.fileName", [search: '.*' + search.toLowerCase() + '.*']))
                break

        }

        if (species) {
            Set<String> speciesKeys = new HashSet()
            for (String fileName in uniqueKeys) {
                Publication pub = Publication.findByFileName(fileName)
                for (KeyWordSet kws in pub.markupSources) {
                    for (LexiconSource ls in kws.sources) {
                        if (species == ls.species.name) {
                            speciesKeys.add(fileName)
                        }
                    }
                }

            }
            uniqueKeys = speciesKeys
        }

        return uniqueKeys.size()
    }

    /**
     * https://stackoverflow.com/a/40099983/1739366
     * @param linkString
     */
    def validateLink(String linkString, String idString) {
        try {
            def response = new URL(linkString).openStream()

            Scanner scanner = new Scanner(response);
            String responseBody = scanner.useDelimiter("\\A").next();
            String titleString = responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"))

            Boolean hasTitle = titleString.toLowerCase().contains(idString.toLowerCase())
            println "title ${titleString.toLowerCase()} contains ${idString.toLowerCase()} -> ${hasTitle}"

            return hasTitle
        } catch (e) {
            println "error loading link string ${linkString} ${e}"
            return false
        }
    }

    Publication validateLinks(Publication publication) {
        JSONObject jsonObject = new JSONObject()
        jsonObject.validLinks = new JSONArray()
        jsonObject.invalidLinks = new JSONArray()
        // 1. get markups for lexica?
        def linkMap = [:]
        def pubLinks = Publication.executeQuery("MATCH (p:Publication)--(m:Markup)--(l:Lexicon)--(ls:LexiconSource) where p.doi={doi} RETURN l.externalModId as id,l.publicName as name,ls.urlConstructor as url", [doi: publication.doi])
        println "validating ${pubLinks.size()} links"

        pubLinks.each {
            String idString = it["id"]
            String urlString = it["url"]
            String name = it["name"]
            def linkString = urlString.replace("@@ID@@", idString)
            linkMap.put(linkString, name)
        }
        println "validating ${linkMap.size()} links"

        linkMap.each { it ->
            JSONObject linkItem = new JSONObject(
                    name: it.value,
                    link: it.key
            )
            if (validateLink(it.key as String, it.value as String)) {
                jsonObject.validLinks.add(linkItem)
            } else {
                jsonObject.invalidLinks.add(linkItem)
            }
        }
        jsonObject.checkDate = (new Date()).toString()

//        println "saving pub with ${jsonObject}"
        publication.linkValidationJson = jsonObject
        publication.save(flush: true, failOnError: true)

//        println "SAVED pub with ${Publication.findByDoi(publication.doi).linkValidationJson}"

        String messageString = "# Link validation report\n"
        messageString += "## Validated Links\n"
        jsonObject.validLinks.each {
            messageString += "- <a href=${it.link}>${it.name}</a>\n"
        }
        messageString += "## Invalidated Links\n"
        jsonObject.invalidLinks.each {
            messageString += "- <a href=${it.link}>${it.name}</a>\n"
        }
        println "adding comment ${messageString}"
        githubService.addComment(publication, messageString, true)
        return publication
    }

    List<Publication> getPubsFromString(String search, String status, String species, max, offset) {


        def pubById = null
        if (search && search.isInteger()) {
            PublicationStatusEnum pubStatus = PublicationStatusEnum.getEnumForString(status)
            if (pubStatus) {
                pubById = Publication.findByIdAndStatus(search as Integer, pubStatus, [max: max, offset: offset])
            } else {
                pubById = Publication.findById(search as Integer, [max: max, offset: offset])
            }
        }

        if (pubById) {
            return [pubById]
        }

        List<List<Publication>> pubsArray = []
        search = search ?: ""

        switch (status) {
            case PublicationStatusEnum.INGESTED.name():
            case PublicationStatusEnum.MARKED_UP.name():
            case PublicationStatusEnum.CURATING.name():
            case PublicationStatusEnum.CURATOR_FINISHED.name():
                PublicationStatusEnum pubStatus = PublicationStatusEnum.getEnumForString(status)
                if (species) {
                    pubsArray.add(Publication.executeQuery("MATCH (s:Species)--(ls:LexiconSource)--(kws:KeyWordSet)--(pub:Publication) WHERE s.name={species} AND pub.status={pubStatus} RETURN pub", [species: species, pubStatus: pubStatus.name(), max: max, offset: offset, sort: "lastEdited", order: "desc"]).collect() {
                        return it as Publication
                    })
                } else {
                    pubsArray.add(Publication.findAllByTitleIlikeAndStatus('%' + search + '%', pubStatus, [max: max, offset: offset, sort: "lastEdited", order: "desc"]))
                    pubsArray.add(Publication.findAllByFileNameIlikeAndStatus('%' + search + '%', pubStatus, [max: max, offset: offset, sort: "lastEdited", order: "desc"]))
                    pubsArray.add(Publication.findAllByDoiIlikeAndStatus('%' + search + '%', pubStatus, [max: max, offset: offset, sort: "lastEdited", order: "desc"]))
                }
                break
            case 'OneCuratorStarted':
                // select where at least one person is "STARTED" curating
                if (species) {
                    pubsArray.add(Publication.executeQuery("MATCH (ca:CurationActivity)--(p:Publication)--(kws:KeyWordSet)--(ls:LexiconSource)--(s:Species) where ca.curationStatus='STARTED' AND s.name={species} RETURN distinct p ", [species: species, max: max, offset: offset, sort: "lastEdited", order: "desc"]).collect() {
                        return it as Publication
                    })
                } else {

                    pubsArray.add(Publication.executeQuery("MATCH (ca:CurationActivity)--(p:Publication) where ca.curationStatus='STARTED' RETURN distinct p ", [max: max, offset: offset, sort: "lastEdited", order: "desc"]).collect() {
                        return it as Publication
                    })
                }
                break
            case 'OneCuratorFinished':
                // select where at least every person is "FINISHED" curating
                if (species) {
                    pubsArray.add(Publication.executeQuery("MATCH (ca:CurationActivity)--(p:Publication)--(kws:KeyWordSet)--(ls:LexiconSource)--(s:Species) where ca.curationStatus='FINISHED' AND s.name={species} RETURN distinct p ", [species: species, max: max, offset: offset, sort: "lastEdited", order: "desc"]).collect() {
                        return it as Publication
                    })
                } else {
                    pubsArray.add(Publication.executeQuery("MATCH (ca:CurationActivity)--(p:Publication) where ca.curationStatus='FINISHED' RETURN distinct p ", [max: max, offset: offset, sort: "lastEdited", order: "desc"]).collect() {
                        return it as Publication
                    })
                }
                break
            default:
                if (species) {
                    pubsArray.add(Publication.executeQuery("MATCH (s:Species)--(ls:LexiconSource)--(kws:KeyWordSet)--(pub:Publication) WHERE s.name={species} RETURN pub", [species: species, max: max, offset: offset, sort: "lastEdited", order: "desc"]).collect() {
                        return it as Publication
                    })

                } else {
                    pubsArray.add(Publication.findAllByTitleIlike('%' + search + '%', [max: max, offset: offset, sort: "ingested", order: "desc"]))
                    pubsArray.add(Publication.findAllByFileNameIlike('%' + search + '%', [max: max, offset: offset, sort: "ingested", order: "desc"]))
                    pubsArray.add(Publication.findAllByDoiIlike('%' + search + '%', [max: max, offset: offset, sort: "ingested", order: "desc"]))
                }
                break
        }


        def publications = pubsArray.size() > 0 ? pubsArray.sort() { a, b -> a.size() <=> b.size() }.last() : []
        return new ArrayList(publications)
    }

    Publication createIndices(Publication publication) {
        if (publication.originalData.indices) return publication

        Set<String> content = []
        if (publication.journal == "elife") {
            content = elifeIngester.extractIndexableContent(publication)
        } else if (publication.journal == "genetics") {
            content = geneticsIngester.extractIndexableContent(publication)
        }
        println "initial size: " + content.size()
        println "contains word 1" + content.contains("myo-2")
        content = content - StopWord.all.value
        println "removed stop words: " + content.size()
        println "contains word 2" + content.contains("myo-2")

        content = filterTerms(content)
        println "filtered terms: " + content.size()
        println "contains word 3" + content.contains("myo-2")
        // find contentWordIndex
        def existingWordIndices = ContentWordIndex.findAllByWordInList(content as List)
        def existingWords = existingWordIndices.word
        def newWords = content - existingWords
        println "contains word 4" + existingWords.contains("myo-2")
        println "contains word 5 - new " + newWords.contains("myo-2")
//        def newWordIndices = ContentWordIndex.findAllByWordNotInList(content as List)

        Set<ContentWordIndex> contentWordIndexSet = []
        for (String word in newWords) {
            ContentWordIndex contentWordIndex = new ContentWordIndex(
                    word: word
            ).save()
            contentWordIndexSet.add(contentWordIndex)
        }

        long startTime = System.currentTimeMillis()
        int addedNew = ContentWordIndex.executeUpdate("MATCH (p:Publication)-[r:ORIGINALDATA]-(c:Content),(cwi:ContentWordIndex) where p.fileName = {fileName} and cwi.word in {words} create (c)-[:CONTENTS]->(cwi) RETURN p,length(c.value),cwi.word",
                [fileName: publication.fileName, words: content])
        long endTime = System.currentTimeMillis()

        println "added relationships ${addedNew} -> ${endTime - startTime}"

        return publication
    }

    Set<String> filterTerms(Set<String> strings) {
        Set<String> newContent = new HashSet<>()
        for (String s in strings) {
            String validatedTerm = validTerm(s)
            if (validatedTerm) {
                newContent.add(validatedTerm)
            }
        }
        return newContent
    }

    String validTerm(String s) {
        s = s.trim()
        if (s.endsWith("),") || s.endsWith(").") || s.length() < 2) {
            return null
        }

        if (s.startsWith("(") && s.endsWith(")")) {
            return s.length() > 4 ? s.substring(1, s.length() - 2) : null
        }

        if (s.startsWith("(") || s.endsWith(")")) {
            return null
        }

        if (s.endsWith(",")) {
            return s.length() > 3 ? s.substring(0, s.length() - 2) : null
        }
        if (s.endsWith(".")) {
            return s.length() > 3 ? s.substring(0, s.length() - 2) : null
        }


        return s
    }


    @NotTransactional
    String filterXml(String xmlData, String pubType, String originalXml) {
        xmlData = xmlData.replaceAll(/ xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\" id=\"unsupported-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"unsupported-\d+\" xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "")
        xmlData = xmlData.replaceAll(/ xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\" id=\"unsupported-inline-\d+\"/, "")
        xmlData = xmlData.replaceAll(/table-wrap xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "table-wrap")
        xmlData = xmlData.replaceAll(/fig-group xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "fig-group")
        xmlData = xmlData.replaceAll(/media xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "media")
        xmlData = xmlData.replaceAll(/sub-article xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "sub-article")
        xmlData = xmlData.replaceAll(/ack xmlns=\"http:\/\/www.w3.org\/1999\/xhtml\"/, "ack")
        xmlData = xmlData.replaceAll(/underline xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "underline")
        xmlData = xmlData.replaceAll(/fn xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "fn")
        xmlData = xmlData.replaceAll(/fn-group xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "fn-group")
        xmlData = xmlData.replaceAll(/related-article xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "related-article")
        xmlData = xmlData.replaceAll(/journal-id xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "journal-id")
        xmlData = xmlData.replaceAll(/journal-title xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "journal-title")
        xmlData = xmlData.replaceAll(/abbrev-journal-title xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "abbrev-journal-title")
        xmlData = xmlData.replaceAll(/issn xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "issn")
        xmlData = xmlData.replaceAll(/publisher xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "publisher")
        xmlData = xmlData.replaceAll(/corresp xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "corresp")
        xmlData = xmlData.replaceAll(/month xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "month")
        xmlData = xmlData.replaceAll(/year xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "year")
        xmlData = xmlData.replaceAll(/date xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "date")
        xmlData = xmlData.replaceAll(/copyright-statement xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "copyright-statement")
        xmlData = xmlData.replaceAll(/copyright-year xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "copyright-year")
        xmlData = xmlData.replaceAll(/<p xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "<p")
        xmlData = xmlData.replaceAll(/kwd xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "kwd")
        xmlData = xmlData.replaceAll(/fig-count xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "fig-count")
        xmlData = xmlData.replaceAll(/title xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "title")
        xmlData = xmlData.replaceAll(/list xmlns=\"http:\/\/www\.w3\.org\/1999\/xhtml\"/, "list")
        xmlData = xmlData.replaceAll(/ext-link-type=\"doi\" xmlns:xlink=\"http:\/\/www\.w3\.org\/1999\/xlink\"/, 'ext-link-type="doi"')
        // Remove ids
        xmlData = xmlData.replaceAll(/ id=\"italic-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"label-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"subscript-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"superscript-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"article\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"front\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"article-meta\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"title-group\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"body\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"back\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"bold-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"article-title-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"paragraph-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"xref-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"graphic-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"contrib-group-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"contrib-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"caption-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"ref-list-\d+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"title-[\w\d]+\"/, "")
        xmlData = xmlData.replaceAll(/ id=\"ext-link-[\w\d]+\"/, "")

        // Fix count elements
        xmlData = xmlData.replaceAll(/(count=\"\d+\")>/, '$1/>')
        xmlData = xmlData.replaceAll(/<\/.+-count>/, "")
        // Text above table, fig and media should be in caption
        xmlData = xmlData.replaceAll(/(?s)<table-wrap (.+?)<\/label>(.+?)<table /, '<table-wrap $1</label><caption>$2</caption><table ')
        // Remove weird unicode
        xmlData = xmlData.replaceAll(/\u200B/, "")

        // Convert to unicode
        xmlData = xmlData.replaceAll("&amp;lt;", "&#x003C;")
        xmlData = xmlData.replaceAll("&amp;gt;", "&#x003E;")
        xmlData = xmlData.replaceAll("&lt;", "&#x003C;")
        xmlData = xmlData.replaceAll("&gt;", "&#x003E;")

        // Remove space in closing tags
        xmlData = xmlData.replaceAll(" />", "/>")

        xmlData = xmlData.replaceAll(/(?s)<\/front-stub>(.+?)<\/sub-article>/, '</front-stub><body>$1</body></sub-article>')

        // Different DTDs between elife and GSA
        if (pubType.contains("elife")) {
            xmlData = xmlData.replaceAll(/(?s)<fig (.+?)<\/label>(.+?)<graphic /, '<fig $1</label><caption>$2</caption><graphic ')
            xmlData = xmlData.replaceAll(/(?s)<media (.+?)<\/label>(.+?)<\/media>/, '<media $1</label><caption>$2</caption></media>')
            xmlData = xmlData.replaceAll(/(?s)<supplementary-material (.+?)<\/label>(.+?)<\/supp/, '<supplementary-material $1</label><caption>$2</caption></supp')
            xmlData = xmlData.replaceAll(/(?s)<supplementary-material (.+?)<\/label>(.+?)<media /, '<supplementary-material $1</label><caption>$2</caption><media ')

            xmlData = '<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE article PUBLIC "-//NLM//DTD JATS (Z39.96) Journal Archiving and Interchange DTD v1.1d3 20150301//EN"  "JATS-archivearticle1.dtd">\n' + xmlData
        } else {
            // Scan GSA articles for special unicode characters
            def matcher = xmlData =~ /[^\x00-\x7F]/


            while (matcher.find()) {
                xmlData = xmlData.substring(0, matcher.start()) + "&#x" + Integer.toHexString((int) matcher.group()) + ";" + xmlData.substring(matcher.end(), xmlData.length())
                matcher = xmlData =~ /[^\x00-\x7F]/
            }

            def runHeader = "";
            matcher = originalXml =~ /<\?RF.*\?>/
            while (matcher.find()) {
                runHeader += matcher.group() + "\n"
            }
            xmlData = xmlData.replaceAll(/<\/article-title><\/title-group>/, "</article-title>\n" + runHeader + "</title-group>")
//            xmlData = '<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE article PUBLIC "-//NLM//DTD Journal Publishing DTD v2.3 20070202//EN" "journalpublishing.dtd">\n' + xmlData
            // Grab the xml and doctype tags from the oringal document because texture loses them
            String[] doctype = originalXml.split("\n", 3)
            xmlData = doctype[0] + "\n" + doctype[1] + "\n" + xmlData
        }

        // * should be &#00X2A;
        xmlData = xmlData.replaceAll(/ \* /, " &#x002A; ")
        xmlData = xmlData.replaceAll(/>\*</, ">&#x002A;<")

        // Remove empty italics
        xmlData = xmlData.replaceAll("<italic> </italic>", " ")
        xmlData = xmlData.replaceAll("<italic/>", "")

        // self-uri should be self-closing
        xmlData = xmlData.replaceAll("></self-uri>", "/>")



        // For some reason texture puts the caption tag in there twice
        xmlData = xmlData.replaceAll("<caption><caption", "<caption")
        xmlData = xmlData.replaceAll("</caption></caption>", "</caption>")
        // Probably a better way to do this, but one case had a line return in it
        xmlData = xmlData.replaceAll("</caption>\n</caption>", "</caption>")

        xmlData = xmlData.replaceAll("<genetics-comment>", "<!--")
        xmlData = xmlData.replaceAll("</genetics-comment>", "-->")
        return xmlData
    }

    String getPubType(Publication publication) {
        if (publication.doi.toLowerCase().contains("/w2")) {
            return "micropublication"
        } else if (publication.doi.toLowerCase().contains("elife")) {
            return "elife"
        } else if (publication.doi.toLowerCase().contains("genetics")) {
            return "GSA"
        } else if (publication.doi.toLowerCase().contains("g3")) {
            return "GSA"
        } else {
            return null
        }
    }

//    @NotTransactional
//    Boolean validatePub(Publication publication) {
//        String xmlFile = publication.exportedData.value
//        return validatePubXml(xmlFile, publication.journal)
//    }

    @Transactional(readOnly = true)
    def validatePub(Publication publication) {
        return validatePubXml(publication.exportedData.value)
    }

//    Boolean validatePubXml(String xmlFile, String journal) {
//
//        // load resource DTD
//        XmlSlurper xmlSlurper = new XmlSlurper()
//        if (journal == 'genetics') {
//            xmlSlurper.parse(xmlFile)
//            return true
//        } else if (journal == 'elife') {
//            xmlSlurper.parse(xmlFile)
//            return true
//        }
//
//        return false
//    }

    @NotTransactional
    def validatePubFile(File file) {
        if (file.exists()) {
            return validatePubXml(file.text)
        } else {
            println "File ${file.absolutePath} does not exist."
            return false
        }

    }

    @NotTransactional
    def validatePubXml(String xmlString) {
        try {
            XmlSlurper xmlSlurper = new XmlSlurper(true, true, true)
            xmlSlurper.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
            xmlSlurper.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            xmlSlurper.setEntityResolver(InternalDtdResolver.entityResolver)
            xmlSlurper.parseText(xmlString)
            return true
        } catch (e) {
            println e
            return false
        }

    }

    List<String> getUsersForPublication(Publication publication) {

        if (Environment.currentEnvironment == Environment.PRODUCTION) {
            return ["kyook"]
        } else {
            []
        }
    }


    @NotTransactional
    def fixFileName(String xmlFileName) {
        if (xmlFileName.endsWith(".XML")) {
            xmlFileName = xmlFileName.substring(0, xmlFileName.lastIndexOf(".")) + ".xml"
        } else if (xmlFileName.endsWith(".xml")) {
            // do nothing
        } else {
            xmlFileName = xmlFileName + ".xml"
        }
        return xmlFileName
    }


}
