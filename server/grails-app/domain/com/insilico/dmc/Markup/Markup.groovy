package com.insilico.dmc.markup

import com.insilico.dmc.lexicon.Lexicon
import com.insilico.dmc.publication.Publication

/**
 * we can use the wordIndex WITH the locationStart/locationEnd within the word
 * Typically the start will be 0 and the end the length of the word described by the lexicon or synonym
 *
 * Should use @MarkupLocationDTO to record location
 */
class Markup {

    Publication publication
    KeyWord keyWord
    Lexicon finalLexicon
    String uuid

    Integer locationStart
    Integer locationEnd
    String path
    String locationJson // I think we can get a JSON object out of this.  Should go to @MarkupLocationDTO
	String extLinkId
    MarkupStatusEnum status
    MarkupTypeEnum type

    static constraints = {
        uuid unique: true
        locationStart nullable: true
        locationEnd nullable: true
        path nullable: true
        status nullable: true // for now, should be false later
        type nullable: true  // for now, should be false later
        locationJson blank: false
        keyWord nullable: true // should be closed probably
        finalLexicon nullable: true
    }

    static mapping = {
        locationJson type: "text"
//        uuid index: true

    }

    static belongsTo = [Publication]

    static hasMany = [
            events: MarkupEvent,
            notes: MarkupNote
    ]
}
