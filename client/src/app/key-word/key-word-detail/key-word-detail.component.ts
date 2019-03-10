import {Component, Input, OnInit} from '@angular/core';
import {KeyWordSet} from "../key-word-set";

@Component({
  selector: 'key-word-detail',
  templateUrl: './key-word-detail.component.html',
  styleUrls: ['./key-word-detail.component.css']
})
export class KeyWordDetailComponent implements OnInit {

  @Input() selectedKWS: KeyWordSet ;
  constructor() { }

  ngOnInit() {
  }

}
