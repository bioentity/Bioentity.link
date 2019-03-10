import { Component, OnInit, Input } from '@angular/core';

import { Species } from '../species';

@Component({
  selector: 'species-detail',
  templateUrl: './species-detail.component.html',
  styleUrls: ['./species-detail.component.css']
})
export class SpeciesDetailComponent implements OnInit {

	@Input() selectedSpecies: Species;

  constructor() { }

  ngOnInit() {
  }

}
