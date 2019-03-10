import {Publication} from "../publication/publication";
import {Lexicon} from "../lexicon/lexicon";
import {KeyWord} from "../key-word/key-word";
export class Markup {

	id: number;
    work: string;
    start: number;
    end: number;
    path: string;
    uuid: string;
	extLinkId: string;
    status: string; // status of the markup . . . verified, etc.
    finalLexicon: Lexicon; // status of the markup . . . verified, etc.
    lexica: Lexicon[]; // status of the markup . . . verified, etc.
    keyWord: KeyWord;
    publication: Publication;


}
