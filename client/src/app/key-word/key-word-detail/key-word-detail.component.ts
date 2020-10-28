import {Component, Input, OnInit} from '@angular/core';
import {KeyWordSet} from "../key-word-set";
import {KeyWordService} from '../key-word.service'

@Component({
  selector: 'key-word-detail',
  templateUrl: './key-word-detail.component.html',
  styleUrls: ['./key-word-detail.component.css'],
  providers: [KeyWordService]
})
export class KeyWordDetailComponent implements OnInit {

  @Input() selectedKWS: KeyWordSet;

  constructor(private keyWordService: KeyWordService) { }

  ngOnInit() {
  }

  toggleHidden() {
    this.keyWordService.toggleHidden(this.selectedKWS).subscribe(result => {
      this.selectedKWS.isHidden = false
    })
  }

}
