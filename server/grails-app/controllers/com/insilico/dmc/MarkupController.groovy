package com.insilico.dmc

import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.Markup
import com.insilico.dmc.markup.MarkupStatusEnum
import com.insilico.dmc.markup.MarkupTypeEnum
import com.insilico.dmc.publication.Publication
import grails.converters.JSON
import grails.rest.*
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.StatementResult


class MarkupController extends RestfulController {

    static responseFormats = ['json', 'xml']

    def markupService

//    static allowedMethods = [delete: "DELETE"]

    MarkupController() {
        super(Markup)
    }


    @Transactional
    def setFinalLexicon() {
//        println "set final lexicon ${params}"

        def markupObject = new JSONObject(JSON.parse(params.markup))
        def lexiconObject = new JSONObject(JSON.parse(params.lexicon))
        Markup markup = Markup.findByUuid(markupObject.uuid) ?: Markup.findById(markupObject.id)
        Lexicon lexicon = Lexicon.findByUuid(lexiconObject.uuid) ?: Lexicon.findById(lexiconObject.id)

//        println "markup: ${markup}"
//        println "lexicon: ${lexicon}"

        markup.finalLexicon = lexicon
        lexicon.addToMarkups(markup)
        markup.save(flush: true)
        lexicon.save(flush: true)

//        Markup.findAll().sort(){ a,b -> a.keyWord.uuid <=> b.keyWord.uuid }.each {
//            if(it.finalLexicon){
//                println "markup ${it.keyWord.value} has final lexicon ${it.finalLexicon}"
//            }
//        }

        render markup as JSON
    }

    @Transactional
    def saveLink() {
        println "saving link ${params}"
        Publication publication = Publication.findByFileNameIlike(params.fileName + ".xml")
        println "found pub: ${publication}"
        def termObject = new JSONObject(params.termData)

        println "saving term: ${termObject as JSON}"
        KeyWord keyWord = KeyWord.findByUuid(termObject.uuid)
        keyWord = keyWord ?: KeyWord.findById(termObject.id)
        if (!keyWord) {
            println termObject.id
            //		keyWord = new KeyWord();
            //		keyWord.uuid = UUID.randomUUID().toString()
            //		keyWord.value = termObject.lexica[0].publicName
        }
        JSONObject selectionObject = termObject.selection
        Markup markup = new Markup(
            publication: publication,
            uuid: UUID.randomUUID().toString(),
            status: MarkupStatusEnum.MATCHED,
            type: MarkupTypeEnum.LINK,
            path: selectionObject.path[0],
            locationStart: selectionObject.startOffset,
            locationEnd: selectionObject.endOffset,
            locationJson: selectionObject.toString(),
            extLinkId: termObject.extLinkId,
        ).save(failOnError: true, insert: true)
        keyWord.addToMarkups(markup)

        Lexicon lexicon = Lexicon.findByUuid(termObject.lexica[0].uuid)
        println "found a lexicon: ${lexicon}"
        lexicon.addToMarkups(markup)

        publication.addToMarkups(markup)
        publication.addToMarkupSources(keyWord.keyWordSet)
//        Event event = new Event(
//                uuid: UUID.randomUUID().toString()
//        )
//
//        MarkupEvent markupEvent = new MarkupEvent(
//                eventType : MarkupEventEnum.CREATED
//                ,uuid: UUID.randomUUID().toString()
//                ,from: markup
//                ,to: event
//        )

        render publication as JSON
    }

    @Transactional
    def saveBulkLinks() {
        //println "saving link ${params}"
        println "saving with filename ${params.fileName}"
        Publication publication = Publication.findByFileName(params.fileName )
        println "found pub: ${publication}"
        def termObjects = new JSONObject(params.termData)

		Map<String, Object> markups = new HashMap<>()
        for (def termObject in termObjects.termData) {
//            println "saving term: ${termObject as JSON}"
//            KeyWord keyWord = KeyWord.findByUuid(termObject.uuid)
            KeyWord keyWord = KeyWord.findByUuid(termObject.uuid)
            Lexicon lexicon = Lexicon.findByUuid(termObject.lexica[0].uuid)
            JSONObject selectionObject = termObject.selection

            println keyWord
			def markup = [
                publication: publication.fileName,
                uuid: UUID.randomUUID().toString(),
                status: MarkupStatusEnum.MATCHED.name(),
                type: MarkupTypeEnum.LINK.name(),
                path: selectionObject.path[0],
                locationStart: selectionObject.startOffset,
                locationEnd: selectionObject.endOffset,
                locationJson: selectionObject.toString(),
                extLinkId: termObject.extLinkId,
				keyWord: keyWord.uuid,
				keyWordSet: keyWord.keyWordSet.uuid,
				lexicon: lexicon.uuid]
			markups.put(markup.uuid, markup)

/*
            Markup markup = new Markup(
                    publication: publication
                    , uuid: UUID.randomUUID().toString()
                    , status: MarkupStatusEnum.MATCHED
                    , type: MarkupTypeEnum.LINK
                    , path: selectionObject.path[0]
                    , locationStart: selectionObject.startOffset
                    , locationEnd: selectionObject.endOffset
                    , locationJson: selectionObject.toString()
                    , extLinkId: termObject.extLinkId
            ).save(failOnError: true, insert: true)
            keyWord.addToMarkups(markup)

            println "found a lexicon: ${lexicon}"
            lexicon.addToMarkups(markup)

            publication.addToMarkups(markup)*/
  //          publication.addToMarkupSources(keyWord.keyWordSet)
        }

		Map<String, Object> params = new HashMap<>()

		params.put("props", markups.values())

		Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic(EnvironmentUtil.username, EnvironmentUtil.password))
        Session session = driver.session() 

		session.run("""
			UNWIND {props} as row

			MATCH (publication:Publication {fileName:row.publication})
			MATCH (lexicon:Lexicon {uuid:row.lexicon})
			MATCH (keyword:KeyWord {uuid:row.keyWord})
			MATCH (keyWordSet:KeyWordSet {uuid:row.keyWordSet})

			CREATE (markup: Markup {uuid: row.uuid})
			SET markup.status = row.status
			SET markup.type = row.type
			SET markup.path = row.path
			SET markup.locationStart = row.locationStart
			SET markup.locationEnd = row.locationEnd
			SET markup.locationJson = row.locationJson
			SET markup.extLinkId = row.extLinkId

			MERGE (markup)-[:PUBLICATION]->(publication)
			MERGE (lexicon)-[:MARKUPS]->(markup)
			MERGE (publication)-[:MARKUPS]->(markup)
			MERGE (keyword)-[:MARKUPS]->(markup)
			MERGE (publication)-[:MARKUPSOURCES]-(keyWordSet)
		""", params)
		

		session.close()
		
        render publication as JSON
    }


    def getByExtLinkId() {
        Markup markup = Markup.findByExtLinkId(params.extLinkId)
        render markup as JSON
    }

    @Transactional
    def delete() {
        println "deleting markup ${params}"
        Long id = Long.valueOf(params.id)
        println "found ID ${id}"
//        def markupObject = new JSONObject(JSON.parse(params.markup))
        Markup markup = Markup.findById(id)
        println "found markup ${markup}"
        int removed = markupService.deleteMarkup(markup)
        println "removed ${removed}"

        JSONObject returnObject = new JSONObject(
                removed: removed
                , markup: markup
        )

        render returnObject as JSON
    }

    @Transactional
    def deleteAll() {
        def markupArray = JSON.parse(params.markups) as JSONArray

        def markupList = []
        markupArray.each{
            markupList.add(it)
        }

        def markups = Markup.findAllByUuidInList(markupList)
        println "markups to delete size: ${markups.size()}"
        println "markups to delete ${markups}"

        println "markup list to delete [${markupList}]"

        int deleted = Markup.executeUpdate("MATCH (p:Publication)-[r]-(m:Markup)-[q]-(k:KeyWord),(m)-[s]-(l:Lexicon) where m.uuid in {uuids} delete m,r,q,s",[uuids:markupList]) ;
        println "deleted: ${deleted}"
        render markups as JSON
    }

    def findForKeyWords() {
        println "find for keywords ${params}"
        Publication publication = Publication.findByDoi(params.publication)
        KeyWord keyWord = KeyWord.findByValue(params.keyWord)


        println "publicatin: ${publication}"
        println "keyword: ${keyWord}"

        if (publication && keyWord) {
//            def markups = Markup.findAllByPublicationAndKeyWord(publication,keyWord)
            def markups = []
            List<Node> nodeList = Markup.executeQuery("MATCH (p:Publication)--(m:Markup)--(k:KeyWord) where p.doi = {doi} and k.value = {keyWord} RETURN m", [doi: params.publication, keyWord: params.keyWord])
            nodeList.each {
                Markup markup = it as Markup
                markups.add(markup)
            }
            println "markups size: ${markups.size()}"
            println "markups: ${markups}"
            render markups as JSON
        } else {
            render new JSONObject() as JSON
        }
    }
}
