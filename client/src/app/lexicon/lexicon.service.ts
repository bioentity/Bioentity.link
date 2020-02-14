import {Injectable} from '@angular/core';
import {Http, Headers,RequestOptions, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import "rxjs/add/operator/publishReplay";

import {environment} from '../../environments/environment';
import {Lexicon} from './lexicon';
import {LexiconSource} from './lexicon-source';
import {Species} from '../species/species';
import {UUID} from "angular2-uuid";

@Injectable()
export class LexiconService {

    _lexica: Observable<any>;
    _selectedLex: Observable<any>;
    _lexiconSources: Observable<any>;

    constructor(private http: Http) {
    }

    getLexica(): Observable<any> {
        this._lexica = this.http.get(environment.serverUrl + 'lexicon/?max=1000')
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._lexica;
    }

    getLexicaBySource(lexSource: LexiconSource, search, offset, max): Observable<any> {
        return this.http.get(environment.serverUrl + 'lexiconSource/getLexica/' + lexSource.id, {
            "params": {
                "offset": offset,
                "max": max,
                "search": search
            }
        })
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getLexicaCount(lexSource: LexiconSource, search): Observable<any> {
        return this.http.get(environment.serverUrl + 'lexiconSource/getLexicaCount/' + lexSource.id, {"params": {"search": search}})
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getLexicon(id: any): Observable<any> {
        this._selectedLex = this.http.get(environment.serverUrl + 'lexicon/' + id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._selectedLex;
    }

    getLexiconbyIdLookup(id: any): Observable<any> {
        let headers = new Headers();
        headers.append('Content-Type','application/json');
        headers.append('Accept', 'application/json');
        headers.append('Access-Control-Allow-Methods', 'POST, GET, OPTIONS, DELETE, PUT');
        headers.append('Access-Control-Allow-Origin', '*');
        headers.append('Access-Control-Allow-Headers', "X-Requested-With, Content-Type, Origin, Authorization, Accept, Client-Security-Token, Accept-Encoding");

        let options = new RequestOptions({ headers: headers });
        this._selectedLex = this.http.get(environment.serverUrl + 'lexicon/lookup/' + id,options)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._selectedLex;
    }

    getLexiconByModId(id: string) {
        return this.http.get(environment.serverUrl + 'lexicon/getLexicaByModId/' + id)
          .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getLexiconSources(): Observable<any> {
        this._lexiconSources = this.http.get(environment.serverUrl + 'lexiconSource/')
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._lexiconSources;
    }

    createLexicon(lexicon: Lexicon): Observable<any> {
        lexicon.uuid = UUID.UUID();
        return this.http.post(environment.serverUrl + 'lexicon/save/', lexicon)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    uploadLexica(lexicacsv: File, lexiconSource: LexiconSource) {
        let formData: FormData = new FormData();
        formData.append('lexicacsv', lexicacsv);
        return this.http.post(environment.serverUrl + 'lexiconSource/uploadCSV/' + lexiconSource.id, formData)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    deleteLexicon(id: any) {
        return this.http.delete(environment.serverUrl + 'lexicon/' + id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getSpecies(): Observable<any> {
        return this.http.get(environment.serverUrl + 'species/')
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getClassNames() {
        return this.http.get(environment.serverUrl + 'lexiconSource/classNames')
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    createLexiconSource(lexiconSource: LexiconSource) {
        return this.http.post(environment.serverUrl + 'lexiconSource/save', lexiconSource)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    deleteLexiconSource(id: any) {
        return this.http.delete(environment.serverUrl + 'lexiconSource/' + id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

		

    checkLexicon(lexicon: Lexicon) {
        return this.http.post(environment.serverUrl + 'lexicon/lexiconCheck', lexicon)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }



    updateLexicon(lexicon: Lexicon) {
        return this.http.put(environment.serverUrl + 'lexicon/update/' + lexicon.id, lexicon)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    updateLexiconSource(lexSource: LexiconSource) {
        return this.http.put(environment.serverUrl + 'lexiconSource/update/' + lexSource.id, lexSource)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    searchLexicon(lexSearch: string, classType: string, species: Species) {
        let speciesId = "undefined";
        if (species) {
            speciesId = species.id.toString();
        }
        return this.http.get(environment.serverUrl + 'lexicon/search/?term=' + lexSearch + "&class=" + classType + "&species=" + speciesId)
            .map((res: Response) => res.json())
        //	.publishReplay()
        //	.refCount();
    }

    clearLexicon(lexSource: LexiconSource) {
        return this.http.put(environment.serverUrl + 'lexiconSource/clear/' + lexSource.id, lexSource)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    uploadNewLexica(lexicacsv: string, lexiconSource: LexiconSource) {
        let formData: FormData = new FormData();
        formData.append('lexicacsv', lexicacsv);
        return this.http.post(environment.serverUrl + 'lexiconSource/uploadCSV/' + lexiconSource.id, formData)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    downloadLexicon(selectedSource: LexiconSource) {
        window.open(environment.serverUrl + 'lexiconSource/download/'+selectedSource.id,'_blank');
    }
}
