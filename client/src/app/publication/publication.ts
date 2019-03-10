import {KeyWordSet} from "../key-word/key-word-set";
import {PublicationCurationStatus} from "../user/publication-curation-status";
export enum PublicationStatusEnum {
	INGESTED = 10,
	MARKED_UP = 20,
	CURATING = 30,
	CURATOR_FINISHED = 40,
	PUB_APPROVED = 50,
	CLOSED = 60,

}



export class Publication {

    id: number;
    fileName: string;
    title: string;
    journal: string;
    doi: string;
    abstractText: string;
	status: PublicationStatusEnum;
    markupSources: KeyWordSet[];

    accepted: Date;
    received: Date;

    epubDate: Date;
    ingested: Date;
    finalized: Date;
	lastEdited: Date;
    words: any;
    linkValidationJson:any ;

    markupCount: number; // generated
    githubLink: string;

    curationStatuses: [PublicationCurationStatus];

    getShortTitle():string{
        return this.title.substr(0,20)
    }

    getFileNamePrefix(){
        if(this.fileName){
            this.fileName.split(".")[0];
        }
        return null ;
    }

    constructor( ) {
    }

}
