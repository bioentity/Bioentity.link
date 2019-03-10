import { Component, OnInit } from '@angular/core';

import { Lexicon } from '../lexicon/lexicon';

@Component({
  selector: 'search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

	lexica: Lexicon[];

  constructor() { }

  ngOnInit() {
  }

	lexiconSearch(lexica: Lexicon[]) {
		this.lexica = lexica;
	}

}
