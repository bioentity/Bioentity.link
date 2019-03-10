import { Component, OnInit } from '@angular/core';

import { Species } from './species';

@Component({
  selector: 'species',
  templateUrl: './species.component.html',
  styleUrls: ['./species.component.css']
})
export class SpeciesComponent implements OnInit {

	selectedSpecies: Species;
	
  constructor() { }

  ngOnInit() {
  }

	selectSpecies(species: Species) {
		this.selectedSpecies = species;
	}

}
