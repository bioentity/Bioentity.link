package com.insilico.dmc

import grails.util.Environment

class EnvironmentUtil {

    static String getUsername(){
        return "neo4j"
    }

    static String getPassword(){
        Environment.current == Environment.PRODUCTION  ? "ctmtckYhQ2Xy9MQn" : "LAT8gjThKJ26TR"
    }
}
