import {Injectable} from "@angular/core";
import {Species} from "../../species/species";
import {LexiconSource} from "../../lexicon/lexicon-source";
import {Markup} from "../../markup/markup";
import {Lexicon} from "../../lexicon/lexicon";
import {Publication} from "../publication";
import {environment} from "../../../environments/environment";
import { Http, Response, RequestOptions } from '@angular/http';
import {Observable} from "rxjs/Observable";
import {KeyWordSet} from "../../key-word/key-word-set";
import {UUID} from 'angular2-uuid';

@Injectable()
export class StatisticsService {

    species: any;
    lexiconSource: any;
    keyWords: any;
    filteredKeyWords: any;
    markups: any;
    markupKeys: any;
    _linkedWords: Observable<any>;
    private _markupSources: Observable<any>;


    constructor(private http: Http) {
        this.species = [];
        this.species.push(new Species("yeast", "NCBI:123"));
        this.species.push(new Species("worm", "NCBI:345"));
        this.species.push(new Species("fly", "NCBI:678"));
        this.species.push(new Species("fish", "NCBI:9891"));


        this.lexiconSource = [];
        this.lexiconSource.push(new LexiconSource(1, "Gene", this.species[0], "SGD","SGD", "http://sgd.org/@@ID@@",UUID.UUID()));
        this.lexiconSource.push(new LexiconSource(2, "Gene", this.species[1], "WB","Wormbase", "http://wormbase.org/@@ID@@",UUID.UUID()));
        this.lexiconSource.push(new LexiconSource(3, "Gene", this.species[2], "FB","Flybase", "http://flybase.org/@@ID@@",UUID.UUID()));
        this.lexiconSource.push(new LexiconSource(4, "Gene", this.species[3], "ZFIN","ZFIN", "http://zfin.org/@@ID@@",UUID.UUID()));

        this.keyWords = [
            "couptf3", "couptf4", "couptf5", "ppara2", "ppardb", "igfbp2a", "dlc", "dbx1b", "dbx2", "dbx1a", "anos1b", "pbx4", "anos1a", "crestin", "calr3a", "urod", "mixl1", "kifc1", "col11a2", "bmp7a", "ccl27a", "zic1", "copg2", "cmn", "robo1", "robo3", "tpst1", "nccrp1", "btg2", "hug", "rbp4", "cat", "inhbaa", "meis2b", "khdrbs1a", "chico", "vdra", "nme2b.1", "nme2b.2", "nme3", "nme7", "twist1a", "twist3", "pcna", "phox2a", "igf2bp3", "actc1b", "tnnc2", "tnnt3a", "pvalb2", "myhz1.1", "mylz3"
        ];

        this.markups = this.getMarkups();

    }

    getSpecies() {
        return this.species;
    }

    getLexiconSources() {
        return this.lexiconSource;
    }

    getKeyWords() {
        let returnArray = [];
        for(let k of this.keyWords){
            if(this.markups[k] && this.markups[k].length>0){
                returnArray.push(k);
            }
        }
        return returnArray;
    }

    getMarkups() {
        if(this.markups){
            return this.markups;
        }
        let markups = {};
        // each keyWord had 7 markups
        for (let k of this.keyWords) {
            let markupArray = [];
            let numMarkups = Math.round(Math.random()*8);
            for (let i = 0; i < numMarkups; i++) {
                let markup = new Markup();
                markup.start = (i * 100) + 7;
                markup.end = markup.start + k.length;
                markup.keyWord = k;

                markup.lexica = [];
                for(let source of this.lexiconSource){
                    if(Math.random() < 0.5){
                        let lexicon = new Lexicon();
                        lexicon.externalModId = "ZDB-GENE-990415-200";
                        lexicon.publicName = markup.keyWord.value;
                        lexicon.lexiconSource = source;
                        markup.lexica.push(lexicon);
                    }
                }
                if(markup.lexica.length==0){
                    let lexicon = new Lexicon();
                    lexicon.externalModId = "ZDB-GENE-990415-200";
                    lexicon.publicName = markup.keyWord.value;
                    lexicon.lexiconSource = this.lexiconSource[0];
                    markup.lexica.push(lexicon);
                }

                let lexicaIndex = Math.round(Math.random() * markup.lexica.length);
                if (lexicaIndex >= markup.lexica.length) {
                    lexicaIndex = markup.lexica.length - 1;
                }
                markup.finalLexicon = markup.lexica[lexicaIndex];
                markup.finalLexicon.lexiconSource = markup.lexica[lexicaIndex].lexiconSource;
                markupArray.push(markup);
            }
            if(markupArray.length>0){
                markups[k] = markupArray;
            }
        }


        return markups;
    }

    getIndexedTerms(pub:Publication,kws:KeyWordSet) {
        this.http.get(environment.serverUrl + 'publication/getIndexedTerms/' + pub.id + '?kws='+kws.id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
    }

    getLinkedTerms(pub:Publication):Observable<any> {
        this._linkedWords = this.http.get(environment.serverUrl + 'publication/getLinkedTerms/' + pub.id + '?doi='+pub.doi)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._linkedWords;
    }

    getMarkupSource(pub: Publication) {
        this._markupSources = this.http.get(environment.serverUrl + 'publication/markupSources/' + pub.id)
            .map((res: Response) => res.json())
            .publishReplay()
            .refCount();
        return this._markupSources;
    }
}
