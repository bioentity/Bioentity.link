import {Species} from "../species/species";

export class LexiconSource {

    id: number;
    className: string;
    species: Species;
    source: string;
    prefix: string;
    urlConstructor: string;
    lexicaCount: number;
	notes: string;

    timer: number;
    url: string;
    protocol: string;
    username: string;
    password: string;
    uuid: string;


    constructor(id: number, className: string, species: Species, prefix: string, source: string, url: string, uuid: string) {
        this.id = id;
        this.className = className;
        this.species = species;
        this.prefix = prefix;
        this.source = source;
        this.urlConstructor = url;
        this.uuid = uuid;
    }

    getSourceName(){
        return this.source + "("+this.className +")";
    }

}
