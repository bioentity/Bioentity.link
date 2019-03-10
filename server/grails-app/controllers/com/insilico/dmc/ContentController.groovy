package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import grails.converters.*
import org.grails.web.json.JSONObject

class ContentController {
	static responseFormats = ['json', 'xml']
	
    def index() {
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("ASdf","asdfasdf")
        render jsonObject as JSON
    }


    def publication(Publication publication) {
        println "pub ${publication}"
//        JSONObject publicationObject = publication.properties as JSONObject
//        publicationObject.content = JSON.parse(publication.ingestedData.value)
//        render publicationObject as JSON
//        render publication
//        [publication:publication]
        render publication as JSON
    }
}
