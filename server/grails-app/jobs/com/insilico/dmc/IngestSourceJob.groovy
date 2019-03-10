package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import com.insilico.dmc.publication.PublicationSource
import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.lexicon.LexiconSource


class IngestSourceJob {


    static triggers = {}

    def execute(context) {
		def sourceType = context.mergedJobDataMap.sourceType
		def sourceId = context.mergedJobDataMap.sourceId

		def localFiles = []
        if(sourceType == "publication") {
			def pubSource = PublicationSource.findById(sourceId)
			for(def pub : Publication.findByPublicationSource(pubSource)) {
				localFiles << pub.fileName
			}
			// TODO: Fetch file list from pub source url, and download / injest anything new
			
			println "start publication job " + sourceId
			def url = "http://localhost/eLife_Fly/".toURL()
			def newFiles = []
			url.eachLine {
				(it =~ /.*<a href="(.*?\.xml)">/).each {
					if(!localFiles.contains(it[1])) {
						newFiles << it[1]
					}
				}
			}

			for(def file : newFiles) {

				// Download and ingest new file
				def xmlFile = new URL(pubSource.url + "/" + file).getText()
				Publication pub = new Publication()
				// Ingester.ingestFile(pub, xmlFile)
				
			}
		}

		if(sourceType == "lexicon") {
			def lexSource = LexiconSource.findById(sourceId)
			for(def lex : Lexicon.findByLexiconSource(lexSource)) {
				localFiles << lex.fileName
			}
			// TODO: Fetch file list from lexicon source url, and download / injest anything new
			println "start lexicon job"
			// This assumes http connection. Need to handle FTP (SFTP?) and login
			def url = lexSource.url.toURL()
			def newFiles = []
			url.eachLine {
				(it =~ /.*<a href="(.*?\.csv)">/).each {
					if(!localFiles.contains(it[1])) {
						newFiles << it[1]
					}
				}
			}
			for(def file : newFiles) {

				// Download and ingest new file
			}

			
		}
    }
}
