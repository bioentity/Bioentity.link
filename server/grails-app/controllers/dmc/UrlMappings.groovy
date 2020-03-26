package dmc

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        "/application/"(controller: 'application', action:'index')
        "/publication/findByFileName/${xmlFileName}.xml"(controller: 'publication', action:'findByFileName')
        "/publication/storeByFileName/${xmlFileName}.xml"(controller: 'publication', action:'storeByFileName')
        "/publication/find/$id"(controller: "publication", action: "find",[params:params])
        "/publication/find/$doiPrefix/$doiSuffix"(controller: "publication", action: "findByDoi")
        "/publication/lexica/$doiPrefix/$doiSuffix"(controller: "publication", action: "fetchLexica")

        "/"(view:"/index")
        "/notFound"(view:"/notFound")
        "/error"(view:"/error")
        "500"(view:'/error')
        "404"(view:'/notFound')

		"/apidoc/$action?/$id?"(controller: "apiDoc", action: "getDocuments")
		"/api/v1/publication/$pubId"(controller: 'publication', action: 'getPublication', method: "GET")
		"/api/v1/publication/list"(controller: 'publication', action: 'getPublicationList', method: "GET")
		"/api/v1/publication/createUpdate"(controller: 'publication', action: 'createOrUpdatePublication', method: "POST")
		"/api/v1/publication/$pubId"(controller: 'publication', action: 'deletePublication', method: "DELETE")


    }
}
