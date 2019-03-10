package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import grails.converters.*
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.kohsuke.github.GHCommitComment
import org.kohsuke.github.GHIssue

@Transactional
class GithubController {
    static responseFormats = ['json', 'xml']


    def githubService
    def publicationService



    def generatePublicationLink(Publication publication) {
        if (!publication) {
            response.status = 404
            return
        }

        if (!publication.githubLink) {
            GHIssue issue = githubService.getIssueForPublication(publication)

            if(issue){
                githubService.autoAssignForPub(publication,publicationService.getUsersForPublication(publication))
            }
            else{
                log.error("Could not find issue")
            }

        } else {
            println "found the github link issue"
        }


        render publication as JSON
    }

    def getComments(Publication publication) {

        if (!publication) {
            response.status = 404
            return
        }

        GHIssue issue = githubService.getIssueForPublication(publication)
        List<GHCommitComment> comments = issue.getComments()

        JSONArray array = new JSONArray()
        comments.each { comment ->
            println "COMMENT: ${comment}"
            if (comment.body && comment.body.trim().size() > 1) {
                JSONObject jsonObject = new JSONObject()
                jsonObject.comment = comment?.body?.size() > 50 ? comment.body.substring(0, 50) + " ..." : comment?.body
                jsonObject.authorName = comment.user.name
                jsonObject.authorLink = comment.user.htmlUrl
                jsonObject.date = comment.createdAt.format("yyyy-MMM-dd")

                if(comment.url){
                    try {
                        // NOTE: we get rate limits when pulling the API this way often
                        def remoteUrl = JSON.parse(comment.url.text)
                        jsonObject.url = remoteUrl.html_url
                        jsonObject.issueUrl = remoteUrl.issue_url
                    } catch (e) {
                        println "ignoring 403 ${e.message}"
                        jsonObject.url = publication.githubLink
                        jsonObject.issueUrl = publication.githubLink
                    }
                }
                array.add(jsonObject)
            }
        }

        render array as JSON
    }
}
