package com.insilico.dmc.markup

import com.insilico.dmc.publication.Publication
import com.insilico.dmc.Species

/**
 * Created by nathandunn on 4/23/17.
 */
class MarkupObject{
    String lookupTerm
    Species species
    String className

    // article + links
    Map<Publication,Integer> publicationMap = [:]

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        MarkupObject that = (MarkupObject) o

        if (lookupTerm != that.lookupTerm) return false

        return true
    }

    int hashCode() {
        return (lookupTerm != null ? lookupTerm.hashCode() : 0)
    }
}

