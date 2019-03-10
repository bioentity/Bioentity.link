package com.insilico.dmc

import com.insilico.dmc.publication.CurationActivity
import com.insilico.dmc.publication.Publication
import com.insilico.dmc.publication.PublicationStatusEnum
import com.insilico.dmc.user.User
import grails.converters.JSON
import grails.transaction.Transactional
import grails.util.Environment
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueBuilder
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHRateLimit
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHub

@Transactional
class GithubService {

    final String GH_ORGANIZATION = "bioentity"

    // TODO: bring up
//    final String GH_REPOSITORY = Environment.current == Environment.PRODUCTION ? "dev-papers-v1" : "dev-papers-v1"
//    final String DMSCL_SERVER = Environment.current == Environment.PRODUCTION ? "https://dev.insilico.link/" : "https://dev.insilico.link/"

    final String GH_REPOSITORY = Environment.current == Environment.PRODUCTION ? "papers" : "dev-papers-v1"
    final String DMSCL_SERVER = Environment.current == Environment.PRODUCTION ? "https://bioentity.link/" : "http://localhost:4200/"

    def issuesBeingCreated = []

    @Transactional(readOnly = true)
    private String getPublicationLink(Publication publication) {
        return DMSCL_SERVER + "/#/publication/" + publication.doi
    }

    GHIssue getIssueForPublication(Publication publication) {
        if (Environment.currentEnvironment == Environment.TEST) return null

        GitHub github = GitHub.connect()
        GHRateLimit rateLimit = github.getRateLimit()
        println "RATE LIMIT: ${rateLimit as JSON}"
        println rateLimit.limit + " remain: " + rateLimit.remaining
        println rateLimit.reset



        GHOrganization organization = github.getOrganization(GH_ORGANIZATION)
        GHRepository repo = organization.getRepository(GH_REPOSITORY)


        if (publication.issue) {
            println "found issue within the pub already  ${publication.issue} so returning"
            return repo.getIssue(publication.issue)
        }

        println "trying to get issue "
        GHIssue issue = repo.getIssues(GHIssueState.OPEN).find() {
            return it.title == publication.doi
        }
        println "tried to find an issue for ${publication.doi}"
        if (!issue) {
            if (issuesBeingCreated.contains(publication.doi)) {
                println "already creating an issue for ${publication.doi}"
            } else {
                println "issue not found so reating ${publication.doi}"
                issuesBeingCreated.push(publication.doi)
                GHIssueBuilder issueBuilder = repo.createIssue(publication.doi)
                String pubLink = getPublicationLink(publication)
                issueBuilder.body(pubLink)
                issue = issueBuilder.create()
                issuesBeingCreated.remove(publication.doi)
            }
        }

        if (!issue) {
            throw new RuntimeException("There was an error creating an issue for ${publication.doi}")
        }

        println "html url: " + issue.getHtmlUrl()
        println "api url: " + issue.getApiURL()

        if (!publication.githubLink) {
            publication.githubLink = issue.getHtmlUrl()
            publication.githubApiLink = issue.getApiURL()
            publication.save(flush: true)
        }
        return issue
    }

    def addStartComment(Publication publication, User user) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        addComment(publication, user)
    }

//    def addStartComment(CurationActivity curationActivity) {
//        if (Environment.currentEnvironment == Environment.TEST) return null
//        addStartComment(curationActivity.publication, curationActivity.user)
//    }

    def addComment(Publication publication, User user) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        addComment(publication, "@${user.username} started curating")
    }

    def addFinishComment(Publication publication, User user) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        addComment(publication, "@${user.username} finished curating")
    }

//    def addFinishComment(CurationActivity curationActivity) {
//        if (Environment.currentEnvironment == Environment.TEST) return null
//        addFinishComment(curationActivity.publication, curationActivity.user)
//    }

    def addComment(Publication publication, String text, Boolean showLink = true) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        GitHub github = GitHub.connect()
        GHIssue ghIssue = getIssueForPublication(publication)
        println "found an issue for publication ${ghIssue}"
        String inputText = (showLink ? "   ${getPublicationLink(publication)}\n" : '')
        inputText += text
        ghIssue.comment(inputText)
    }


//    def assign(CurationActivity curationActivity) {
//        if (Environment.currentEnvironment == Environment.TEST) return null
//        assign(curationActivity.publication, curationActivity.user)
//    }

    def assign(Publication publication, User user) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        assignOnly(publication, user.username)
    }

    def unassign(Publication publication,User user) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        unassignOnly(publication, user.username)
    }

//    def unassign(CurationActivity curationActivity) {
//        if (Environment.currentEnvironment == Environment.TEST) return null
//        unassign(curationActivity.publication, curationActivity.user)
//    }

    List<GHUser> getAssigned(Publication publication) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        GHIssue ghIssue = getIssueForPublication(publication)
        println "found an issue for publication ${ghIssue}"
        List<GHUser> assignees = ghIssue.getAssignees()
        println "assignees for issue ${assignees}"
        return assignees
    }

    def assignOnly(Publication publication, String username) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        GitHub github = GitHub.connect()
        GHIssue ghIssue = getIssueForPublication(publication)
        println "found an issue for publication ${ghIssue}"
        List<GHUser> assignees = ghIssue.getAssignees() ?: new ArrayList<>()
        println "assignees for issue ${assignees}"
        GHUser ghUser = github.getUser(username)
        println "gh user ${ghIssue}"

        if (!assignees.contains(ghUser)) {
            println "Assigned user ${ghUser.name} to ${publication.doi}"
            ghIssue.addAssignees(ghUser)
        } else {
            println "User already assigned"
        }
    }

    def unassignOnly(Publication publication, String username) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        GitHub github = GitHub.connect()
        GHIssue ghIssue = getIssueForPublication(publication)
        println "found an issue for publication ${ghIssue}"
        List<GHUser> assignees = ghIssue.getAssignees()
        println "assignees for issue ${assignees}"
        GHUser ghUser = github.getUser(username)
        println "gh user ${ghIssue}"

        if (assignees.contains(ghUser)) {
            println "Removed user ${ghUser.name} from ${publication.doi}"
            ghIssue.removeAssignees(ghUser)
        } else {
            println "User not assigned"
        }
    }

    List<GHUser> getAssignable() {
        if (Environment.currentEnvironment == Environment.TEST) return null

        GitHub github = GitHub.connect()

        GHOrganization organization = github.getOrganization(GH_ORGANIZATION)
        GHRepository repo = organization.getRepository(GH_REPOSITORY)
        return repo.getCollaborators() as List<GHUser>
    }

    def getPublicationsForUser(String username) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        GitHub github = GitHub.connect()
        GHUser ghUser = github.getUser(username)
        GHOrganization organization = github.getOrganization(GH_ORGANIZATION)
        GHRepository repo = organization.getRepository(GH_REPOSITORY)

        List<GHIssue> ghIssueList = repo.getIssues(GHIssueState.OPEN).findAll() {
            return it.assignees.contains(ghUser)
        }

        JSONArray ghIssuesArray = new JSONArray()
        ghIssueList.each {
            JSONObject pubObject = new JSONObject()
            pubObject.doi = it.title
            pubObject.assigned = it.assignees.collect() { assignee ->
                assignee.login
            }
            ghIssuesArray.add(pubObject)
        }

        return ghIssuesArray
    }

    def autoAssignForPub(Publication publication, List<String> usernames) {

        usernames.each {
            this.assignOnly(publication, it)
        }

    }

    def synchronizeLabelStatus(Publication publication) {
        setLabel(publication, publication.status)
    }

    def setLabel(Publication publication, PublicationStatusEnum publicationStatusEnum) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        GitHub github = GitHub.connect()
        GHIssue ghIssue = getIssueForPublication(publication)
        println "found an issue for publication ${ghIssue}"
        println "gh user ${ghIssue}"
        ghIssue.setLabels(publicationStatusEnum.labelText)

    }

    def closePub(Publication publication, User user) {
        if (Environment.currentEnvironment == Environment.TEST) return null
        GitHub github = GitHub.connect()
        GHIssue ghIssue = getIssueForPublication(publication)
        println "found an issue for publication ${ghIssue}"
        ghIssue.comment("Publication closed by @${user.username} because also being deleted from the linking server ${getPublicationLink(publication)}.")
        ghIssue.close()

    }
}
