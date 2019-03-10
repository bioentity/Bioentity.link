import { Component, OnInit, Output, EventEmitter } from '@angular/core';

import { LexiconService } from '../../lexicon/lexicon.service';
import { Species } from '../../species/species';
import { Lexicon } from '../../lexicon/lexicon';

@Component({
  selector: 'search-list',
	providers: [ LexiconService ],
  templateUrl: './search-list.component.html',
  styleUrls: ['./search-list.component.css']
})
export class SearchListComponent implements OnInit {

	species: Species[];
    classNames: string[];
	search: string;
	selectedSpecies: Species;
	selectedClass: string;
	@Output() lexica = new EventEmitter <Lexicon[]>();

	lexicaBySource: {[source: string]: Lexicon[]};
	sources: string[];


	constructor(private lexiconService: LexiconService) { }

	ngOnInit() {
		this.updateSpeciesList();
		this.updateClassNames();
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

	searchLexica() {
		this.lexiconService.searchLexicon(this.search, this.selectedClass, this.selectedSpecies).subscribe(applicationData => {
//			this.lexica.emit(applicationData);
			this.lexicaBySource = {};
			for (let lex of applicationData) {
				let source = lex.lexiconSource.source;
				if(!this.lexicaBySource[source]) {
					this.lexicaBySource[source] = [];
				}
				this.lexicaBySource[source].push(lex);
			}
			this.sources = Object.keys(this.lexicaBySource);
		});
	}

	select(source) {
		this.lexica.emit(this.lexicaBySource[source]);
	}

}
