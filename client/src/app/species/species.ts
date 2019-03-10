export class Species {

	id: number;
    name: string ;
    taxonId: string;

    constructor(name: string,taxonId: string){
        this.name = name ;
        this.taxonId = taxonId ;
    }
}
