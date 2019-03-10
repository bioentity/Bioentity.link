import { Component, OnInit, Input } from '@angular/core';

import { Lexicon } from '../../lexicon/lexicon';

@Component({
  selector: 'search-content',
  templateUrl: './search-content.component.html',
  styleUrls: ['./search-content.component.css']
})
export class SearchContentComponent implements OnInit {

	@Input() lexica: Lexicon[];

  constructor() { }

  ngOnInit() {
  }

	ngOnChange() {
		if(this.lexica) {
			console.log(this.lexica.length);
		}
	}

}
