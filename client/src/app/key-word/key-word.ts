import {Lexicon} from "../lexicon/lexicon";
import {KeyWordSet} from './key-word-set';
import {Markup} from "../markup/markup";
export class KeyWord {

    value: string;
	uuid: string;
	keyWordSet: KeyWordSet;
    lexica: Lexicon[];
    markups: Markup[];
    markupCount: number;

    isSynonym(lexicon:Lexicon):boolean{
        alert('value: '+this.value);
        alert('lexicon : '+lexicon.publicName);
        return lexicon.publicName==this.value;
    }

}
