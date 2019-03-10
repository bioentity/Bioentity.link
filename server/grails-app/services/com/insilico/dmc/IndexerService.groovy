package com.insilico.dmc

import com.insilico.dmc.index.PublicationContentIndex
import com.insilico.dmc.publication.Content
import com.insilico.dmc.publication.Publication
import com.romix.scala.collection.concurrent.TrieMap
import grails.transaction.Transactional

@Transactional
class IndexerService {

    /**
     * Slurp in pub XML
     * Index from EACH word in xml (may need some special stuff here)
     * @param publication
     */
    def indexContent(Content content) {

    }

    def searchWord(String word,Publication publication){

    }

    List<String> searchWordsByComparingIndices(List<String> words,Publication publication){
        TrieMap publicationMap = getPublicationIndex(publication)

        // TODO: use a filter
        List<String> returnList = []
        words.each { word ->
            if(publicationMap.containsKey(word)){
                returnList.add(word)
            }
        }
        return returnList
    }

    TrieMap getPublicationIndex(Publication publication) {
        String xmlString = publication.originalData.value

        def result = new XmlSlurper().parse(xmlString)


        TrieMap trieMap = new TrieMap()

        // for result(){



        PublicationContentIndex contentIndex = new PublicationContentIndex()
        contentIndex.indexData = trieMap.bytes
        contentIndex.save(flush:true)
        publication.originalDataIndex = contentIndex
        publication.save(flush:true)


        return trieMap

    }

    List<String> searchWords(List<String> words, Publication publication){
        TrieMap publicationMap = getPublicationIndex(publication)
        TrieMap indexMap = getEntityIndex(publication)

    }

    TrieMap getEntityIndex(Publication publication) {
        null
    }

    def indexPubList(List<String> words){

    }

}
