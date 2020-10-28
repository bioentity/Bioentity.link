package com.insilico.dmc

import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.markup.Markup
import com.insilico.dmc.markup.MarkupObject
import com.insilico.dmc.publication.Publication
import grails.converters.JSON
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.scheduling.annotation.Async
import org.springframework.web.servlet.ModelAndView

import static org.springframework.http.HttpStatus.*

//@Transactional(readOnly = true)
class LexiconController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def lexicaCaptureService
    def markupService

    def index() {
        println "in here with ${params}"
        def lexicon = Lexicon.findByUuid(params.id) ?: Lexicon.findByExternalModId(params.id)
        if (!lexicon) {
            // do by lexicon source and public name
            LexiconSource lexiconSource = LexiconSource.findBySource(params.source)
            lexicon = Lexicon.findByLexiconSourceAndPublicName(lexiconSource, params.word)
        }
        if (!lexicon) {
            response.status = NOT_FOUND
            return
        }
        render lexicon as JSON
//        [lexicon: lexicon]
//        return new ModelAndView("/lexicon/index", [lexicon: lexicon])

    }

    def getLexicaByUUID(String uuid) {
        respond Lexicon.findByUuid(uuid)
    }

    def markedUp(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        long start, stop
        start = System.currentTimeMillis()
        List<Lexicon> markupList = Lexicon.findAllByMarkupsIsNotEmpty().sort() { a, b ->
            b.markups.size() <=> a.markups.size()
        }
        stop = System.currentTimeMillis()
        println "time: ${(stop - start) / 1000.0}"
        println "for retrieved size: ${markupList.size()}"

        return new ModelAndView("/lexicon/markedUp", [markupList: markupList])
    }

    /**
     * Return: sortable list, with species, number of articles, number of times linked per article
     *
     * Should be a set of objects:
     * - lookup / public_name
     * - species (lexicon_source)
     * - links per article (use this to get the number of articles)
     *
     * https://app.asana.com/0/309726565909056/318772320119467
     * @param max
     * @return
     */
    def hitCount(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        long start, stop
        start = System.currentTimeMillis()
        List<Markup> markupList = Markup.all
        Map<String, MarkupObject> markupObjectMap = [:]
        for (Markup markup in markupList) {
            MarkupObject markupObject = markupObjectMap.get(markup.finalLexicon.publicName)
            if (!markupObject) {
                markupObject = new MarkupObject(
                        lookupTerm: markup.finalLexicon.publicName,
                        species: markup.finalLexicon.lexiconSource.species,
                        className: markup.finalLexicon.lexiconSource.className,
                )
                def publicationMap = new HashMap<Publication, Integer>()
                publicationMap.put(markup.publication, 0)
                markupObject.publicationMap = publicationMap
            }
            Integer markupCount = markupObject.publicationMap.get(markup.publication) ?: 0
            markupObject.publicationMap.put(markup.publication, markupCount + 1)
            markupObjectMap.put(markupObject.lookupTerm, markupObject)
        }
        stop = System.currentTimeMillis()
        println "time: ${(stop - start) / 1000.0}"
        println "markups: ${Markup.count} pubs: ${Publication.count} lexicons: ${Lexicon.count}"
        println "for retrieved size: ${markupList.size()}"

        return new ModelAndView("/lexicon/hitCount", [markupList: markupObjectMap.values()])
    }

    def multipleSources(Integer max) {
        params.max = Math.min(max ?: 10, 100)

//       def keys = ambiguousKeys ?: markupService.populateMapForSources(LexiconSource.all).findAll(){
        long start, stop
        start = System.currentTimeMillis()
        // TODO: need to switch per
//        $MATCH (l:Lexicon)-[:LEXICONSOURCE]->(source) where l.publicName is not null RETURN distinct l.publicName,count(source) as cnt order by cnt desc limit 10000 ;
        def keys = Lexicon.executeQuery("select l.publicName,count(s) from Lexicon l join l.lexiconSource s group by l.publicName having count(*) > 1")
//        def keys = markupService.populateMapForSources(LexiconSource.all).findAll() {
//            it.value.size() > 1
//        }.collectEntries {
//            [(it.key): it.value.size()]
//        }
        stop = System.currentTimeMillis()
        println "time: ${(stop - start) / 1000.0}"

        return new ModelAndView("/lexicon/multipleSources", [lexicaList: keys])
    }

    def show(Lexicon lexica) {
        respond lexica
    }

    def getLexicaByModId(String id) {
        Lexicon lexicon = Lexicon.findByExternalModId(id)
        if(!lexicon) {
              JSONObject returnObject = new JSONObject()
            returnObject.error = "Could not find lexicon for ${id}"
//            response.status = 404
            render returnObject as JSON
            return
        }
        render lexicon as JSON
    }

    def lookup(String id) {
        Lexicon lexicon = Lexicon.findByUuid(id)
        lexicon = lexicon ?: Lexicon.findByExternalModId(id)
        if (!lexicon && id.contains(":")) {
 	    def splitId = id.split(":") as List
            String sourcePrefix = splitId.remove(0)
            String modId = splitId.join("")        
//    String sourcePrefix = id.split(":")[0]
  //          String modId = id.split(":")[1]
            def lexiconSources = LexiconSource.findAllByPrefix(sourcePrefix)
            for(ls in lexiconSources){
                lexicon = lexicon ?: Lexicon.findByExternalModIdAndLexiconSource(modId,ls)
            }
        }
        lexicon = lexicon ?: Lexicon.findByPublicName(id)
        println "found lexicon by public name ${lexicon} -> ${id}"

        if (!lexicon) {
            JSONObject returnObject = new JSONObject()
            returnObject.error = "Could not find lexicon for ${id}"
//            response.status = 404
            render returnObject as JSON
            return
        }

        JSONObject returnObject = new JSONObject()
        returnObject.lexicon = lexicon

        // TODO: check the URL asynchronously
        // TODO: note if its an "internal lexica" and set the way
        println "looking up link : ${lexicon.findLink()}"
        long start = System.currentTimeMillis()
        def validateUrl = false
        returnObject.validExternalLink = true
        if(validateUrl){
            def code = (new URL(lexicon.findLink())).openConnection().with {
                requestMethod = 'HEAD'
                connect()
                responseCode
            }
            returnObject.validExternalLink = code == 200
        }
        long stop = System.currentTimeMillis()
        println "found it ${stop-start}"

        // TODO: get all links for this lexicon object
        List<Node> nodeList = Lexicon.executeQuery("MATCH (l:Lexicon)--(k:KeyWord)--(m:Markup)--(p:Publication) where l.externalModId={externalModId} return distinct p , count(distinct m) as markupCount",
                [externalModId: lexicon.externalModId])

        JSONArray linkedPubs = new JSONArray()
        Integer totalCount = 0

        nodeList.each {
            def publication = it["p"] as Publication
            def markupCount = it["markupCount"] as Integer
            JSONObject linkedPubObject = new JSONObject()
            linkedPubObject.doi = publication.doi
            linkedPubObject.title = publication.title
            linkedPubObject.markupCount = markupCount
            totalCount += linkedPubObject.markupCount

            linkedPubs.add(linkedPubObject)
        }

//                { pub: pub1, numLinks: links1 }
        returnObject.linkedPublications = linkedPubs
        returnObject.totalCount = totalCount
        render returnObject as JSON
    }

    def create() {
        respond new Lexicon(params)
    }

    @Transactional
    def uploadCSV(LexiconSource lexiconSource, Species species) {
        def file = request.getFile('lexicacsv')
        lexicaCaptureService.capture(file, lexiconSource, species)
    }

    @Transactional
    def save(Lexicon lexicon) {
		def s = Species.findById(lexicon.lexiconSource.species)
		println s
        if (lexicon == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        Lexicon exists = Lexicon.findByExternalModId(lexicon.externalModId)
        if (exists != null) {
            println "exists"
            render exists as JSON
            return
        }

        if (lexicon.hasErrors()) {
            transactionStatus.setRollbackOnly()
			println lexicon.lexiconSource.species
            respond lexicon.errors, view: 'create'
            return
        }

        println "saving"
        Lexicon lexiconObject = new Lexicon(
                uuid: lexicon.uuid
                , externalModId: lexicon.externalModId
                , synonym: lexicon.synonym
                , publicName: lexicon.publicName
                , link: lexicon.link
        ).save(flush: true, failOnError: true)
        println "saved"


        LexiconSource.executeUpdate("MATCH (l:Lexicon),(ls:LexiconSource) where l.uuid = {lexiconUUID} and ls.uuid = {lexiconSourceUUID} create (l)<-[:LEXICA]-(ls)",
                [lexiconUUID: lexiconObject.uuid, lexiconSourceUUID: lexicon.lexiconSource.uuid]
        )
        println "saved 3 "

        render lexiconObject as JSON
    }

    def edit(Lexicon lexica) {
        respond lexica
    }

//    @Transactional
//    def update(Long id) {
//        println "updating lexicon"
//
//        def json = request.JSON
//
//        Lexicon lexicon = Lexicon.findById(id)
//
//
//
//        println "2 handling lexicon: ${lexicon as JSON}"
//
//        if (lexicon == null) {
//            transactionStatus.setRollbackOnly()
//            notFound()
//            return
//        }
//        lexicon.uuid = lexicon.uuid ?: UUID.randomUUID().toString()
//        lexicon.publicName = json.publicName
//        lexicon.externalModId = json.externalModId
//        lexicon.synonym = json.synonym
////        lexicon.lexiconSource = json.lexiconSource
//        lexicon.isActive = json.isActive
////        lexicon.link = lexicon.generateLink()
//
//        if (lexicon.hasErrors()) {
//            println "has errors: ${lexicon.errors}"
//            transactionStatus.setRollbackOnly()
//            respond lexicon.errors, view: 'edit'
//            return
//        }
//
////        lexicon.save flush: true, failOnError: true
//        println "3 handling lexicon ${lexicon as JSON}"
//        lexicon.save insert: false, failOnError: true
//        println "4 handling lexicon ${lexicon as JSON}"
//
//        render new JSONObject() as JSON
////        request.withFormat {
////            form multipartForm {
////                flash.message = message(code: 'default.updated.message', args: [message(code: 'lexica.label', default: 'Lexica'), lexicon.id])
////                redirect lexicon
////            }
////            '*' { respond lexicon, [status: OK] }
////        }
//    }
    def lexiconCheck() {
        def lexiconJSON = request.JSON
		def returnObject = new JSONObject()
		if(lexiconJSON.publicName) {
	        def lexicon = Lexicon.findAllByPublicName(lexiconJSON.publicName)
    	    if (lexicon.size() > 0) {
				if(lexicon[0].id == lexiconJSON.id) {
					render returnObject
					return
				}
        	    returnObject.put("error", "Public Name [${lexiconJSON.publicName}] Exists")
            	render returnObject as JSON
	        }
		}
		/*
		if(lexiconJSON.externalModId) {
	        def lexicon = Lexicon.findAllByExternalModId(lexiconJSON.externalModId)
    	    if (lexicon.size() > 0) {
        	   	if(lexicon[0].id == lexiconJSON.id) {
					render returnObject
					return
				}
				returnObject.put("error", "ID [${lexiconJSON.externalModId}] Exists")
            	render returnObject as JSON
	        }
		}
		*/
        render returnObject
    }


    @Transactional
    def update(Long id) {

        Lexicon lexicon = Lexicon.findById(id)
        println "found lexicon ${lexicon}"
        def json = request.JSON
        println json
        Lexicon.executeUpdate("MATCH (l:Lexicon) WHERE ID(l) = {id} " +
                "set l.publicName = {publicName} " +
                "set l.externalModId = {externalModId} " +
                "set l.synonym = {synonym} " +
                "set l.isActive = {isActive} " +
                "set l.comments = {comments} ",
                [id             : id
                 , publicName   : json.publicName
                 , externalModId: json.externalModId
                 , synonym      : json.synonym
                 , isActive     : json.isActive
                 , comments     : json.comments
                ]
        )

        lexicon = Lexicon.findById(id)
        respond lexicon

    }

    @Transactional
    def delete(Lexicon lexicon) {
        if (lexicon == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        return Lexicon.executeUpdate("MATCH (l:Lexicon)-[r]-() WHERE ID(l) = {id} DELETE l,r", [id: lexicon.id])
    }

    def search() {
        def lexica = Lexicon.findAllByPublicNameIlike("%" + params.term + "%")
        if (!lexica) {
            lexica = Lexicon.findAllByExternalModIdIlike("%" + params.term + "%")
        }
        def lexResult = []
        if (params.class == "undefined" && params.species == "undefined") {
            lexResult = lexica
        } else if (params.species == "undefined") {
            for (def l in lexica) {
                if (l.lexiconSource.className.toString() == params.class) {
                    lexResult.push(l)
                }
            }

        } else if (params.class == "undefined") {
            def species = Species.findById(params.species)
            for (def l in lexica) {
                if (l.lexiconSource.species == species) {
                    lexResult.push(l)
                }
            }
        } else {
            def species = Species.findById(params.species)
            for (def l in lexica) {
                if (l.lexiconSource.species == species && l.lexiconSource.className.toString() == params.class) {
                    lexResult.push(l)
                }
            }
        }


        render lexResult as JSON
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'lexica.label', default: 'Lexica'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
