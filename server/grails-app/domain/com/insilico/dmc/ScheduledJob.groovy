package com.insilico.dmc

class ScheduledJob {

    static constraints = {
        uuid nullable: false, unique: true
        jobData nullable: false
        dateStarted nullable: false
        active nullable: false
        jobSize nullable: true
        lastUpdated nullable: true
    }

    String uuid
    String jobData
    Integer jobSize
    Boolean active = true 
    Date dateStarted
    Date dateStopped
    Date lastUpdated
}
