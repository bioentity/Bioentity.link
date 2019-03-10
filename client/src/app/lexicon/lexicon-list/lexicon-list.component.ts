import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';

import {LexiconService} from '../lexicon.service';
import {Lexicon} from '../lexicon';
import {LexiconSource} from '../lexicon-source';
import {Species} from '../../species/species';
import {UUID} from "angular2-uuid";

@Component({
    selector: 'lexicon-list',
    providers: [LexiconService],
    templateUrl: './lexicon-list.component.html',
    styleUrls: ['./lexicon-list.component.css']
})
export class LexiconListComponent implements OnInit {

    lexica: Lexicon[];
    @Output() selectedSource = new EventEmitter<LexiconSource>();
    lexiconSources: LexiconSource[];
    species: Species[];
    classNames: String[];


    newLexiconSource = new LexiconSource(null, null, null, null, null, null, UUID.UUID());
    removeId: number;
    selectedSpecies: Species;
    selectedType: string;

    constructor(private lexiconService: LexiconService, private modalService: NgbModal) {
    }

    ngOnInit() {
        this.updateLexiconSources();
        this.updateSpeciesList();
        this.updateClassNames();
    }

    updateLexica() {
        this.lexiconService.getLexica().subscribe(applicationData => {
            this.lexica = applicationData;
        });
    }

    updateLexiconSources() {
        this.lexiconService.getLexiconSources().subscribe(applicationData => {
            if (this.selectedSpecies != null) {
                this.lexiconSources = new Array<LexiconSource>();
                for (let source of applicationData) {
                    if (source.species.taxonId === this.selectedSpecies.taxonId) {
                        this.lexiconSources.push(source);
                    }
                }
            } else {
                this.lexiconSources = applicationData;
            }
            if (this.selectedType != null) {
                this.lexiconSources = this.lexiconSources.filter(this.isType, this);

            }

        });
    }


    setLexiconById(id: any) {
        this.lexiconService.getLexiconSources().subscribe(applicationData => {
            this.lexiconSources = applicationData;

            // iterate through for id
            for (let lexiconSource of this.lexiconSources) {
                if (lexiconSource.id == id) {
                    this.select(lexiconSource);
                    return;
                }
            }
        });
    }

    isType(source: LexiconSource, i: number, sources: LexiconSource[]) {
        return (this.selectedType.toUpperCase() === source.className.toUpperCase());
    }


    updateSpeciesList() {
        this.lexiconService.getSpecies().subscribe(applicationData => {
            this.species = applicationData;
        });
    }

    updateClassNames() {
        this.lexiconService.getClassNames().subscribe(applicationData => {
            this.classNames = applicationData;
        });
    }

    select(source: LexiconSource) {
        //this.lexiconService.getLexicon(lexicon.id).subscribe(applicationData => {
        this.selectedSource.emit(source);
        //});
    }

    createLexiconSource() {
        this.lexiconService.createLexiconSource(this.newLexiconSource).subscribe(applicationData => {
            this.newLexiconSource = new LexiconSource(null, null, null, null, null, null, UUID.UUID());
            this.updateLexiconSources();
        });
    }

    confirmDelete() {
        this.lexiconService.deleteLexiconSource(this.removeId).subscribe(applicationData => {
            this.updateLexiconSources();
        });
    }

    open(content, id: any) {
        this.modalService.open(content);
        this.removeId = id;
    }

    filterBySpecies(species: Species) {
        this.selectedSpecies = species;
        this.updateLexiconSources();
    }

    filterByType(type: string) {
        this.selectedType = type;
        this.updateLexiconSources();
    }

}
