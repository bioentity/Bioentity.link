import {Component, Input, OnInit, ViewChild} from '@angular/core';

import {Lexicon} from '../lexicon';
import {LexiconSource} from '../lexicon-source';
import {LexiconService} from '../lexicon.service';
import {Species} from '../../species/species';
import {UUID} from "angular2-uuid";

@Component({
    selector: 'lexicon-detail',
    providers: [LexiconService],
    templateUrl: './lexicon-detail.component.html',
    styleUrls: ['./lexicon-detail.component.css']
})
export class LexiconDetailComponent implements OnInit {

    @Input() selectedLex: Lexicon;
    @Input() selectedSource: LexiconSource;
    @ViewChild('acc') acc;

    csvFile: string;
    reader: FileReader = new FileReader();
    lexiconCount: number;
    progress = 0;
    uploadCount = 0;
    alertMessage = "";
    uploadAlert = false;
    species: Species[];
    classNames: String[];
    isCollapsed = true;

    constructor(private lexiconService: LexiconService) {
    }

    ngOnInit() {
        this.updateSpeciesList();
        this.updateClassNames();
    }

    ngOnChanges() {
        this.isCollapsed = true;
    }


    fileChangeEvent(event) {
        let f = event.target.files[0];

        this.reader.onload = (e) => {
            this.csvFile = this.reader.result;
        };

        this.reader.readAsText(f);
    }


    uploadLexica() {
        if (!this.csvFile) {
            return;
        }
        let lines = this.csvFile.split("\n").filter(
            line => line && !line.startsWith("#")
        );

        this.lexiconCount = lines.length;
        console.log(this.lexiconCount);
        this.progress = 0;
        this.uploadCount = 0;
        for (let line of lines) {
            let columns = line.split("\t");
            let lexicon = new Lexicon();
            // let synonyms = columns[2].split("|");
            lexicon.externalModId = columns[0];
            lexicon.publicName = columns[1];
            lexicon.synonym = columns[2];
            lexicon.lexiconSource = this.selectedSource;
            lexicon.uuid = UUID.UUID();
            setTimeout(300);
            this.lexiconService.createLexicon(lexicon).subscribe(applicationData => {
                this.uploadCount += 1;
                this.progress = (this.uploadCount / this.lexiconCount) * 100;
                if (this.progress == 100.0) {
                    // alert('full progress');
                    this.acc.toggle('upload-panel');
                    // Close upload panel
                    // Update lexicon list
                }
            });
        }
    }

    clearLexica() {
        this.lexiconService.clearLexicon(this.selectedSource).subscribe(applicationData => {
            alert('Lexica Cleaned');
        });
    }

    uploadBulkLexica(chunk: number) {
        // let lines = this.csvFile.split("\n").filter(
        //     line => line && !line.startsWith("#")

        if (!this.csvFile) {
            this.alertMessage = "You must choose a file";
            this.uploadAlert = true;
            return;
        }
        this.alertMessage = "Uploading lexica...";
        this.uploadAlert = true;

        let step = 2000000;
        if (chunk + step < this.csvFile.length) {
            while (!this.csvFile.endsWith('\n', chunk + step)) {
                step -= 1;
            }
        }
        this.lexiconService.uploadNewLexica(this.csvFile.substring(chunk, chunk + step), this.selectedSource).subscribe(applicationData => {

                if (chunk + step < this.csvFile.length) {
                    this.uploadBulkLexica(chunk + step);
                } else {
                    this.uploadAlert = false;
                    this.acc.toggle('upload-bulk-panel');
                }
            },
            error => {
                alert('Unable to upload new lexica as job is already too large or a job is already running');
            }
        );
    }

    updateSource() {
        this.lexiconService.updateLexiconSource(this.selectedSource).subscribe(applicationData => {
            this.selectedSource = applicationData;
        });
        this.isCollapsed = true;
    }

    updateClassNames() {
        this.lexiconService.getClassNames().subscribe(applicationData => {
            this.classNames = applicationData;
        });
    }

    updateSpeciesList() {
        this.lexiconService.getSpecies().subscribe(applicationData => {
            this.species = applicationData;
        });
    }

    byId(item1: Species, item2: Species) {
        if (item2) {
            return item1.taxonId === item2.taxonId;
        }
    }

    downloadLexica() {
        console.log('downloading lexica: ' + JSON.stringify(this.selectedSource));
        this.lexiconService.downloadLexicon(this.selectedSource);
    }
}
