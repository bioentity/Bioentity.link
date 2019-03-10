import { Component, OnInit, Input } from '@angular/core';

import { Species } from '../species';

@Component({
  selector: 'species-content',
  templateUrl: './species-content.component.html',
  styleUrls: ['./species-content.component.css']
})
export class SpeciesContentComponent implements OnInit {

	@Input() selectedSpecies: Species;

  constructor() { }

  ngOnInit() {
  }

}
