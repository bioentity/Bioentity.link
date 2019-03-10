package com.insilico.dmc.ingester

import org.xml.sax.InputSource

class InternalDtdResolver {

    String gsaDtd = "./src/main/resources/dtd/journal-publishing-dtd-2.3/journalpublishing.dtd"
    /**
     * Return DTD 'systemId' as InputSource.
     * @param publicId
     * @param systemId
     * @return InputSource for locally cached DTD.
     */
    def static entityResolver = [
            resolveEntity: { publicId, systemId ->
                try {
//                    println "A '${publicId}'"
//                    println "A.1 '${systemId}'"

                    // DTD filename:
//                    src/test/resources/publication/2017_worm_xml/gsa/journalpublishing.dtd'
                    String fileName = systemId.split("/").last()
//                    println "filename: ${fileName}"
                    if (fileName == "journalpublishing.dtd") {
                        // choose a local source
                        systemId = new File("./src/main/resources/dtd/journal-publishing-dtd-2.3/journalpublishing.dtd").absolutePath
                    }
                    else
                    if (fileName == "JATS-archivearticle1.dtd") {
                        systemId = new File("./src/main/resources/dtd/journal-archiving-dtd-1.1d3/JATS-archivearticle1.dtd").absolutePath
                    }
//                    else{
//                        println "unkown system ID: ${systemId}"
//                    }
//                    println " fixed systenID: ${systemId}"

                    // move: src/test/resources/publication/2017_worm_xml/gsa/journalpublishing.dtd
                    // to: src/main/resources/dtd/journal-publishing-dtd-2.3/journalpublishing.dtd
//                    println "B: " + InternalDtdResolver.class.getResourceAsStream(".")
//                    println "C: " + InternalDtdResolver.class.getResourceAsStream("/")
//                    println "D:" + new File("/").absolutePath
//                    println "E:" + new File(".").absolutePath
//                    println "F:" + new File("src/main/resources/dtd/journal-publishing-dtd-2.3/journalpublishing.dtd").absolutePath
//                    String inputSource = InternalDtdResolver.class.getResourceAsStream(journalPath)
//                    println "input source: ${inputSource}"
//                    if (inputSource) {
//                        new InputSource(inputSource)
//                    } else {
                        return new InputSource(systemId)
//                    }
                } catch (e) {
                    e.printStackTrace()
                    return null
                }
            }
    ] as org.xml.sax.EntityResolver
}
