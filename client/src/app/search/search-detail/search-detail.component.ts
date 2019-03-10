import { Component, OnInit, Input } from '@angular/core';

import { Lexicon } from '../../lexicon/lexicon';

@Component({
  selector: 'search-detail',
  templateUrl: './search-detail.component.html',
  styleUrls: ['./search-detail.component.css']
})
export class SearchDetailComponent implements OnInit {

	@Input() lexica: Lexicon[];

  constructor() { }

  ngOnInit() {
  }

}
