import {KeyWord} from "./key-word";
import {LexiconSource} from "../lexicon/lexicon-source";

export class KeyWordSet {

    id: any ;
    uuid: string;
    sources: LexiconSource[];
    keywords: KeyWord[];
    name: string ;
    description: string ;
    keyWordCount: number;
    isHidden: boolean;

    constructor(){
    }

    ngOnInit(){
    }

}
