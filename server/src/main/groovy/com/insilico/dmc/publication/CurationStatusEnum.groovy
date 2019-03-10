package com.insilico.dmc.publication

enum CurationStatusEnum {

    NOT_ANNOTATED,
    ASSIGNED,
    ASSIGNABLE,
    STARTED,
    FINISHED,

    static CurationStatusEnum getForString(String s) {
        for(n in CurationStatusEnum.values()){
            if(s==n.name()){
                return n
            }
        }
        return  null
    }
}