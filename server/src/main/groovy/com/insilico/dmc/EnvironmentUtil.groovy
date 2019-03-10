package com.insilico.dmc

import grails.util.Environment

class EnvironmentUtil {

    static String getUsername(){
        return "neo4j"
    }

    static String getPassword(){
        Environment.current == Environment.PRODUCTION  ? "LAT8gjThKJ26TR" : "LAT8gjThKJ26TR"
    }
}
