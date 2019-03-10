import {PublicationCurationStatus} from "./publication-curation-status";

export class User {

    id: number;
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    defaultRole: any;
    roles: any;

    publications: [PublicationCurationStatus]

    // constructor( ) {
    // }

    constructor( username:string,firstName:string,lastName:string,email:string) {
        this.username = username ;
        this.firstName = firstName ;
        this.lastName = lastName ;
        this.email = email ;
    }

    findDefaultRole(){
        if(this.roles){
            return this.roles[0];
        }

        return null ;
    }

}
