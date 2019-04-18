package com.insilico.dmc

import com.google.gson.Gson
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.KeyWordSet
import com.insilico.dmc.markup.Markup
import com.insilico.dmc.publication.CurationActivity
import com.insilico.dmc.publication.CurationStatusEnum
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.publication.PublicationStatusEnum
import com.insilico.dmc.user.User
import grails.converters.JSON
import grails.rest.RestfulController
import io.swagger.annotations.*
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.kohsuke.github.GHUser
import org.neo4j.driver.v1.Record
import org.neo4j.driver.v1.StatementResult
import org.springframework.transaction.annotation.Transactional

import static org.springframework.http.HttpStatus.*

@Api(value = "/api/v1", tags = ["Publication"], description = "Publication Api's")
class PublicationController extends RestfulController<Publication> {
    static namespace = 'v1'
    static responseFormats = ['json', 'xml']

    def publicationService
    def githubService
    def markupService
    def userService


    private Gson gson = new Gson()

    PublicationController() {
        super(Publication)
    }

    def findByDoi(String doiPrefix, String doiSuffix) {
        println "finding by doi ${doiPrefix} ${doiSuffix} ${params}"
        String doiString = doiPrefix + "/" + doiSuffix
        Publication publication = Publication.findByDoi(doiString)
        try {
            if (publication) {
                render publication as JSON
            } else {
                println "not found "
                response.status = 404

            }
        } catch (e) {
            log.error(e)
            response.status = 404
        }
    }

    def find() {
//        println "findinng by ID ${params}"
        Publication publication
        try {
            publication = Publication.findById(params.id as Long)
            publication = publication ?: Publication.findByDoi(params.id as String)
            publication = publication ?: Publication.findByFileNameIlike(params.id + ".xml")
            publication = publication ?: Publication.findByFileName(params.id as String)

            if (publication) {
                render publication as JSON
                return
            }
        } catch (e) {
            log.error(e)
        }

        String fileNameWithSuffix = params.id + (!params.id.toLowerCase().endsWith(".xml") ? ".xml" : "")
        publication = Publication.findByFileName(fileNameWithSuffix)
        if (publication) {
            render publication as JSON
        } else {
            return null
        }
    }

    def findByFileName(String xmlFileName) {
        String fileNameWithSuffix = xmlFileName + (!xmlFileName.toLowerCase().endsWith(".xml") ? ".xml" : "")
        Publication publication = Publication.findByFileName(fileNameWithSuffix)
        // Temp fix for XML parse error
        render publication.exportedData.value
    }

    def storeByFileName(String xmlFileName) {
        def json = request.JSON
        String xml = json.content
        String fileNameWithSuffix = xmlFileName + (!xmlFileName.endsWith(".xml") ? ".xml" : "")

        try {
            Publication.withNewTransaction {
                Publication publication = Publication.findByFileName(fileNameWithSuffix)
                publication.exportedData.value = xml
                publication.exportedData.save(flush: true, failOnError: true)
                if (publication.status == PublicationStatusEnum.INGESTED) {
                    publication.status = PublicationStatusEnum.MARKED_UP
                }
                githubService.synchronizeLabelStatus(publication)
                publication.save(flush: true, failOnError: true)
            }
        } catch (e) {
            println "error saving file: ${e}"
        }

        render xml
    }

    def getIndexedTerms(Publication publication) {
        def kwsId = params.kws
        KeyWordSet keyWordSet = KeyWordSet.findById(kwsId)
        // return the ke
        List<KeyWord> keyWordList = KeyWordSet.executeQuery("MATCH (kws:KeyWordSet)--(k:KeyWord),(p:Publication)--(c:Content)--(i:ContentWordIndex) where kws.name={kwsName} and p.id = {pubId} and i.word=k.value RETURN k", [kwsName: keyWordSet.name, pubId: publication.id])

        def returnArray = new JSONArray()
        for (KeyWord keyWord in keyWordList) {
            JSONObject keyWordObject = new JSONObject()
            keyWordObject.id = keyWord.id
            keyWordObject.value = keyWord.value
            keyWordObject.lexica = keyWord.lexica
            keyWordObject.uuid = keyWord.uuid
//            // TODO: make a separate query, instead of re-using it a bunch of times
            keyWordObject.sources = keyWordSet.sources
            returnArray.add(keyWordObject)
        }
//        println "the final return array ${returnArray as JSON}"
        render returnArray as JSON

    }


    @Transactional
    def applyKeyWordSet() {
        def keyWordSetId = request.JSON.keyWordSet.name
        def publicationId = request.JSON.publication.id
        KeyWordSet keyWordSet = KeyWordSet.findByName(keyWordSetId)
        Publication publication = Publication.findById(publicationId)
        publication.lastEdited = new Date()
        def indices = publicationService.getIndices(publication)
        if (!indices) {
            publicationService.createIndices(publication)
        }

        //markupService.revertPub(publication)

        List<Node> nodeList = KeyWordSet.executeQuery("MATCH (kws:KeyWordSet)--(k:KeyWord)--(l:Lexicon),(p:Publication)--(c:Content)--(i:ContentWordIndex) where kws.name={kwsName} and p.fileName = {fileName} and i.word=k.value RETURN {root:k, lexica:collect(l)}", [kwsName: keyWordSet.name, fileName: publication.fileName])
        List<KeyWord> keyWordList = new ArrayList<>()
        nodeList.unique().each {
            KeyWord keyWord = it["root"] as KeyWord
            List lexiconList = it["lexica"] as List

            keyWord.lexica = lexiconList.collect() { lexicon ->
                lexicon as Lexicon
            }
            keyWordList.add(keyWord)
        }
        println "keywords size: ${keyWordList.size()}"
        println "keywords: ${keyWordList.value.join(",")}"

        publication.addToMarkupSources(keyWordSet)
        keyWordSet.addToPublications(publication)
        publication.status = PublicationStatusEnum.MARKED_UP
        githubService.synchronizeLabelStatus(publication)
        publication.save(flush: true)

        JSONObject jsonObject = new JSONObject(publication.properties)
        // for some reason not part of the properties?
        jsonObject.id = publicationId
        jsonObject.remove("exportedData")
        jsonObject.remove("originalData")
        jsonObject.remove("markupSource")
        JSONObject markupSource = new JSONObject()
        markupSource.id = keyWordSet.id //publication.markupSource.id
        markupSource.name = keyWordSet.name //publication.markupSource.name
        jsonObject.put("markupSource", markupSource)

        // you can't set the status here or it complains
        jsonObject.statusString = publication.status.name()
        jsonObject.words = keyWordList
        render jsonObject as JSON
    }

    @Transactional
    def revertPub(Publication publication) {
        markupService.revertPub(publication)
        render publication as JSON
    }


    @Transactional
    def clearWords() {
        Publication publication = Publication.findByFileName(params.fileName)
        publication.lastEdited = new Date()
        JSONArray termArray = new JSONArray(params.termData)

        List<String> uuids = termArray.collect() {
            it.uuid
        }
        println "clearing markup for uuids: ${uuids}"

        markupService.clearTerms(publication, uuids)
        render publication as JSON
    }

    def show(Publication publication) {
        [publication: publication]
    }


    def downloadRaw(Publication publication) {
        response.setHeader("Content-disposition", "attachment; filename=raw-${publication.fileName}")
        def xmlData = publication.exportedData.value

        if (publicationService.validatePubXml(xmlData)) {
            println "Valid Raw XML for ${publication.doi} / ${publication.title}"
        } else {
            println "BAD Raw XML for ${publication.doi} / ${publication.title}"
        }


        def outputStream = response.outputStream
        outputStream << xmlData
        outputStream.flush()
        outputStream.close()
    }

    def download(Publication publication) {
        response.setHeader("Content-disposition", "attachment; filename=${publication.fileName}")
        def outputStream = response.outputStream
        // Remove xmlns
        def xmlData = publication.exportedData.value
        String pubType = publicationService.getPubType(publication)

        if (publicationService.validatePubXml(xmlData)) {
            println "Valid Raw XML for ${publication.doi} / ${publication.title}"
        } else {
            println "BAD Raw XML for ${publication.doi} / ${publication.title}"
        }

        xmlData = publicationService.filterXml(xmlData, pubType, publication.originalData.value)

        if (publicationService.validatePubXml(xmlData)) {
            println "Valid filtered XML for ${publication.doi} / ${publication.title}"
        } else {
            println "BAD filtered XML for ${publication.doi} / ${publication.title}"
        }

        outputStream << xmlData
        //outputStream << publication.exportedData.value
        outputStream.flush()
        outputStream.close()
    }

    // TODO: use this method as its a lot faster
    def getLinkedTermsDirect(Publication publication) {
        // NOTE: this is how to do a direct cypher query
        JSONObject returnObject = new JSONObject()
        StatementResult result = KeyWord.executeCypher("MATCH (m:Markup)--(k:KeyWord)--(l:Lexicon)--(s:LexiconSource),(p:Publication)--(m:Markup)  where p.fileName={fileName} RETURN {root: k,markups: collect(m),lexica: collect({lexicon:l,source:s } ) }", [fileName: publication.fileName])
        JSONArray resultsArray = new JSONArray()
        while (result.hasNext()) {
            Record record = result.next();
            resultsArray.add(new JSONObject(gson.toJson(record.asMap())))
        }
        returnObject.put("keyWords", resultsArray)
        render returnObject as JSON
    }


    def getLinkedTerms(Publication publication) {

//        println "gettling linked terms for pub: ${publication}"
//        println "gettling linked terms for params: ${params}"

        if (!publication && params.doi) {
            publication = Publication.findByDoi(params.doi)
            println "got pub from doi: ${publication}"
        }

        def keyWords = [:]
        Map<String, List<Markup>> markups = new TreeMap<>()
        publication = publication ?: Publication.findByFileName(publication.fileName)
        List<Node> nodeList = Markup.executeQuery("MATCH (m:Markup)-[]-(k:KeyWord)-[]-(l:Lexicon),(m:Markup)--(p:Publication) where p.fileName={fileName} RETURN m,k,l", [fileName: publication.fileName])
        Set<KeyWordSet> keyWordSets = []
        nodeList.each {
            Markup markup = it["m"] as Markup
            KeyWord keyWord = it["k"] as KeyWord
            Lexicon lexicon = it["l"] as Lexicon

            keyWord = keyWords.get(keyWord.value) as KeyWord ?: keyWord
            keyWordSets.add(keyWord.keyWordSet)
            Set<Markup> markupList = markups.get(keyWord.value) ?: []
            markupList.add(markup)
            lexicon.link = lexicon.findLink()
            lexicon.internalLink = lexicon.findInternalLink()
            keyWord.lexica = keyWord.lexica ?: []
            keyWord.lexica.add(lexicon)

            keyWords.put(keyWord.value, keyWord)
            markups.put(keyWord.value, markupList)
        }

        // sort markups
        for (markupEntry in markups) {
            List<Markup> markupValues = markupEntry.value as List
            Collections.sort(markupValues, new Comparator<Markup>() {
                @Override
                int compare(Markup o1, Markup o2) {
                    // sort by start second
//                    "paragraph-3"
                    if (o1.path.startsWith("paragraph-") && o2.path.startsWith("paragraph-")) {
                        def paragraph1 = o1.path.split("-")[1] as Integer
                        def paragraph2 = o2.path.split("-")[1] as Integer
                        def paragraphDiff = paragraph1 - paragraph2
                        if (paragraphDiff != 0) return paragraphDiff
                    } else {
                        println "unable to compare paths [${o1.path}] vs [${o2.path}]"
                    }

                    return o1.locationStart <=> o2.locationStart
                }
            })
            markups.put(markupEntry.key, markupValues)
        }

        def returnArray = new JSONArray()
        for (KeyWord keyWord in keyWords.values()) {
            JSONObject keyWordObject = new JSONObject()
            keyWordObject.id = keyWord.id
            keyWordObject.value = keyWord.value
            keyWordObject.lexica = keyWord.lexica
            keyWordObject.markups = new JSONArray(markups.get(keyWord.value) as List<Markup>)
            keyWordObject.uuid = keyWord.uuid
//            // TODO: make a separate query, instead of re-using it a bunch of times
            keyWordObject.sources = new JSONArray()
            for (KeyWordSet keyWordSet in keyWordSets) {
                keyWordObject.sources.add(keyWordSet.sources)
            }
            returnArray.add(keyWordObject)
        }
//        println "the final return array ${returnArray as JSON}"
        render returnArray as JSON
    }


    def content(Publication publication) {
        if (publication.originalData) {
            render publication.originalData.value
        } else {
            render "<content>No Data</content>"
        }
    }

    def markupSources(Publication publication) {
        render publication.markupSources ? publication.markupSources as JSON : new JSONObject()
    }

    def index() {
        params.status = params.status ? params.status == 'null' ? null : params.status : null
        params.search = params.search ? params.search == 'null' ? null : params.search : null
        params.species = params.species ? params.species == 'null' ? null : params.species : null

        def publications = publicationService.getPubsFromString(params.search, params.status, params.species, params.max, params.offset)
//        if (params.status) {
//            if (params.status == "INGESTED") {
//                publications = Publication.findAllByStatus(PublicationStatusEnum.INGESTED, [max: params.max, offset: params.offset])
//            } else if (params.status == "MARKED_UP") {
//                publications = Publication.findAllByStatus(PublicationStatusEnum.MARKED_UP, [max: params.max, offset: params.offset])
//            } else if (params.status == "CURATOR") {
//                publications = Publication.findAllByStatus(PublicationStatusEnum.CURATOR, [max: params.max, offset: params.offset])
//
//            }
//        } else if (params.search && params.search.size() > 2) {
//            // Search title, filename, doi
//            publications = publicationService.getPubsFromString(params.search, params.max, params.offset)
//        } else {
//            publications = Publication.findAll([max: params.max, offset: params.offset])
//        }

        JSONArray jsonArray = new JSONArray()
        for (p in publications) {
            JSONObject jsonObject = new JSONObject()
            jsonObject.id = String.valueOf(p.id)
            jsonObject.fileName = p.fileName
            jsonObject.title = p.title
            jsonObject.journal = p.journal
            jsonObject.doi = p.doi
//            jsonObject.status = new JSONObject()
//            jsonObject.status.value = p.status.getStatus()
//            jsonObject.status.name = p.status.name()

            jsonObject.status = p.status != null ? p.status.getStatus() : null

            jsonObject.accepted = p.accepted
            jsonObject.received = p.received
            jsonObject.ingested = p.ingested
            jsonObject.lastEdited = p.lastEdited
            jsonObject.species = new HashSet<>()
            for (def kws in p.markupSources) {
                for (def ls in kws.sources) {
                    jsonObject.species.add(ls.species.name)
                }
            }

            jsonArray.add(jsonObject)
        }

        render jsonArray as JSON
    }

    def getPubCount() {
        params.status = params.status ? params.status == 'null' ? null : params.status : null
        params.search = params.search ? params.search == 'null' ? null : params.search : ''
        params.species = params.species ? params.species == 'null' ? null : params.species : null

        def pubCount = publicationService.getPubCountFromString(params.search, params.status, params.species, params.max, params.offset)
        respond pubCount: pubCount
    }

    def ingestFile() {
        def xmlFile = params.xmlFile as String
        def fileName = params.fileName as String

        // Make sure we don't already have this file ingested
        println "fileName: ${fileName}"
//        Publication pub = Publication.findByFileName(fileName)
//        // if it already exists, don't try to re
//        if (pub) {
////            render status: UNPROCESSABLE_ENTITY
//            JSONObject jsonObject = new JSONObject(
//                    "error":e.message
//            )
//            response.status = 500
//            render jsonObject as JSON
//        }

        Publication publication
        try {
            publication = publicationService.ingestPublicationContent(fileName, xmlFile)
            render publication as JSON
        } catch (e) {
            JSONObject jsonObject = new JSONObject(
                    "error": e.message
            )
            response.status = 500
            render jsonObject as JSON
        }
    }


    def batchUpload() {
        println params.url
        def url = params.url.toURL()
        url.eachLine {
            if (it =~ /"(\d+\.xml)"/) {
                println it
            }
            //Publication publication = publicationService.ingestPublicationContent(fileName,
        }
    }


    def statistics(Publication publication) {
        println "statistics ${publication.fileName}"
        def markups = publication.markups
        println "markups ${publication?.markups?.size()}"
        render markups as JSON
    }

    @Transactional
    def update(Publication publication) {
        if (publication == null) {
            render status: NOT_FOUND
        }
        if (publication.hasErrors()) {
            respond publication.errors, view: 'edit'
        }
        publication.lastEdited = new Date()
        publication.save flush: true
        withFormat {
            html {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'publication.label', default: 'Publication'), publication.id])
                redirect book
            }
            '*' { respond publication, [status: OK] }
        }
    }

    @Transactional
    def delete() {
        println "params ${params}"
        println "json ${request.JSON}"
        def publication = Publication.findByDoi(request.JSON.doi)
        println "pub ${publication}"
        def user = User.findById(request.JSON.user.id)
        println "user ${user}"

        publicationService.deletePub(publication)
        githubService.closePub(publication, user)
        withFormat {
            html {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Publication.label', default: 'Publication'), publication.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NO_CONTENT }
        }
    }

    def getIndices(Publication publication) {
        render publicationService.getIndices(publication) as JSON
    }

    def downloadIndices(Publication publication) {
        render(text: publicationService.getIndices(publication).join("\n"))
    }

    @Transactional
    def clearIndices(Publication publication) {
        println "deleting indices: ${publication.fileName}"
        int deleted = publicationService.clearIndices(publication)
        println "DELTED indices: ${deleted}"
        getIndices(publication)
    }


    @Transactional
    def createIndices(Publication publication) {
        println "creating indices ${publication}"
        def fileName = params.fileName as String

        // Make sure we don't already have this file ingested
        println "fileName: ${fileName}"
        Publication pub = Publication.findByFileName(fileName)
        if (pub) {
            render status: UNPROCESSABLE_ENTITY
        }

        publicationService.createIndices(publication)
        render publication as JSON
    }


    @ApiOperation(
            value = "Get publication",
            nickname = "publication/{publicationId}",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Publication.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "pubId",
                    paramType = "path",
                    required = true,
                    value = "Publication Id",
                    dataType = "string"),

            @ApiImplicitParam(name = "applicationType",
                    paramType = "header",
                    required = true,
                    defaultValue = "web",
                    value = "Application Types",
                    dataType = "string"),

            @ApiImplicitParam(name = "Accept-Language",
                    paramType = "header",
                    required = true,
                    defaultValue = "en",
                    value = "Accept-Language",
                    dataType = "string")
    ])

    def getPublication(String pubId) {
        Publication pub = Publication.findById(pubId)
        render(pub as JSON)
    }

    @ApiOperation(
            value = "List Publicationss",
            nickname = "publication/list",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "GET",
            response = Publication.class
    )


    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only GET is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])

    @ApiImplicitParams([
            @ApiImplicitParam(name = "offset",
                    paramType = "query", required = true,
                    value = "Offset", dataType = "integer"),

            @ApiImplicitParam(name = "limit",
                    paramType = "query",
                    required = true,
                    value = "Max size",
                    dataType = "integer"),

            @ApiImplicitParam(name = "applicationType",
                    paramType = "header",
                    required = true,
                    defaultValue = "web",
                    value = "Application Types",
                    dataType = "string"),

            @ApiImplicitParam(name = "Accept-Language",
                    paramType = "header",
                    required = true,
                    defaultValue = "en",
                    value = "Accept-Language",
                    dataType = "string")
    ])

    def getPublicationList(Integer offset, Integer limit) {
        Publication[] pubs = Publication.findAll([offset: offset, max: limit])
        render(pubs as JSON)
    }

    @ApiOperation(
            value = "Create Publication",
            notes = "Creates a new Publication. Accepts a Publication json.",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "POST",
            nickname = "/publication/createUpdate",
            response = Publication.class
    )

    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only POST is allowed"),

            @ApiResponse(code = 404,
                    message = "Method Not Found")
    ])
    @ApiImplicitParams([
            @ApiImplicitParam(name = "body",
                    paramType = "body",
                    required = true,
                    value = "Requires Publication Details",
                    dataType = "dto.CityDTO"),

            @ApiImplicitParam(name = "applicationType",
                    paramType = "header",
                    required = true,
                    defaultValue = "web",
                    value = "Application Types",
                    dataType = "string"),

            @ApiImplicitParam(name = "Accept-Language",
                    paramType = "header",
                    required = true,
                    defaultValue = "en",
                    value = "Accept-Language",
                    dataType = "string")
    ])

    def createOrUpdatePublication(Publication pub) {
        pub.save(flush: true)
        render(pub as JSON)
    }

    @ApiOperation(
            value = "Delete Publication",
            notes = "Deletes a Publication. Accepts a Publication ID .",
            produces = "application/json",
            consumes = "application/json",
            httpMethod = "DELETE",
            nickname = "/publication/{publicationId}",
            response = Publication.class
    )
    @ApiResponses([
            @ApiResponse(code = 405,
                    message = "Method Not Allowed. Only Delete is allowed"),
            @ApiResponse(code = 404,
                    message = "Method Not Found")])

    @ApiImplicitParams([
            @ApiImplicitParam(name = 'publicationId',
                    paramType = 'path',
                    required = true, value = "Requires publication id for delete",
                    dataType = "string"),

            @ApiImplicitParam(name = "applicationType",
                    paramType = "header",
                    required = true,
                    defaultValue = "web",
                    value = "Application Types",
                    dataType = "string"),

            @ApiImplicitParam(name = "Accept-Language",
                    paramType = "header",
                    required = true,
                    defaultValue = "en",
                    value = "Accept-Language",
                    dataType = "string")
    ])

    def deletePublication(String publicationId) {
        Publication pub = Publication.findById(publicationId as Long)
        pub.delete()
        render(pub as JSON)
    }

    def getCurators(Publication publication) {
        JSONObject returnObject = new JSONObject()
        JSONObject curators = new JSONObject()
        println "find all by publication ${publication} -> ${publication.doi}"
        def curationActivities = CurationActivity.findAllByPublication(publication)


        println "curation activity found ${curationActivities} "

        curationActivities.each {
            println "it: ${it}"
            JSONObject jsonObject = new JSONObject(
                    username: it.user.username,
                    status: it.curationStatus.name()
            )

            curators.put(it.user.username, jsonObject)
        }

        println "curator list: ${curators as JSON}"

        List<GHUser> assignableUserList = githubService.getAssignable()
        List<GHUser> ghUserList = githubService.getAssigned(publication)

        println "totla user list ${assignableUserList.size()}"
        println "gh user list ${ghUserList.size()}"
        assignableUserList = assignableUserList - ghUserList
        println "assignable user list ${assignableUserList.size()}"

        // get all users from the DB
        List<String> usernameList = assignableUserList.collect { it.login }
        println "username list ${usernameList}"
        Set<String> existingUsers = User.findAllByUsernameInList(usernameList).collect { it.username } as Set
        assignableUserList = assignableUserList.findAll {
            existingUsers.contains(it.login)
        }

        println "github curator list: ${curators as JSON}"

        for (GHUser user in ghUserList) {
            if (!curators.containsKey(user.login)) {
                println "not contained for ${user.login} so creating"
                def curatorObject = new JSONObject(
                        username: user.login,
                        status: CurationStatusEnum.ASSIGNED.name()
                )
                curators.put(user.login, curatorObject)
            } else {
                println "Contained by ${user.login} "
            }
        }

        for (GHUser user in assignableUserList) {
            def curatorObject = new JSONObject(
                    username: user.login,
                    status: CurationStatusEnum.ASSIGNABLE.name()
            )
            curators.put(user.login, curatorObject)
        }

        returnObject.curators = curators.values()
        render returnObject as JSON
    }

    @Transactional
    def sendToPublisher() {
        println "send to publisher ${params}"
        println "json: ${request.JSON}"
        def publication = Publication.findById(request.JSON.publication.id)
        def user = User.findById(request.JSON.user.id)
        def inputObject = request.JSON
        println "input object ${inputObject as JSON}"
        println "pub ${publication as JSON}"
        println "uewr ${user as JSON}"

        publication = publicationService.validateLinks(publication)

        // get publisher
        User defaultPublisher = userService.getDefaultPublisher()
        // assign to publisher
        githubService.assignOnly(publication, defaultPublisher.username)
        // create a pub comment for the publisher that it is ready
        githubService.addComment(publication, "@${user.username} marked publication ready for publisher review @${defaultPublisher.username}")
        // set pub status
        publication.status = PublicationStatusEnum.CURATOR_FINISHED
        // add pub label
        githubService.synchronizeLabelStatus(publication)


        publication.save(flush: true, insert: false)

        render publication as JSON

    }

    @Transactional
    def cancelSendToPublisher() {
        println "CANCEL send to publisher ${params}"
        println "json: ${request.JSON}"
        def publication = Publication.findById(request.JSON.publication.id)
        def user = User.findById(request.JSON.user.id)
        def inputObject = request.JSON
        println "input object ${inputObject as JSON}"
        println "pub ${publication as JSON}"
        println "uewr ${user as JSON}"

        // TODO:
        // get publisher
        User defaultPublisher = userService.getDefaultPublisher()
        // assign to publisher
        githubService.unassignOnly(publication, defaultPublisher.username)
        githubService.assignOnly(publication, user.username)
        // create a pub comment for the publisher that it is ready
        githubService.addComment(publication, "@${user.username} is going to continue to curate the pub and is not ready for review by @${defaultPublisher.username}")
        // set pub status
        publication.status = PublicationStatusEnum.CURATING
        // add pub label
        githubService.synchronizeLabelStatus(publication)

        publication.save(flush: true, insert: false)

        render publication as JSON
    }


    @Transactional
    def createAnnotation() {
        println "set annotation: ${params}"
        println "json: ${request.JSON}"
        def publication = Publication.findById(request.JSON.publication.id)
        def user = User.findById(request.JSON.user.id)
        String status = request.JSON.status
        def inputObject = request.JSON
        println "input object ${inputObject as JSON}"
        println "status ${status}"
        println "pub ${publication as JSON}"
        println "uewr ${user as JSON}"

        CurationActivity curationActivity = CurationActivity.findByPublicationAndUser(publication, user)
        println "curation activate ${curationActivity as JSON}"

        CurationStatusEnum curationStatus = CurationStatusEnum.getForString(status)
        if (!curationActivity) {
//            CurationActivity curationActivity = CurationActivity.executeUpdate(" Match (u:User), (p:Publication) where u.username=:user and p.doi = :doi create ca:CurationActivity { values ) create u-r:R1-ca-r:R2-p return ca ")
            String inputUUID = UUID.randomUUID().toString()


//            CurationActivity.executeUpdate("MATCH (p:Publication),(u:User) where p.doi={doi} and u.username = {username} create (p)-[r:REVIEWERS]->(ca:CurationActivity {uuid:'"+inputUUID+"',curationStatus:'"+curationStatus+"',version:0})<-[c:Curators]-(u) RETURN ca ",[doi:publication.doi,username:user.username])
//            curationActivity = CurationActivity.executeQuery("MATCH (ca:CurationActivity) where ca.uuid={uuid} return ca",[uuid:inputUUID])[0] as CurationActivity
//            curationActivity.curationStatus = curationStatus
            curationActivity = new CurationActivity( curationStatus: curationStatus )
            curationActivity.save(flush: true, failOnError:true)
            curationActivity.publication = publication
            curationActivity.save(flush: true, failOnError:true)
            curationActivity.uuid = inputUUID
            curationActivity.save(flush: true, failOnError:true)
            user.addToCurators(curationActivity)
            curationActivity.user = user
            curationActivity.save(flush: true, failOnError:true)
        } else {
//            curationActivity.curationStatus = curationStatus
//            curationActivity.save(flush: true, failOnError: true)
        }

//        curationActivity.curationStatus = CurationStatusEnum.getForString(status)
//        curationActivity.save(flush: true, failOnError: true)


        switch (curationStatus) {
            case CurationStatusEnum.STARTED:
                publication.status = PublicationStatusEnum.CURATING
                publication.save(flush: true)
                githubService.synchronizeLabelStatus(publication)
                githubService.assign(publication,user)
                githubService.addStartComment(publication,user)
                githubService.synchronizeLabelStatus(publication)
                break
            case CurationStatusEnum.FINISHED:
            case CurationStatusEnum.NOT_ANNOTATED:
                githubService.unassign(publication,user)
                githubService.addFinishComment(publication,user)
                break
        }

        return getCurators(publication)
//        render new JSONObject() as JSON
    }


    @Transactional
    def assignUser() {
        println "assigning a user ${request.JSON}"
        String username = request.JSON.user.username
        Publication publication = Publication.findByDoi(request.JSON.publication.doi)

        println "username: ${username}"
        println "publicadtion: ${publication}"

        githubService.assignOnly(publication, username)

        render getCurators(publication) as JSON
    }

    @Transactional
    def unassignUser() {

        String username = request.JSON.user.username
        Publication publication = Publication.findByDoi(request.JSON.publication.doi)

        githubService.unassignOnly(publication, username)

        render getCurators(publication) as JSON
    }

    /**
     * 1. Get all lexicon markups for the publication
     * 2. Test each one and count as valid or invalid
     * 3. Return a list of valid and invalid ones
     *
     *
     * @param publication
     * @return
     */
    @Transactional
    def validateLinks(Publication publication) {
        publication = publicationService.validateLinks(publication)
        render publication as JSON
    }

}
