package com.insilico.dmc

import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource
import com.insilico.dmc.lexicon.LexiconSourceClassEnum
import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Session


import static org.springframework.http.HttpStatus.*

@Transactional(readOnly = true)
class LexiconSourceController extends RestfulController {
    static responseFormats = ['json', 'xml']

    def lexicaCaptureService
    def scheduledJobService

    LexiconSourceController() {
        super(LexiconSource)
    }

    @Transactional
    def uploadCSV(LexiconSource lexiconSource) {
        println "receiving file ${params.keySet().join("::")} and ${lexiconSource as JSON}"


        println "A.1"
        println "A.2"
        if(!scheduledJobService.canRunJob()){
            println "A"
            throw new RuntimeException("Running too many jobs")
        }
        println "B"

        //  NOTE: if put is too large (over 2 MB), lexicacsv will not be populated
        def file = params.lexicacsv
        Map<String, Object> params = new HashMap<>()
        Set<String> lexiconSet = new HashSet<>()
        Map<String, Map> mapOfLexicas = [:]
        file.eachLine { line ->
            if (!line.startsWith("#") && !line.startsWith("id")) {
                //println "hear is the line: $line"
                def rowMap = [:]
                def rowSplit = line.split('\t')
                rowMap['id'] = rowSplit[0]
                rowMap['uuid'] = UUID.randomUUID().toString()
                rowMap['lexiconSourceUUID'] = UUID.randomUUID().toString()
                rowMap['className'] = lexiconSource.className.toString()
                rowMap['speciesId'] = lexiconSource.species.id.toString()
                rowMap['speciesName'] = lexiconSource.species.name.toString()
                rowMap['taxonId'] = lexiconSource.species.taxonId.toString()
                rowMap['lexiconSourceUUID'] = lexiconSource.uuid
                rowMap['urlConstructor'] = lexiconSource.urlConstructor.toString()

                //TODO reduce syntax
                if (rowSplit.size() > 1) {
                    rowMap['name'] = rowSplit[1]
                } else {
                    rowMap['name'] = ''
                }
                if (rowSplit.size() > 2) {
                    rowMap['synonym'] = rowSplit[2]
                } else {
                    //for UNWIND command
                    rowMap['synonym'] = ''
                }
                if (rowSplit.size() > 3) {
                    rowMap['notes'] = rowSplit[3]
                } else {
                    rowMap['notes'] = ''
                }
                while (mapOfLexicas.containsKey(rowMap.uuid)) {
                    rowMap.uuid = UUID.randomUUID().toString()
                }
                mapOfLexicas.put(rowMap.uuid, rowMap)

            }
        }
        params.put("props", mapOfLexicas.values())

        println "original size ${mapOfLexicas.size()}"

        JSONObject jobData = new JSONObject()
        jobData.lexiconSource = lexiconSource.toString()
        String uuid = UUID.randomUUID().toString()

        if(!scheduledJobService.generateAndStartJob(mapOfLexicas.size())){
            throw new RuntimeException("Running too many jobs or job size too large ${mapOfLexicas.size()}")
        }

        Set<String> uuids = mapOfLexicas.collect() {
            it.value.uuid
        } as Set
        println "final size: ${uuids.size()}"

        //println "handling lines: ${file.split("\n").size()}"

        int count = 0
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic(EnvironmentUtil.username, EnvironmentUtil.password))
        Session session = driver.session()
        println "Loading into Neo4j"

        session.run("""
             UNWIND {props} as row
             MERGE (species:Species {name:row.speciesName})
             MERGE (lexiconSource:LexiconSource {uuid:row.lexiconSourceUUID})
             SET lexiconSource.uuid = row.lexiconSourceUUID
             
             CREATE (lexicon:Lexicon {externalModId:row.id})
             SET lexicon.publicName = row.name
             SET lexicon.synonym = row.synonym
             SET lexicon.curatorNotes = row.notes 
             SET lexicon.uuid = row.uuid
             SET lexicon.isActive = true
  
             MERGE (species)<-[:SPECIES]-(lexiconSource)

             MERGE (lexicon)<-[:LEXICA]-(lexiconSource)
 
              """, params)

        session.close()
        println "session closed."


        JSONObject returnObject = new JSONObject()
        returnObject.lexiconSource = lexiconSource
        returnObject.lexicaCount = lexiconSet.size()
        //println "BUILD object: ${returnObject as JSON}"
        scheduledJobService.finishJob()

        render returnObject as JSON
    }

    def classNames() {

        def lexiconNames = LexiconSourceClassEnum.values().collect() {
            it.name()
        }
        render lexiconNames as JSON
    }


    private static JSONObject generateReturnObject(LexiconSource lexiconSource){
        JSONObject jsonObject = new JSONObject()
        jsonObject.id = lexiconSource.id
        jsonObject.uuid = lexiconSource.uuid
        jsonObject.className = lexiconSource.className.name()
        jsonObject.source = lexiconSource.source
        jsonObject.prefix = lexiconSource.prefix
        jsonObject.urlConstructor = lexiconSource.urlConstructor
        jsonObject.file = lexiconSource.file
        jsonObject.url = lexiconSource.url
        JSONObject speciesObject = new JSONObject(
                name: lexiconSource.species.name,
                taxonId: lexiconSource.species.taxonId,
        )
        jsonObject.species = speciesObject
        jsonObject.notes = lexiconSource.notes

        return jsonObject
    }

    def index() {
        JSONArray lexiconSourceArray = new JSONArray()
        List<Node> nodeList = LexiconSource.executeQuery("MATCH (ls:LexiconSource)--(l:Lexicon) RETURN ls as lexiconSource,count(l) as lexicaCount")
        nodeList.each {
            LexiconSource lexiconSource = it["lexiconSource"] as LexiconSource
            Integer lexicaCount = it["lexicaCount"] as Integer
            JSONObject jsonObject = generateReturnObject(lexiconSource)
            jsonObject.lexicaCount = lexicaCount
            lexiconSourceArray.add(jsonObject)
        }
        List<Node> nodeList2 = LexiconSource.executeQuery("MATCH (ls:LexiconSource)--(s:Species) where not (ls)--(:Lexicon) RETURN ls as lexiconSource, s as species")
        nodeList2.eachWithIndex { it, index ->
            // not array needed if just a single object returned
            LexiconSource lexiconSource = it["lexiconSource"] as LexiconSource

            Species species = it["species"] as Species

//            println "species ${species}"

            Integer lexicaCount = 0

            JSONObject jsonObject = generateReturnObject(lexiconSource)
//            jsonObject.id = lexiconSource.id
//            jsonObject.uuid = lexiconSource.uuid
//            jsonObject.className = lexiconSource.className.name()
            jsonObject.lexicaCount = lexicaCount
//            jsonObject.source = lexiconSource.source
//            jsonObject.prefix = lexiconSource.prefix
//            jsonObject.urlConstructor = lexiconSource.urlConstructor
//            jsonObject.file = lexiconSource.file
//            jsonObject.url = lexiconSource.url
//
//
//            JSONObject speciesObject = new JSONObject(
//                    name: species.name,
//                    taxonId: species.taxonId,
//            )
//            jsonObject.species = speciesObject
//			jsonObject.notes = lexiconSource.notes


            lexiconSourceArray.add(jsonObject)
        }

        render lexiconSourceArray as JSON
    }

    def getLexica(LexiconSource lexiconSource) {
        if (params.search?.size() > 2) {
			def lexicaCount = LexiconSource.executeQuery("MATCH (l:Lexicon)--(ls:LexiconSource) WHERE ls.uuid = {ls} AND (l.publicName CONTAINS {search} OR l.externalModId CONTAINS {search} OR l.synonym CONTAINS {search} OR l.curatorNotes CONTAINS {search}) RETURN COUNT(l) AS lexicaCount", [ls: lexiconSource.uuid, search: params.search])
	 		def nodeList = LexiconSource.executeQuery("MATCH (l:Lexicon)--(ls:LexiconSource) WHERE ls.uuid = {ls} AND (l.publicName CONTAINS {search} OR l.externalModId CONTAINS {search} OR l.synonym CONTAINS {search} OR l.curatorNotes CONTAINS {search}) RETURN l AS lexica SKIP {offset} LIMIT {max}", [ls: lexiconSource.uuid, search: params.search, max: params.max.toInteger(), offset: params.offset.toInteger()])
            def jsonObject = new JSONObject()
			jsonObject.lexica = new JSONArray()
			jsonObject.lexicaCount = lexicaCount[0]
			for(def n in nodeList) {
				jsonObject.lexica.add(n as Lexicon)
			}
			/*
			def lexica = Lexicon.findAllByLexiconSourceAndPublicNameIlike(lexiconSource, params.search + "%", [max: params.max, offset: params.offset])
            if (!lexica) {
                lexica = Lexicon.findAllByLexiconSourceAndExternalModIdIlike(lexiconSource, params.search + "%", [max: params.max, offset: params.offset])
            }
			if(!lexica) {
				lexica = Lexicon.findAllByLexiconSourceAndSynonymIlike(lexiconSource, params.search + "%", [max: params.max, offset: params.offset])
			}
			if(!lexica) {
				lexica = Lexicon.findAllByLexiconSourceAndNotesIlike(lexiconSource, params.search + "%", [max: params.max, offset: params.offset])
			}
			*/
			
            respond jsonObject
        } else {
            //def lexica = Lexicon.findAllByLexiconSource(lexiconSource, [max: params.max, offset: params.offset])
			def lexicaCount = LexiconSource.executeQuery("MATCH (l:Lexicon)--(ls:LexiconSource) WHERE ls.uuid = {ls} RETURN COUNT(l) AS lexicaCount", [ls: lexiconSource.uuid])
            def jsonObject = new JSONObject()
			jsonObject.lexica = new JSONArray()
			jsonObject.lexicaCount = lexicaCount[0]
			def nodeList = LexiconSource.executeQuery("MATCH (l:Lexicon)--(ls:LexiconSource) WHERE ls.uuid = {ls} RETURN l AS lexica SKIP {offset} LIMIT {max}", [ls: lexiconSource.uuid, max: params.max.toInteger(), offset: params.offset.toInteger()])
			for(def n in nodeList) {
				jsonObject.lexica.add(n as Lexicon)
			}
        	respond jsonObject
		}
    }

    def getLexicon(LexiconSource lexiconSource) {
        def lexicon = Lexicon.findByLexiconSourceAndPublicName(lexiconSource, params.word)
        respond lexicon
    }

    def getLexicaCount(LexiconSource lexiconSource) {
        if (params.search?.size() > 2) {

            def lexica = Lexicon.findAllByLexiconSourceAndPublicNameIlike(lexiconSource, params.search + "%")
            if (!lexica) {
                lexica = Lexicon.findAllByLexiconSourceAndExternalModIdIlike(lexiconSource, params.search + "%")
            }
            if (!lexica) {
                lexica = Lexicon.findAllByLexiconSourceAndSynonymIlike(lexiconSource, params.search + "%")
            }
            if (!lexica) {
                lexica = Lexicon.findAllByLexiconSourceAndNotesIlike(lexiconSource, params.search + "%")
            }
           respond lexicaCount: lexica.size()
        } else {
            def lexica = Lexicon.findAllByLexiconSource(lexiconSource)
            respond lexicaCount: lexica.size()
        }
    }

    @Transactional
    def save(LexiconSource lexSource) {
        println "has a lexicon source species ${lexSource}"
        if (lexSource.hasErrors()) {
            println "ERRORS ${lexSource.errors}"
            respond lexSource.errors, view: 'create'
        }
        // For some reason the species is submitted without id, so we have to fetch it
        def species = Species.findByTaxonId(lexSource.species.taxonId)
        println "found a species ${species}"
        lexSource.species = species
        lexSource.save(flush: true)
        println "saved ${lexSource}"
        withFormat {
            html {
                flash.message = message(code: 'default.created.message', args: [message(code: 'lexSource.label', default: 'LexiconSource'), lexSource.id])
                redirect lexSource
            }
            '*' { respond lexSource, status: CREATED }
        }
//		if(lexSource.timer != null) {
//			println "create new job"
//			IngestSourceJob.schedule(60000 * lexSource.timer, -1, [sourceType: "lexicon", sourceId: lexSource.id])
//		}
    }

    @Transactional
    def update() {
        LexiconSource lexSource = LexiconSource.findByUuid(request.JSON.uuid)
        lexSource.source = request.JSON.source
        lexSource.prefix = request.JSON.prefix
        lexSource.file = request.JSON.file
        lexSource.notes = request.JSON.notes
        lexSource.url = request.JSON.url
        lexSource.protocol = request.JSON.protocol
        lexSource.username = request.JSON.username
        lexSource.notes = request.JSON.notes
        lexSource.urlConstructor = new URL(request.JSON.urlConstructor)
        lexSource.className = LexiconSourceClassEnum.valueOf(request.JSON.className)

        lexSource.species = null
        lexSource.species = Species.findByTaxonId(request.JSON.species.taxonId)

        lexSource.save(flush:true,failOnError:true)
        if (lexSource == null) {
            render status: NOT_FOUND
            return
        }
//        int deleted = LexiconSource.executeUpdate("MATCH (n:LexiconSource)-[r]-(s:Species) where n.uuid={uuid} delete r",[uuid:lexSource.uuid])
//        println "deleted ${deleted} for ${lexSource.uuid}"
//        int created = LexiconSource.executeUpdate("MATCH (n:LexiconSource),(s:Species) where n.uuid={uuid} and s.taxonId={taxonId} create (n)-[:SPECIES]->(s)",[uuid:lexSource.uuid,taxonId: lexSource.species.taxonId])
//        println "created ${created} for ${lexSource.uuid} and ${lexSource.species.taxonId}"

        LexiconSource.executeUpdate("match (l:Lexicon)-[r]-(ls:LexiconSource) where ls.uuid = {uuid} set l.link = null  return l limit 10"
                ,[uuid:lexSource.uuid])



        render generateReturnObject(LexiconSource.findByUuid(lexSource.uuid)) as JSON

    }

    @Transactional
    def clear(LexiconSource lexiconSource) {
        println "trying to clear ${lexiconSource}"
        int deleted = lexicaCaptureService.clearLexica(lexiconSource)
        JSONObject jsonObject = new JSONObject()
        jsonObject.deleted = deleted
        render jsonObject as JSON
    }

    def download(LexiconSource lexiconSource) {
        String generatedName = "${lexiconSource.source}-${lexiconSource.className}-${lexiconSource.prefix}-${lexiconSource.species}"
        response.setHeader("Content-disposition", "attachment; filename=${generatedName}.tsv")
        def outputStream = response.outputStream

        def nodeList = LexiconSource.executeQuery("MATCH (n:LexiconSource {uuid:{uuid}})--(l:Lexicon) RETURN {id:l.externalModId ,publicName:l.publicName,synonym:l.synonym,notes:l.notes}", [uuid: lexiconSource.uuid])
        println "size ${nodeList.size()}"

        int count = 0
        for (n in nodeList) {
            String nodeString = n.id+"\t"+(n.publicName?:'')+"\t"+(n.synonym?:'')+"\t"+(n.notes?:'')+"\n"
            outputStream << nodeString

            if (count % 500 == 0) {
                outputStream.flush()
            }

            ++count
        }

        outputStream.flush()
        outputStream.close()
    }
}
