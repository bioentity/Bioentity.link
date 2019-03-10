package com.insilico.dmc

//import groovy.transform.EqualsAndHashCode

//@EqualsAndHashCode
class Species {

    String name
    String taxonId

    static constraints = {
        name unique: true
        taxonId unique: true
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Species species = (Species) o

        if (name != species.name) return false
        if (taxonId != species.taxonId) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (taxonId != null ? taxonId.hashCode() : 0)
        return result
    }
}
