package com.insilico.dmc

import com.insilico.dmc.user.User
import grails.rest.*

class DmcslUserController extends RestfulController {
    static responseFormats = ['json', 'xml']
    DmcslUserController() {
        super(User)
    }

    def findBySub(String subString){
        

    }
}
