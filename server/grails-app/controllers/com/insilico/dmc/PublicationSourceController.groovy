package com.insilico.dmc

import com.insilico.dmc.publication.PublicationSource

import grails.rest.*
import grails.converters.*
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.*

class PublicationSourceController extends RestfulController {
    static responseFormats = ['json', 'xml']
    PublicationSourceController() {
        super(PublicationSource)
    }

	@Transactional
	def save(PublicationSource pubSource) {
		if(pubSource.hasErrors()) {
			respond pubSource.errors, view: 'create'
		}

		// TODO: Create new IngestJob with simple trigger set for the specified timer


		pubSource.save flush:true
			withFormat {
				html {
        		    flash.message = message(code: 'default.created.message', args: [message(code: 'pubSource.label', default: 'PublicationSource'), pubSource.id])
    	        redirect pubSource
	        	}
	        	'*' { render status: CREATED }
    		}
		if(pubSource.timer != null) {
			IngestSourceJob.schedule(60000 * lexSource.timer, -1, [sourceType: "publication", sourceId: pubSource.id])
		}

	}
}
