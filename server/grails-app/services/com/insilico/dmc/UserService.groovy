package com.insilico.dmc

import com.insilico.dmc.user.User
import grails.transaction.Transactional

@Transactional
class UserService {


    def getDefaultPublisher(){
        User user = User.findByUsername("bioentity-publisher")
        return user
    }

    def getG3Publisher() {
        User user = User.findByUsername("G3-Database")
        return user
    }

    def getGeneticsPublisher() {
        User user = User.findByUsername("Genetics-Database")
        return user
    }

    def getKWGlobalPublisher() {
        User user = User.findByUsername("KGLOUPGSA")
        return user
    }

    def getKWGeneticsPublisher() {
        User user = User.findByUsername("KGLGENETICSOUP")
        return user
    }

    def get KWG3Publisher() {
        User user = User.findByUsername("KGLG3OUP")
        return user
    }

    def getAdmin() {
        User user = User.findByUsername("kyook")
        return user
    }
}
