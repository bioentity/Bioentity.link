import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'markup-list',
  templateUrl: './markup-list.component.html',
  styleUrls: ['./markup-list.component.css']
})
export class MarkupListComponent implements OnInit {

  constructor() { }

  ngOnInit() {
    this.updateMarkup();
  }

  private updateMarkup() {
  }
}
