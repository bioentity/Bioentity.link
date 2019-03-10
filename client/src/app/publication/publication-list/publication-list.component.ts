import {Component, EventEmitter, OnInit, Output, ViewChild} from "@angular/core";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Publication, PublicationStatusEnum} from "../publication";
import {PublicationService} from "../publication.service";
import {AuthenticationService} from "../../authentication/authentication.service";
import {SpeciesService} from "../../species/species.service";
import {Species} from "../../species/species";


@Component({
    selector: 'publication-list',
    templateUrl: './publication-list.component.html',
    styleUrls: ['./publication-list.component.css'],
    providers: [SpeciesService]
})
export class PublicationListComponent implements OnInit {

    publications: Publication[];
    @Output() selectedPub = new EventEmitter<Publication>();
    removeId: number;


    addPub = new Publication();
    publicationsSize: number;
    page = 1;
    searchTerm: string;
    pageSize = 10;
    selectedStatus: string;
	selectedSpecies: string;
	speciesList: Species[];
    @ViewChild('deletepub') deletePubModal;


    // TODO: use Enums here

    pubLevels = [
        PublicationStatusEnum[PublicationStatusEnum.INGESTED]
        , PublicationStatusEnum[PublicationStatusEnum.MARKED_UP]
        , PublicationStatusEnum[PublicationStatusEnum.CURATING]
        , PublicationStatusEnum[PublicationStatusEnum.CURATOR_FINISHED]
        // , PublicationStatusEnum[PublicationStatusEnum.PUB_APPROVED]
        // , PublicationStatusEnum[PublicationStatusEnum.AUTHOR_APPROVED]
        // , PublicationStatusEnum[PublicationStatusEnum.CLOSED]
    ];
    isAdmin: boolean;
    // private activePub: Publication;
    activePub: Publication;


    constructor(private publicationService: PublicationService, public authenticationService: AuthenticationService, private speciesService: SpeciesService
        , private modalService: NgbModal) {
    }

    ngOnInit() {
        this.isAdmin = this.authenticationService.isAdmin();
        this.selectedStatus = localStorage.getItem("publicationSelectedStatus");
        this.searchTerm = localStorage.getItem("publicationSearchTerm");
        this.selectedSpecies = "";//localStorage.getItem("publicationSelectedSpecies");
        this.updatePubs();

        this.speciesService.getSpecies().subscribe(applicationData => {
            this.speciesList = applicationData;
        });

    }

    updatePubs() {

        this.publicationService.getPubCount(this.selectedStatus, this.searchTerm, this.selectedSpecies).subscribe(applicationData => {
            this.publicationsSize = applicationData.pubCount;
        });

        this.publicationService.getPublications((this.page - 1) * this.pageSize, this.pageSize, this.selectedStatus, this.searchTerm, this.selectedSpecies).subscribe(applicationData => {
            for (let line in applicationData) {
                console.log(applicationData[line])
            }
            this.publications = applicationData;
        });
    }

    isStatus(pub: Publication, i: number, pubs: Publication[]) {
        return (this.selectedStatus === pub.status.toString());
    }

    search(term: string) {
        setTimeout(300);
        this.searchTerm = term;
        localStorage.setItem("publicationSelectedStatus", this.selectedStatus);
        localStorage.setItem("publicationSearchTerm", this.searchTerm);
        this.updatePubs();
    }

    removeConfirm(id: any) {
        if (id) {
            this.removeId = id;
        }
        this.modalService.open(this.deletePubModal);
    }

    remove(id: any) {
        this.publicationService.deletePublications(id,this.authenticationService.getAuthenticatedUser()).subscribe(applicationData => {
            this.updatePubs();
        });
    }

    xmlFile: string;
    reader: FileReader = new FileReader();


    fileChangeEvent(event) {

        let f = event.target.files[0];

        this.reader.onload = (e) => {
            this.xmlFile = this.reader.result;
            console.log("loaded");
        };

        this.reader.readAsText(f);

        this.addPub.fileName = f.name;
    }


    pubFiles: any[];

    getFiles(event) {
        let files = [].slice.call(event.target.files);
        this.pubFiles = files;

    }

    create() {
        if (this.pubFiles) {
            for (let f of this.pubFiles) {
                let reader: FileReader = new FileReader();
                let xmlFile = "";
                let newPub = new Publication();
                newPub.fileName = f.name;
                reader.onload = (e) => {
                    xmlFile = reader.result;
                    this.publicationService.createPublications(newPub, xmlFile).subscribe(applicationData => {
                            this.updatePubs();
                        },
                        (error) => {
                            let errorString = JSON.parse(error._body).error
                            alert(errorString)
                        });
                };
                reader.readAsText(f);
            }
        } else {
            this.publicationService.createPublications(this.addPub, this.xmlFile).subscribe(applicationData => {
                    this.updatePubs();
                    // this.indexPub(this.xmlFile);
                },
                (error) => {
                    let errorString = JSON.parse(error._body).error
                    alert(errorString)
                });
        }

    }

    select(pub: Publication) {
        this.selectedPub.emit(pub);
        this.activePub = pub;
    }

    open(content, id: any) {
        this.modalService.open(content);
        this.removeId = id;
    }


    filterByStatus(status: string) {
        this.selectedStatus = status;
        localStorage.setItem("publicationSelectedStatus", this.selectedStatus);
        localStorage.setItem("publicationSearchTerm", this.searchTerm);
        this.updatePubs();
    }

    filterBySpecies(species: string) {
        this.selectedSpecies = species;
        localStorage.setItem("publicationSelectedSpecies", this.selectedSpecies);
        this.updatePubs();
    }


    /**
     * This was a cool idea, but a lot of numbers seemed to fall at the wrong range
     * @param {PublicationStatusEnum} status
     * @param {number} min
     * @param {number} max
     * @returns {number}
     */
    getNumberFromEnum(status: PublicationStatusEnum, min: number, max: number): number {
        let stringValue = status.valueOf().toString();
        let returnValue: number = 0;
        for (let index = 0; index < stringValue.length; ++index) {
            returnValue += stringValue.charCodeAt(index);
        }
        let returnReturnValue = (returnValue % (max - min)) + min;
        return returnReturnValue;

    }

    getColor(status: PublicationStatusEnum) {

        switch (status) {
            case PublicationStatusEnum.INGESTED:
                return '#2277AA';
            case PublicationStatusEnum.CURATING:
                return '#DD8833';
            case PublicationStatusEnum.CURATOR_FINISHED:
                return '#88AA44';
            case PublicationStatusEnum.MARKED_UP:
                return '#DDEE00';
            default:
                return '#FF1122';
        }
    }

    getColorByCompletion(status: PublicationStatusEnum) {


        // // TODO: get enum status
        // // let value = status as number ;
        // // let value =   status.valueOf().toString() ;
        //
        // // console.log('value: ' + status.valueOf().toString());
        // // let min = PublicationStatusEnum.getMin();
        // // let max = PublicationStatusEnum.getMax();
        let min = 10;
        let max = 60;
        let mean = Math.round((min + max) / 2.0);
        //
        // // from red (min) to green (middle) to blue (max)
        let relativeValue = ((status - min) / (max - min)) * 256;
        // console.log(status + ' -> ' + relativeValue);
        let redString = '' + Math.round(256 - relativeValue);
        let greenString = '' + Math.round(Math.pow((relativeValue - mean) / (max - min), 2) * 256);
        let blueString = '' + Math.round(relativeValue);
        let returnString = 'rgb(' + redString + ',' + greenString + ',' + blueString + ')';
        // console.log(returnString);
        return returnString;
        // return returnString;
        // // return 'rgb(12,56,92)'
    }

    isActivePub(pub: Publication) {
        if (this.activePub && this.activePub.doi == pub.doi) {
            return '#eeeeff';
        }
    }
}
