package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import grails.converters.*
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.kohsuke.github.GHIssueComment
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
        def comments = issue.getComments()
        JSONArray array = new JSONArray()
        comments.each { comment ->
            if (comment.body && comment.body.trim().size() > 1) {
                JSONObject jsonObject = new JSONObject()
                jsonObject.comment = comment?.body?.size() > 50 ? comment.body.substring(0, 50) + " ..." : comment?.body
                jsonObject.authorName = comment.user.name
                jsonObject.authorLink = comment.user.htmlUrl
                jsonObject.date = comment.createdAt.format("yyyy-MMM-dd")
                jsonObject.issueUrl = comment.url
                // comment.getHtmlUrl() returns null, so just using the issue link
                jsonObject.url = comment.getHtmlUrl()
                array.add(jsonObject)
            }
        }

        render array as JSON
    }
}
