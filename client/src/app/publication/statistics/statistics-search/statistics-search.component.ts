import {Component, Input, OnInit} from '@angular/core';
import {Publication} from "../../publication";

@Component({
  selector: 'statistics-search',
  templateUrl: './statistics-search.component.html',
  styleUrls: ['./statistics-search.component.css']
})
export class StatisticsSearchComponent implements OnInit {

  @Input() selectedPub: Publication;

  constructor() { }

  ngOnInit() {
  }

}
