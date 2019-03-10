package com.insilico.dmc

import com.fasterxml.jackson.databind.ObjectMapper
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.markup.*
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.publication.PublicationStatusEnum
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

@Transactional
class MarkupService {

    def keyWordService
    def githubService

    private Long start, stop

//    int generateMarkup(Publication publication, List<LexiconSource> sourceList, Integer max = Integer.MAX_VALUE) {
//        start = System.currentTimeMillis()
//
//        // 1. we have to generate the DTO
//        println "populating search map"
//        Map<String, List<Lexicon>> lookupMap = populateMapForSources(sourceList, max)
//        println "populated search map"
//        stop = System.currentTimeMillis()
//        println "populate total time ${(stop - start) / 1000}"
//        // generate keywords for each of these:
//        KeyWordSet keyWordSet = new KeyWordSet(
//                sources: sourceList
//        ).save(flush: true, insert: true)
//        lookupMap.each {
//            KeyWord keyWord = new KeyWord(
//                    value: it.key
//                    , keyWordSet: keyWordSet
//                    , lexica: it.value
//            ).save()
//            keyWordSet.keywords.add(keyWord)
//        }
//
//        println "lookupMap size: ${lookupMap.size()}"
//        keyWordSet.save(flush: true)
////        generateMarkup(publication, lookupMap)
//        generateMarkupForKeyWords(publication, keyWordSet)
//    }

    /**
     * Find KeyWordSet as the same as LexiconSource
     *
     * @param publication
     * @param sourceList
     * @return
     */
    int generateMarkupForKeyWords(Publication publication, List<LexiconSource> sourceList) {
        int count = 0


        List<KeyWordSet> keyWordSetList = keyWordService.getKetWordSets(sourceList)
        println "keywordsetlist ${keyWordSetList.size()}"
        if (keyWordSetList) {
            count = generateMarkupForKeyWords(publication, keyWordSetList.first())
        }
//                KeyWordSet.findAllBySources(sourceList as Set<LexiconSource>)
//        keyWordSetList.each{ keyWordSet ->
//            count += generateMarkupForKeyWords(publication,keyWordSet)
//        }
        return count
    }

    int generateMarkupForKeyWords(Publication publication, KeyWordSet keyWordSet) {
        start = System.currentTimeMillis()

        int count = 0
        int FLUSH_SIZE = 2000
        Long saveTime = 0
        int added = 0
        KeyWord.findAllByKeyWordSet(keyWordSet).each { it ->
            List<MarkupLocationDTO> markupLocationDTOS = lookupTerms(it.value, publication)
            if (markupLocationDTOS.size() > 0) {
                added += markupLocationDTOS.size()
                println "${it} size for ${markupLocationDTOS.size()}"
            }
            markupLocationDTOS.each { markupLocationDTO ->
                it.lexica.each { lexicon ->
                    new Markup(
                            publication: publication
                            , finalLexicon: lexicon
                            , uuid: UUID.randomUUID().toString()
                            , locationStart: markupLocationDTO.start
                            , locationEnd: markupLocationDTO.stop
                            , keyWord: it
                            , status: MarkupStatusEnum.PROPOSED
                            , locationJson: new ObjectMapper().writeValueAsString(markupLocationDTO)
                    ).save(failOnError: true, insert: true)
                    if (count % FLUSH_SIZE == 0) {
//                session.flush()
//                session.clear()
                        println "lookup term hits ${markupLocationDTOS.size()}"
                        stop = System.currentTimeMillis()
                        println "flushing at ${count} time is ${stop - start} speed: ${FLUSH_SIZE / ((stop - start) / 1000)}"
                        saveTime = (stop - start) / 1000
                        start = stop
//                session.flush()
//                session.clear()
                    }
                    ++count
                }
            }
        }
        stop = System.currentTimeMillis()
//        println "${FLUSH_SIZE} storing time ${saveTime} of size: ${keyWordSet.keyWords.size()}  added ${added} for ${(float) keyWordSet.keyWords.size() / saveTime}"

        start = System.currentTimeMillis()
//        session.flush()
        publication.save(insert: false, flush: true)
        stop = System.currentTimeMillis()
//        println "flushing time ${stop - start} of size: ${keyWordSet.keyWords.size()}"
        start = System.currentTimeMillis()
        return count
    }

    int generateMarkup(Publication publication, Map<String, List<Lexicon>> lookupMap) {
        start = System.currentTimeMillis()
//        PublicationDTO publicationDTO
//
//        try {
//            publicationDTO = new ObjectMapper().readValue(publication.ingestedData.value, PublicationDTO.class)
//        } catch (e) {
//            println "error ingesting pub ${publication.fileName} - ${e}"
//            return 0
//        }

        int count = 0
        int FLUSH_SIZE = 2000
        Long saveTime = 0
        int added = 0
        lookupMap.each { k, v ->
            List<MarkupLocationDTO> markupLocationDTOS = lookupTerms(k, publication)
            if (markupLocationDTOS.size() > 0) {
                added += markupLocationDTOS.size()
                println "${k} size for ${markupLocationDTOS.size()}"
            }
            markupLocationDTOS.each { markupLocationDTO ->
                v.each { lexicon ->
                    new Markup(
                            publication: publication
                            , finalLexicon: lexicon
                            , uuid: UUID.randomUUID().toString()
                            , locationJson: new ObjectMapper().writeValueAsString(markupLocationDTO)
                    ).save(failOnError: true, insert: true)
                    if (count % FLUSH_SIZE == 0) {
//                session.flush()
//                session.clear()
                        println "lookup term hits ${markupLocationDTOS.size()}"
                        stop = System.currentTimeMillis()
                        println "flushing at ${count} time is ${stop - start} speed: ${FLUSH_SIZE / ((stop - start) / 1000)}"
                        saveTime = (stop - start) / 1000
                        start = stop
//                session.flush()
//                session.clear()
                    }
                    ++count
                }
            }
        }
        stop = System.currentTimeMillis()
        println "${FLUSH_SIZE} storing time ${saveTime} of size: ${lookupMap.size()}  added ${added} for ${(float) lookupMap.size() / saveTime}"

        start = System.currentTimeMillis()
//        session.flush()
        publication.save(insert: false, flush: true)
        stop = System.currentTimeMillis()
        println "flushing time ${stop - start} of size: ${lookupMap.size()}"
        start = System.currentTimeMillis()
        return count
    }

    List<MarkupLocationDTO> lookupTerms(String key, Publication publication) {
        List<MarkupLocationDTO> markupLocationDTOS = new ArrayList<>()
        if (!key) return markupLocationDTOS
        markupLocationDTOS.addAll(createMarkups(key, publication.originalData.value))
        return markupLocationDTOS
    }

    List<MarkupLocationDTO> createMarkups(String key, String content) {
        List<MarkupLocationDTO> markupLocationDTOList = []
        if (!key || !content) return markupLocationDTOList

//        boolean debugFlag = contentType == ContentTypeEnum.ABSTRACT && key == "Netrin"
//        if (debugFlag) println "looking for ${key} in ${content} && size ${content.size()}"
        for (int index = 0; index < content.length() && index >= 0; index += key.length()) {
//            if (debugFlag) println "init index ${content.length()} at ${index}"
            index = content.indexOf(key, index)
//            if (debugFlag) println "found ${key} at ${index}"
            if (index >= 0) {
                MarkupLocationDTO markupLocationDTO = new MarkupLocationDTO(
                        start: index
                        , stop: index + key.length()
                        , word: key
                        , uuid: UUID.randomUUID().toString()
                )
                markupLocationDTOList.add(markupLocationDTO)
            } else {
                return markupLocationDTOList
            }
        }
//        println "final size: ${markupLocationDTOList.size()}"


        return markupLocationDTOList

    }


    MarkupLocationDTO getLocation(Markup markup) {
        return new ObjectMapper().readValue(markup.locationJson, MarkupLocationDTO.class)
    }

    MarkupLocationDTO setLocation(Markup markup, Integer workIndex, Integer start, Integer stop) {
        MarkupLocationDTO markupLocationDTO = getLocation(markup)
//        MarkupLocationDTO.Range range = markupLocationDTO.locations.get(workIndex) ?: new MarkupLocationDTO.Range(
//                start:start
//                ,stop: stop
//        )
        markupLocationDTO.range.start = start
        markupLocationDTO.range.stop = stop
//        markupLocationDTO.locations.put(workIndex,range)
        return markupLocationDTO
    }

    def markupPubs(List<Publication> pubList, Map<String, List<Lexicon>> lookupMap) {
        Long time = 0, avgTime
        int count = pubList.size()
        pubList.eachWithIndex { it, index ->
            start = System.currentTimeMillis()
            println "generating mark for ${it.fileName} of ${index / count * 100.0}"
            generateMarkup(it, lookupMap)
            stop = System.currentTimeMillis()
            time += stop - start
            avgTime = (float) time / (float) index
            println "DONE generating mark for ${it.fileName} time: ${(stop - start) / 1000.0}"
            println "time left: ${(count - index) * avgTime / 1000f}"
            ++count
        }
    }

    def markupPubsForKeyWords(List<Publication> pubList, List<LexiconSource> sourceList) {
        Long time = 0, avgTime
        int count = pubList.size()
        pubList.eachWithIndex { it, index ->
            start = System.currentTimeMillis()
            println "generating mark for ${it.fileName} of ${index / count * 100.0}"
//            generateMarkup(it, lookupMap)
            generateMarkupForKeyWords(it, sourceList)
            stop = System.currentTimeMillis()
            time += stop - start
            avgTime = (float) time / (float) index
            println "DONE generating mark for ${it.fileName} time: ${(stop - start) / 1000.0}"
            println "time left: ${(count - index) * avgTime / 1000f}"
            ++count
        }
    }

    Publication revertPub(Publication publication) {
        println "orginal data ${publication.originalData}"
        println "exported data ${publication.exportedData}"
        publication.exportedData.value = publication.originalData.value
        try {
            publication.exportedData.save(flush: true, failOnError: true)
            publication.status = PublicationStatusEnum.INGESTED
            githubService.synchronizeLabelStatus(publication)
            publication.markupSources = null;
            publication.lastEdited = new Date()
            println "removing links"
            int removedLinks = Publication.executeUpdate("MATCH (p:Publication)-[r]-(m:Markup)-[kw]-(s)  where p.doi = {doi} delete r,m,kw", [doi: publication.doi])
            println "removed links ${removedLinks}"
            int removedKws = Publication.executeUpdate("MATCH (kws:KeyWordSet)-[r]-(p:Publication) where p.doi = {doi} delete r ", [doi: publication.doi])
            println "removed linkes ${removedLinks} and kws ${removedKws}"

            // Should we set the status back to ingested?
            publication.save(flush: true, failOnError: true)
            return publication
        } catch (e) {
            println "error reverting pub: ${e}"
            return null
        }
    }

    def clearTerms(Publication publication, List<String> uuids) {
        println "clearing terms: ${publication} and ${uuids}"
//        int removed = Publication.executeUpdate("MATCH (p:Publication)-[r]-(m:Markup) where m.uuid in {uuids} delete r,m",[uuids:uuids])
//        int removed = Publication.executeUpdate("MATCH (p:Publication)-[r:MARKUPS]-(m:Markup) where p.fileName = {fileName} and m.status = 'PROPOSED' delete r,m",[fileName: publication.fileName])
        int removed = Publication.executeUpdate("MATCH (p:Publication)-[r:MARKUPS]-(m:Markup)-[q]-() where p.fileName = {fileName} and m.status = 'PROPOSED' delete r,m,q", [fileName: publication.fileName])
        println "removed markups: ${removed}"
    }

    def deleteMarkup(Markup markup) {
        println "removing markup: ${markup}"
        println "trying to execute: MATCH (m:Markups)-[r]-() where m.extLinkId = {markupId} delete m,r with ${markup.extLinkId}"
//        int removed = Markup.executeUpdate("MATCH (m:Markups)-[r]-() where m.extLinkId = {markupId} delete m,r",[markupId: markup.extLinkId])
        int removed = Markup.executeUpdate("MATCH (m:Markup)-[r]-(p:Publication),(m)-[q]-(k:KeyWord),(m)-[s]-(l:Lexicon) where  m.extLinkId= {markupId} delete m,r,q,s", [markupId: markup.extLinkId])
        println "removed markups: ${removed}"
        return removed
    }

    def getLinkedWordsForLexicon(Lexicon lexicon) {
        JSONArray returnArray = new JSONArray()
//        Lexicon.findAllBy


        render returnArray as JSONArray
    }
}
