import {Injectable} from "@angular/core";
import {Http, Response} from "@angular/http";
import {LexiconSource} from "../lexicon/lexicon-source";
import {KeyWordSet} from "./key-word-set";
import {KeyWord} from './key-word';
import {Observable} from "rxjs/Observable";
import {environment} from "../../environments/environment";

@Injectable()
export class KeyWordService {

    _keyWordSets: Observable<any>;
    _selectedKeyWords: Observable<any>;

    constructor(private http: Http) {
    }

    generateKeyWordSet(selectedSources: LexiconSource[], kwsName: string): Observable<any> {
        this._keyWordSets = this.http.post(environment.serverUrl + 'keyWordSet/save',selectedSources, {params: {"name": kwsName}})
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._keyWordSets;
    }

    getKeyWordSets() : Observable<any> {
        this._keyWordSets = this.http.get(environment.serverUrl + 'keyWordSet/')
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._keyWordSets;

    }

    getKeyWords(kws: KeyWordSet, search: string, offset: number, max: number, filter: string) : Observable<any> {
        this._selectedKeyWords = this.http.get(environment.serverUrl + 'keyWordSet/'+ kws.id + '?max=' + max + '&offset=' + offset + '&search=' + search+'&filter='+filter)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._selectedKeyWords;

    }


    deleteKeyWordSet(removeId: number) {

        this._keyWordSets = this.http.delete(environment.serverUrl + 'keyWordSet/'+removeId)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._keyWordSets;
    }

    deleteKeyWord(removeId: number) {

        return this.http.get(environment.serverUrl + 'keyWordSet/deleteKeyWord/?id=' +removeId)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    showMarkups(uuid: string) {
        return this.http.get(environment.serverUrl + 'keyWordSet/findMarkups/' + uuid )
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

	addKeyWord(keyWord: any, kws: KeyWordSet) {
		return this.http.get(environment.serverUrl + 'keyWordSet/addKeyWord/' + kws.id, {params: {lexicon: keyWord.lexica.id, value: keyWord.value, uuid: keyWord.uuid}})
			.map((res: Response) => res.json())
			.publishReplay()
			.refCount();
	}

    downloadAll(kwsUuid: string) {
        window.open(environment.serverUrl + 'keyWordSet/all/' + kwsUuid,'_blank');
    }

    downloadSynonym(kwsUuid: string) {
        window.open(environment.serverUrl + 'keyWordSet/synonym/' + kwsUuid,'_blank');
    }

    downloadPrimary(kwsUuid: string) {
        window.open(environment.serverUrl + 'keyWordSet/primary/' + kwsUuid,'_blank');
    }

    toggleHidden(kws: KeyWordSet) {
        console.log(kws.id)
        return this.http.get(environment.serverUrl + 'keyWordSet/toggleHidden/' + kws.id)
			.map((res: Response) => res.json())
			.publishReplay()
			.refCount();
    }

}
