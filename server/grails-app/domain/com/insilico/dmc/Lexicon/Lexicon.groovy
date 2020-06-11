package com.insilico.dmc.lexicon

import com.insilico.dmc.markup.KeyWord
import com.insilico.dmc.markup.Markup
import com.insilico.dmc.user.Curator
class Lexicon {

    String uuid
    String publicName // formerly entity - column 1 in ingest
    String externalModId // for external link - column 2 in ingest
    String synonym // pipe-separated - column 3 in ingest
    String curatorNotes // added after ingest
    Short ignoreDuringMarkup
    Short possiblyAmbiguous
    boolean isActive
    String comments
	String reasonForAdding
	Date dateAdded
	Curator addedBy

    // foreign keys?
    LexiconSource lexiconSource // I think this is the database_id

    String link
    String internalLink

    String findLink() {
        //if(!this.link && this.lexiconSource?.urlConstructor){
        //    this.link = this.link ?: this.lexiconSource.urlConstructor.toExternalForm().replace("@@ID@@", this.externalModId);
        //}
        //return this.link
        this.lexiconSource.urlConstructor.toExternalForm().replace("@@ID@@", this.externalModId)
    }

    def findInternalLink() {
        if(!this.internalLink && this.lexiconSource && this.externalModId){
            this.internalLink = this.lexiconSource.prefix +':'+ this.externalModId
        }
        return this.internalLink
    }

    static belongsTo = LexiconSource

    static hasMany = [
            markups : Markup,
            keyWords: KeyWord
    ]

    static constraints = {
        publicName nullable: true, blank: false, size: 1..1000
        lexiconSource nullable: true
        externalModId unique: false // should be true, but we'll leave this for now
        synonym nullable: true, size: 1..1000, blank: false
        isActive nullable: false
        link nullable: true
        uuid nullable: false, unique: true
    }

    static mapping = {
        publicName defaultValue: "", type: "text"
        curatorNotes type: "text"
        isActive defaultValue: true
        uuid defaultValue: UUID.randomUUID().toString()
//        publicName index: true
    }

    List<String> getSynonyms() {
        return synonym ? synonym.split("\\|") as List<String> : []
    }

}
