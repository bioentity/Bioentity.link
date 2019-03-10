package com.insilico.dmc

import com.insilico.dmc.user.User
import grails.transaction.Transactional

@Transactional
class UserService {


    def getDefaultPublisher(){
        User user = User.findByUsername("bioentity-publisher")
        return user
    }
}
