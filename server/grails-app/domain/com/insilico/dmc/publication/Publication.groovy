package com.insilico.dmc.publication

import com.insilico.dmc.index.PublicationContentIndex
import com.insilico.dmc.markup.KeyWordSet
import com.insilico.dmc.markup.Markup
import com.insilico.dmc.user.Author
import com.insilico.dmc.user.Curator

//@Resource(uri='/publication')
class Publication {

    String fileName
    String title
    String journal
    // TODO: replace
//    Journal journal
    String doi

    Content originalData // original input form
    PublicationContentIndex originalDataIndex // original input form
    Content exportedData //  data when markup is applied to ingest and then original
    PublicationContentIndex exportedDataIndex // original input form
    PublicationStatusEnum status
	PublicationSource source
//    KeyWordSet markupSource

    Date received
    Date accepted

    Date epubDate
    Date ingested
    Date finalized

	Date lastEdited
    String githubLink
    String githubApiLink
    String linkValidationJson

    static hasMany = [
            markups: Markup
            ,authors: Author
            ,reviewers: CurationActivity
			, markupSources: KeyWordSet
    ]

    static belongsTo = [Author,Curator, KeyWordSet]

    static constraints = {
        doi nullable: true, blank: false
        fileName nullable: false, blank: false, unique: false
        title nullable: true
        journal nullable: true, blank: false

        originalData nullable: true,blank: false
        exportedData nullable: true,blank: false
        status nullable: true
//        markupSource nullable: true

        received nullable: true
        accepted nullable: true
        ingested nullable: true
        epubDate nullable: true
        finalized nullable: true
		lastEdited nullable: true
        githubLink nullable: true
        githubApiLink nullable: true
        linkValidationJson nullable: true
    }

    Integer getIssue(){
        return githubLink ? githubLink.split("/").last().toInteger() : null
    }

    static mapping = {
//        id generator: 'snowflake'
//        doi index: true
//        title index: true
//        originalData index: true

    }

//    String getIdString(){
//        return String.valueOf(id)
//    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Publication that = (Publication) o

        if (doi != that.doi) return false
        if (journal != that.journal) return false
        if (fileName != that.fileName) return false

        return true
    }

    int hashCode() {
        int result
        result = fileName.hashCode()
        result = 31 * result + journal.hashCode()
        result = 31 * result + (doi != null ? doi.hashCode() : 0)
        return result
    }
}
