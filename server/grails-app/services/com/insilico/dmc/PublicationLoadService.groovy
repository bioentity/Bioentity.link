package com.insilico.dmc

import com.insilico.dmc.publication.PubWrapper
import com.insilico.dmc.publication.Publication
import grails.transaction.Transactional
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.kohsuke.github.GHContent
import org.kohsuke.github.GitHub


@Transactional
class PublicationLoadService {

    def publicationService
    def githubService

    final String GSA_PUB_URL = "http://textpresso-dev.caltech.edu/"
    final String GSA_WORM_PUB_DIR = "gsa/worm/incoming_xml/"
    final String GSA_FLY_PUB_DIR = "gsa/fly/incoming_xml/"
    final String GSA_YEAST_PUB_DIR = "gsa/yeast/incoming_xml/"
    final String MOST_RECENT_FILES_COMMAND = "?C=M;O=D"
    final String GSA_GH_URL = "InSilicoLabs/GSA-pubs"
//    final String GH_URL ="https://github.com"

    final String GSA_WORM_PUB_URL = GSA_PUB_URL + GSA_WORM_PUB_DIR
    final String GSA_FLY_PUB_URL = GSA_PUB_URL + GSA_FLY_PUB_DIR
    final String GSA_YEAST_PUB_URL = GSA_PUB_URL + GSA_YEAST_PUB_DIR

    final String GSA_MOST_RECENT_FILES_WORM = "${GSA_WORM_PUB_URL}${MOST_RECENT_FILES_COMMAND}".toString()
    final String GSA_MOST_RECENT_FILES_YEAST = "${GSA_YEAST_PUB_URL}${MOST_RECENT_FILES_COMMAND}".toString()
    final String GSA_MOST_RECENT_FILES_FLY = "${GSA_FLY_PUB_URL}${MOST_RECENT_FILES_COMMAND}".toString()


    private long lastTimeCheck = System.currentTimeMillis()


    /**
     * Check GSA FTP pub site.  Only grab the last few.
     *
     * Send anything else to GH.
     *
     * @param maxPubs
     * @param repeatDelaySeconds
     */
    def loadGSAPubs(int maxPubs = 20) {

        println "checking gsa pubs"
        processGSAFilesForURL(GSA_MOST_RECENT_FILES_YEAST, GSA_YEAST_PUB_URL, maxPubs)
        processGSAFilesForURL(GSA_MOST_RECENT_FILES_WORM, GSA_WORM_PUB_URL, maxPubs)
        processGSAFilesForURL(GSA_MOST_RECENT_FILES_FLY, GSA_FLY_PUB_URL, maxPubs)
        processCheckDate()

    }

    List<Publication> processGSAFilesForURL(String recentFilesUrl, String pubUrl, int maxPubs) {

        println "pubUrl: ${pubUrl}"
//        println "ghUrl: ${ghUrl}"
        List<String> xmlCurrentFileNames = getMostRecentFiles(recentFilesUrl)
//        List<String> ghXmlFileNames = getMostRecentGHFiles(ghUrl, ghDirectory, maxPubs)

        println "xmlCurrentFileNames from http pub server: ${xmlCurrentFileNames.join(",")}"
//        println "ghXmlFileNames: ${ghXmlFileNames.join(",")}"
        def publicationFileNames = Publication.findAllByFileNameInList(xmlCurrentFileNames).fileName

        println "pub file names: ${publicationFileNames.join(",")}"

        // only take the filenames we don't have
        xmlCurrentFileNames = xmlCurrentFileNames - publicationFileNames
        println "current file names: ${xmlCurrentFileNames.join(",")}"

        println "size to save: ${xmlCurrentFileNames.size()} vs max: ${maxPubs}"

        xmlCurrentFileNames = xmlCurrentFileNames.size() > maxPubs ? xmlCurrentFileNames.subList(0, maxPubs) : xmlCurrentFileNames


        println "total to save: ${xmlCurrentFileNames.size()} vs max: ${maxPubs}"

        List<PubWrapper> xmlFilesDownloaded = downloadFiles(pubUrl, xmlCurrentFileNames)
        println "pubs wrapped: ${xmlFilesDownloaded.size()}"
//        println "xml files: ${xmlFilesDownloaded.join(',')}"
//        pushPubsToGitHub(ghUrl, ghDirectory, xmlFilesDownloaded)
//        def ghPubs = loadPubsFromGH(ghUrl, ghDirectory, maxPubs)
        def savedPubs = savePubs(xmlFilesDownloaded)
        println "pubs saved: ${savedPubs}"
        println "pubs saved size: ${savedPubs.size()}"
        if(savedPubs.size()>0){
            savedPubs[0].save(flush: true, failOnError: true)
        }

        return savedPubs
    }

    /**
     * Should be a list of pubs
     * @param pubs
     * @return
     */
    List<Publication> savePubs(List<PubWrapper> pubs) {

        List<Publication> returnPubs = []
        for (p in pubs) {
            try {
                Publication.withTransaction {
                    Publication publication = publicationService.ingestPublicationContent(p.fileName, p.content)
                    githubService.getIssueForPublication(publication)
                    returnPubs.add(publication)
                }
            } catch (e) {
                println "problem ingestion ${p.fileName} -> ${e.toString()}"
            }
        }

        return returnPubs
    }

    List<String> processELIFEFilesForURL(String ghUrl, String ghDirectory, int maxPubs) {
        return loadPubsFromGH(ghUrl, ghDirectory, maxPubs)
    }

    List<PubWrapper> loadPubsFromGH(String ghUrl, String ghDirectory, int maxPubs) {
        List<String> gitPubFileNames = getMostRecentGHFiles(ghUrl, ghDirectory, maxPubs)
        GitHub github = GitHub.connect()
        def repo = github.getRepository(ghUrl)

        List<PubWrapper> pubWrapperList = []

        for (String fileName in gitPubFileNames) {
            GHContent fileContent = repo.getFileContent(ghDirectory + fileName)
            PubWrapper pubWrapper = new PubWrapper(
                    fileName: fileName,
                    content: fileContent.content
            )
            pubWrapperList.add(pubWrapper)
        }
        return pubWrapperList
    }

    List<PubWrapper> getPubsFromGh(String url, String directory, int maxPubs) {

        List<PubWrapper> wrapperList = []
        GitHub github = GitHub.connect()
        def repo = github.getRepository(url)
        println "we have the repos ${url} - ${repo}"
        println "GET ghDir ${directory}"
        def contents = repo.getDirectoryContent(directory)

        return wrapperList
    }

    List<String> getMostRecentFiles(String pubUrl) {

        List<String> files = new ArrayList<>()
        Document doc = Jsoup.connect(pubUrl).get()
        for (Element row : doc.select("tr")) {
            String fileName = row.select("td a").attr("href")
            if (fileName.toLowerCase().endsWith("xml")) {
                String metaData = row.select("td[align]").text()
                if (metaData) {
                    Date date = Date.parse("dd-MMM-yyyy hh:mm", metaData)
                    Calendar calendar = GregorianCalendar.getInstance()
                    calendar.setTime(date)

                    int year = calendar.get(Calendar.YEAR)

                    println "date: ${year} -> ${metaData}"
                    if (year < 2015) {
                        return files
                    }
                    files.add(fileName)
//                    Date lastTimeDate = new Date(lastTimeCheck)
//                    if (files.size() > maxPubs || date.before(lastTimeDate)) {
//                    if (files.size() >= maxPubs) {
//                        return files
//                    }
                }
            }
        }
        return files
    }

    /**
     * compile group: 'org.kohsuke', name: 'github-api', version: '1.92'
     *
     * http://github-api.kohsuke.org/
     *
     * @param pubUrl
     * @param mostRecentGHFiles
     * @param maxPubs
     * @return
     */
    List<String> getMostRecentGHFiles(String ghUrl, String ghDirectory, int maxPubs) {
        GitHub github = GitHub.connect()

        def repo = github.getRepository(ghUrl)
        println "we have the repos ${ghUrl} - ${repo}"
        println "GET ghDir ${ghDirectory}"
        def contents = repo.getDirectoryContent(ghDirectory)
        println "from ghDir ${ghDirectory}"
//        println contents.join("::")

        List<String> returnArray = new ArrayList<>()
        for (c in contents) {
            returnArray.add(c.name)
            if (returnArray.size() >= maxPubs) {
                return returnArray
            }
        }

        return returnArray

    }


    List<PubWrapper> downloadFiles(String pubUrl, List<String> xmlFileNames) {
        List<PubWrapper> files = new ArrayList<>()
        for (xmlFileName in xmlFileNames) {
            String downloadUrl = pubUrl + xmlFileName
            println "downloadURL: [${downloadUrl}]"
            String fileContent = new URL(downloadUrl).text
//            geneticsIngester.
            PubWrapper pubWrapper = new PubWrapper(
                    fileName: xmlFileName
                    , content: fileContent
            )
            files.add(pubWrapper)
        }
        return files
    }

    void pushPubsToGitHub(String ghUrl, String ghDirectory, List<String> xmlFilesDownloaded) {
        GitHub github = GitHub.connect()

        def repo = github.getRepository(ghUrl)
        for (PubWrapper file in xmlFilesDownloaded) {
            repo.createContent(file.content, "added", ghDirectory + file.fileName)
        }
    }

    Long processCheckDate() {
        lastTimeCheck = System.currentTimeMillis()
        return lastTimeCheck
    }


}
