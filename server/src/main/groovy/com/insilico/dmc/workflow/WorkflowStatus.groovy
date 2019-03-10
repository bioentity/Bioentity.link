package com.insilico.dmc.workflow;

/**
 * Created by nathandunn on 4/5/17.
 */
enum WorkflowStatus {

    READY(10),
    PRECURATION(20),
    IN_CURATION(30),
    POST_CURATION(40),
    FINIAHED(50),
    ;

    private int value ;

    WorkflowStatus(int value){
        this.value = value ;
    }

}
