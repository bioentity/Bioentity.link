package com.insilico.dmc.publication

import grails.util.Environment

class PublicationIngesterJob {

   def publicationLoadService

    static triggers = {
//      simple repeatInterval: 5000l // execute job once in 5 seconds
//        simple startDelay: 1000l,repeatInterval: 8*60*60*1000l // execute job three times a day
//        simple startDelay: 100*1000l,repeatInterval: 2*60*60*1000l // execute every two hours
    }

    def execute() {

        if(Environment.currentEnvironment!=Environment.TEST){
            publicationLoadService.loadGSAPubs(5)
        }

        // execute job
    }
}
