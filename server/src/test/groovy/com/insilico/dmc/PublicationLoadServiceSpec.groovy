package com.insilico.dmc

import grails.test.mixin.TestFor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.kohsuke.github.GitHub
import spock.lang.Ignore
import spock.lang.Specification

//import javax.json.JsonObject

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(PublicationLoadService)
class PublicationLoadServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "get most recent list of files"() {

        when: "we get recent files"
        String GSA_WORM = "http://textpresso-dev.caltech.edu/gsa/worm/incoming_xml/?C=M;O=D"
        def files = []
        Document doc = Jsoup.connect(GSA_WORM).get()
        for (Element row : doc.select("tr")) {
            String fileName = row.select("td a").attr("href")
            if (fileName.toLowerCase().endsWith("xml")) {
                String metaData = row.select("td[align]").text()
                if (metaData) {
                    Date date = Date.parse("dd-MMM-yyyy hh:mm", metaData)
                    files.add(fileName)
                }
            }
        }

//        println "files: ${files.join(",")}"


        then: "we should get files"
        assert files != null
        assert files.size() > 1
//        assert mostRecentFiles.size()>0

    }

    void "download a file"() {

        when: "we get recent files"
        String GSA_WORM = "http://textpresso-dev.caltech.edu/gsa/worm/incoming_xml/"
        String xmlText = new URL(GSA_WORM + "/300827.xml").text
//        println "xml text: ${xmlText}"
//        def parsed = new XmlSlurper().parse(xmlText)


        then: "we should get files"
        assert xmlText
//        assert mostRecentFiles.size()>0

    }

    @Ignore
    void "get most recent GSA GH files"() {

        when: "we get most recent github files for repos"
        GitHub github = GitHub.connect()
        def repo = github.getRepository("InSilicoLabs/GSA-pubs")
        def contents = repo.getDirectoryContent("gsa/worm")
        println "contents A: ${contents.toString().substring(0, 1000)}"

        then: "we must valid results"
        assert contents != null

    }

    @Ignore
    void "get most recent ELife GH files"() {

        when: "we get most recent github files for repos"
        GitHub github = GitHub.connect()
//        def repo  = github.getRepository("https://github.com/InSilicoLabs/GSA-pubs")
//        def repo  = github.getRepository("https://github.com/elifesciences/elife-article-xml")
        def repo = github.getRepository("elifesciences/elife-article-xml")
        def contents = repo.getDirectoryContent("articles")
        println "contents B: ${contents.toString().substring(0, 1000)}"

        then: "we must valid results"
        assert contents != null

    }

    @Ignore
    void "download GH file"() {

        when: "we download a GH file"
        GitHub github = GitHub.connect()
        def repo = github.getRepository("elifesciences/elife-article-xml")
//        def downloadUrl = repo.getFileContent("https://github.com/elifesciences/elife-article-xml/blob/master/articles/elife-00005-v1.xml").downloadUrl
        def downloadUrl = repo.getFileContent("articles/elife-00005-v1.xml").downloadUrl
        String fileContent = new URL(downloadUrl).text
//        println "fileContent: ${fileContent}"
//        new XmlSlurper().parse(fileContent)


        then: "we have it"
        assert fileContent != null

    }

    @Ignore
    void "get most recent GH files using the method"() {

        when: "we get the most recent GH files"
        def mostRecentFiles = service.getMostRecentGHFiles(service.GSA_GH_URL, service.GSA_WORM_GH_DIRECTORY, 10)

        then: " we should see them come back in order"
        assert mostRecentFiles.size() == 10

    }

//    void "do GH upload using egit"(){
//
//
//        when: "we upload a user info"
//        GitHubClient client = new GitHubClient();
//        String username = System.getenv("GH_USER")
//        String password = System.getenv("GH_PASSWORD")
////        String authToken = System.getenv("GH_TOKEN")
//        String authToken = "bbedf08348d90992efff8b21fe0dc6b4819fa510"
//        client.setOAuth2Token(authToken)
//        RepositoryService repositoryService = new RepositoryService();
//
//        def repos = repositoryService.getRepositories("InSilicoLabs")
//        for(repo in repos){
//            System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
//        }
//
//
//        Repository repository = repositoryService.getRepository("InSilicoLabs","GSA-pubs")
//
//        ContentsService contentsService = new ContentsService()
//
//        List<RepositoryContents> repositoryContents = contentsService.getContents(repository,"gsa/worm")
//
//        println "contents size: ${repositoryContents.size()}"
//
//        RepositoryContents newContent = new RepositoryContents(
//                path: "asdf"
//                ,content: "asasdfasdlfkj"
//        )
//
//        Commit commit = new Commit()
//
//        // TODO: implmement this?  https://stackoverflow.com/q/46215024/1739366
//
////        for(rc in repositoryContents){
////            println rc.name
////        }
//
////        repository.
////        repository.
////        for (Repository repo : service.getRepository("InSilicoLabs","GSA-pubs"))
////            System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
//
//
//        then: "it should get up there"
//        client.setCredentials("user", "passw0rd");
//
//
//    }
//    void "uploading GH with jcabi"() {
//
//        when: "if we have a proper library"
//        String username = System.getenv("GH_USERNAME")
//        String password = System.getenv("GH_PASSWORD")
//        Github github = new RtGithub(username, password)
//        Repo repo = github.repos().get(
//                new Coordinates.Simple("jcabi/jcabi-github")
//        );
//        Content content = new RtContent()
////        Content content = Content.Smart();
////        repo.contents().
//
//
//        then: "it should be added properly"
//
//
//    }


}
