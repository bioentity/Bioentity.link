package com.insilico.dmc

import grails.transaction.NotTransactional
import grails.transaction.Transactional

//import grails.transaction.Transactional

//@NotTransactional
//@Transactional
class ScheduledJobService {

    final int MAX_JOBS = 1
    final int MAX_SIZE = 350000

    static int jobsRunning = 0

    Integer activeJobs() {
        return jobsRunning
//        return ScheduledJob.executeQuery("MATCH (sj:ScheduledJob) where sj.active=true  RETURN count(sj) LIMIT 1")[0] as Integer
    }

    Boolean canRunJob(Integer jobSize = 0) {

        println "jobs running ${jobsRunning} < ${MAX_JOBS} vs ${jobSize} < ${MAX_SIZE}"
        if(jobsRunning<MAX_JOBS && jobSize < MAX_SIZE){
            println "can run a job"
            return true
        }

        println "not running "


        return false
    }

    def generateJob() {
        ++jobsRunning
    }

    Boolean generateAndStartJob(Integer size = 0) {
        println "generating and start job for  ${size} "
        try{
            println "A going to start one ${activeJobs()}"
            if (canRunJob(size)) {
                println "B going to start one ${activeJobs()}"
                generateJob()
                println "C going to start one ${activeJobs()}"
                println "job start registerd ${size} "
                return true
            }
            else{
                println "can not run job for  ${size}"
            }
        }
        catch (e) {
            println "error: ${e}"
        }
        return false
    }

    Boolean finishJob() {
        if(jobsRunning>0){
            --jobsRunning
        }
        return true
    }
    def inactivateAllJobs() {
        jobsRunning = 0
    }

//    Boolean updateJobStatus(String uuid, int jobSize, String jobData) {
//        println "updating job ${uuid} with ${jobSize} and ${jobData}"
//
//        def scheduledJob = ScheduledJob.findByUuid(uuid)
//        if (scheduledJob) {
//            int updated = ScheduledJob.executeUpdate("MATCH (sj:ScheduledJob) where sj.uuid = {uuid} " +
//                    "set sj.jobSize = {jobSize}, sj.lastUpdated=timestamp(), " +
//                    " sj.jobData = {jobData}  return sj"
//                    ,[uuid:uuid,jobData: jobData])
//            if(jobSize > MAX_SIZE){
//                println "job size is too big so aborting"
//                finishJob(uuid)
//                return false
//            }
//            return true
//        }
//
//        println "job for $uuid is not found"
//        return false
//    }
}
