import {Component, Input, OnInit} from '@angular/core';
import {Publication} from "../publication";
import {PublicationService} from "../publication.service";

@Component({
    selector: 'publication-index',
    templateUrl: './publication-index.component.html',
    styleUrls: ['./publication-index.component.css']
})
export class PublicationIndexComponent implements OnInit {

    @Input() selectedPub: Publication;
    @Input() fullscreen: boolean;
    @Input() expand: boolean;

    indices: any ;
    creating: boolean ;

    constructor(private publicationService: PublicationService) {
        this.creating = false ;
        this.indices = [];
    }

    ngOnInit() {
        if(this.selectedPub){
            this.publicationService.getIndices(this.selectedPub).subscribe(applicationData => {
                this.indices = applicationData;
            });
        }
    }

    ngOnChanges(){
        if(this.selectedPub){
            this.publicationService.getIndices(this.selectedPub).subscribe(applicationData => {
                this.indices = applicationData;
            });
        }
    }

    clearIndexPub() {
        if(this.selectedPub){
            this.publicationService.clearIndices(this.selectedPub).subscribe(applicationData => {
                this.indices = applicationData;
            });
        }
    }

    createPublicationIndex() {
        if(this.selectedPub){
            this.creating = true ;
            this.publicationService.createIndices(this.selectedPub).subscribe(applicationData => {
                this.publicationService.getIndices(this.selectedPub).subscribe(applicationData => {
                    this.indices = applicationData;
                    this.creating = false ;
                });
            });
        }
    }

    downloadPubIndices(doi: string) {
        this.publicationService.downloadIndices(this.selectedPub);
    }

}
