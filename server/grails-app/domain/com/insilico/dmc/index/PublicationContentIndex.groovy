package com.insilico.dmc.index

/**
 * This class stores the binary Index object (say, a Patricia Trie)
 */
class PublicationContentIndex {

    byte[] indexData

    static constraints = {
    }

    static mapping = {
        indexData type: 'blob'
    }

}
