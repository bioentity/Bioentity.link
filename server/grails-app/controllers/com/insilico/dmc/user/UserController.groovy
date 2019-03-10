package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import com.insilico.dmc.user.Role
import grails.converters.JSON
import grails.rest.*
import com.insilico.dmc.user.User
import grails.transaction.Transactional
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

class UserController extends RestfulController<User> {
    static responseFormats = ['json', 'xml']

    def githubService

    UserController() {
        super(User)
    }

    def usersForFilter(String filter) {
        println "input filter: ${filter}"
        println "params: ${params}"
        switch (filter) {
            case "All":
                render User.listOrderByUsername() as JSON
                break
            case "Active":
                render User.findAllByActive(true, [sort: "username", order: "asc"]) as JSON
                break
            case "Inactive":
                render User.findAllByActive(false, [sort: "username", order: "asc"]) as JSON
                break
            default:
                println "there is a problem with ${filter}"
                render User.listOrderByEmail() as JSON
        }
    }

    def index() {
        def filter = params.filter
        usersForFilter(filter)
    }

    @org.springframework.transaction.annotation.Transactional
    def findByUsername() {
        println "params ${params}"
        String username = params.username
        println "username ${username}"
        User user = User.findByUsername(username)
        println "user ${user}"
        if(!user.defaultRole){
            user.defaultRole = user.roles ? user.roles[0] : null
            user.save(flush: true )
        }
        println "roles ${user.roles}"
        println "default Role ${user.defaultRole}"
        render user as JSON
    }

    def roles() {
        def roles = Role.listOrderByName()
        println "roles: ${roles}"
        render roles as JSON
    }

    @Transactional
    def deactivate() {
        def userObject = request.JSON
        User user = User.findByUsername(userObject.username)
        user.active = false
        user.save(flush: true)
        usersForFilter(userObject.userFilter)
    }

    @Transactional
    def activate() {
        def userObject = request.JSON
        User user = User.findByUsername(userObject.username)
        user.active = true
        user.save(flush: true)
        usersForFilter(userObject.userFilter)
    }

    @Transactional
    def update() {
        def userObject = request.JSON

        User user = User.findById(userObject.id)

        user.username = userObject.username
        user.email = userObject.email
        user.firstName = userObject.firstName
        user.lastName = userObject.lastName

        if (user.defaultRole.id != userObject.defaultRole.id) {
            println "updating role : ${userObject as JSON}"
            Role role = Role.findById(userObject.defaultRole.id)
            println "updated role ${role}"
            user.defaultRole = role
            user.roles.clear()
            user.addToRoles(role)
        }
        user.save(failOnError: true, flush: true)

        render user as JSON
    }

    @Transactional
    def save() {
        def userObject = request.JSON

        User user = new User(
                username: userObject.username,
                firstName: userObject.firstName,
                lastName: userObject.lastName,
                email: userObject.email,
                active: true,
        ).save(failOnError: true, flush: true)

        Role role = Role.findByName(userObject.defaultRole.name)

        user.addToRoles(role)
        user.save(flush: true, failOnError: true)

        index()
    }

    def publications() {
        String username = params.username

        Map<String, JSONObject> pubArray = new HashMap<>()

        def pubResults = Publication.executeQuery("MATCH (u:User)--(ca:CurationActivity)--(p:Publication) where u.username={username} RETURN ca.curationStatus as status,p as publication", [username: username])
        for (p in pubResults) {
            Publication publication = p["publication"] as Publication
            JSONObject returnObject = new JSONObject()
            returnObject.title = publication.title
            returnObject.journal = publication.journal
            returnObject.doi = publication.doi
            returnObject.status = publication.status.name()
            returnObject.curationStatus = p["status"] as String
            returnObject.received = publication.received
            returnObject.accepted = publication.accepted
            returnObject.ingested = publication.ingested
            returnObject.finalized = publication.finalized
            returnObject.lastEdited = publication.lastEdited
            pubArray.put(publication.doi, returnObject)
        }

        // enrich the pubs with other assignees?
        def assignedPublicationsForUser = githubService.getPublicationsForUser(username)
        def assignedPubDois = new JSONObject()
        assignedPublicationsForUser.each() { it ->
            assignedPubDois.put(it.doi, it)
        }

        def assignedPubs = Publication.executeQuery("MATCH (p:Publication) where p.doi in {dois} return p ", [dois: assignedPubDois.keySet()])
        for (p in assignedPubs) {
            Publication publication = p as Publication
            JSONObject returnObject = pubArray.get(publication.doi)
            if (!returnObject) {
                println "NOT FOUND ${publication.doi}"
                returnObject = new JSONObject()
                returnObject.title = publication.title
                returnObject.journal = publication.journal
                returnObject.doi = publication.doi
                returnObject.status = publication.status.name()
                returnObject.curationStatus = 'ASSIGNED'
                returnObject.received = publication.received
                returnObject.accepted = publication.accepted
                returnObject.ingested = publication.ingested
                returnObject.finalized = publication.finalized
                returnObject.lastEdited = publication.lastEdited
            }
            def publicationObject = assignedPubDois.get(publication.doi)
            returnObject.assigned = publicationObject.assigned
            pubArray.put(publication.doi, returnObject)
        }
        // get list of pubs with dois that are not above


        render pubArray.values() as JSON
    }

    def alive(){
        render new JSONObject() as JSON
    }
}
