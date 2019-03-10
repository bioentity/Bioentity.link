package com.insilico.dmc.publication

enum PublicationStatusEnum {

    INGESTED(10),
    INGESTION_ERROR(15),
    MARKED_UP(20),
    CURATING(30),
    CURATOR_FINISHED(40),
    PUB_APPROVED(50),
    CLOSED(60),

    private int status

    protected PublicationStatusEnum(int newStatus) {
        this.status = newStatus
    }

    int getStatus() {
        return status
    }

    String getLabelText(){
        return name().toLowerCase().split("_").collect{ it.capitalize()}.join(" ").toUpperCase()
    }

    static PublicationStatusEnum getEnumForString(String statusString){
        if(!statusString) return null
        for(v in values()){
            if(v.name()==statusString){
                return v
            }
        }
        println "not found "
        return null

    }
}

