import { LexiconSource } from './lexicon-source';
import {isArrayLiteralExpression} from "codelyzer/util/astQuery";
import {KeyWord} from "../key-word/key-word";
import {Curator} from "../curator/curator";

export class Lexicon {

	id: number;
    uuid: string;
	publicName: string;
	externalModId: string;
	synonym: string;
	lexiconSource: LexiconSource;
	isActive: boolean;
	comments: string;
	link: string;
    internalLink: string;
	reasonForAdding: string;
	dateAdded: Date;
	addedBy: Curator;
	curatorNotes: string;

	getInternalLink():string{
        if(!this.internalLink) {
            this.internalLink = '#/lexicon/public/' + this.lexiconSource.prefix + ':' + this.externalModId;
        }
        return this.internalLink;
	}

	getLink():string {
		if(!this.link){
            this.link = this.lexiconSource.urlConstructor.replace("@@ID@@",this.externalModId) ;
		}
		return this.link;
	}

	isSynonym(keyWord:KeyWord):boolean{
	    if(this.publicName==keyWord.value){
	        return false ;
        }
        let synonyms = this.synonym.split("|")
        for(let syn of synonyms){
	        if(keyWord.value==syn){
	            return true ;
            }
        }
        console.log("key word not found: "+keyWord.value)
        return null ;
        // if(this.synonym.split("|"))
        //     return synonym ?  synonym.split("\\|") as List<String> : []
    }
}
