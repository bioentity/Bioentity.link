import {Component, OnInit, Output, EventEmitter, Input, ViewChild} from "@angular/core";

import {Publication} from "./publication";
import {UrlHelperService} from "../url-helper.service";
import {PublicationService} from "./publication.service";
import {User} from "../user/user";
import {GithubService} from "./github.service";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";

@Component({
    selector: 'publication',
    templateUrl: './publication.component.html',
    styleUrls: ['./publication.component.css']
})
export class PublicationComponent implements OnInit {

    @Input() selectedPub: Publication;
    @Input() user: User;
    isFullscreen = false;
    isExpand = false;
    showRHS = true;
    showLHS = true;
    showBigRHS = false;
    pubLockedBy = null;
    modalRef: NgbModalRef;
    @Output() pubEmitter = new EventEmitter<Publication>();
    @ViewChild('processing') processingModal;
    @ViewChild('error') errorModal;
    private publicationString: any;


    constructor(private urlHelperService: UrlHelperService
        , private publicationService: PublicationService
        , private modalService: NgbModal
        , private githubService: GithubService) {

    }

    ngOnInit() {
        let publicationString = this.urlHelperService.getGlobalParameter("pub");
        // if not found there, then maybe we can look at the index component

        if (publicationString) {
            this.publicationService.findPublication(publicationString).subscribe(applicationData => {
                if (applicationData) {
                    this.selectPub(applicationData)
                }
            });
        }
    }

    setPublicationById(publicationString: any) {
        this.publicationService.findPublication(publicationString).subscribe(applicationData => {
            if (applicationData) {
                this.selectPub(applicationData)
            }
        } , err => {
            // TODO: make this a modal
            this.publicationString  = publicationString;
            this.modalRef = this.modalService.open(this.errorModal);
        });
    }

    testLockedBy() {
        this.publicationService.getCurators(this.selectedPub).subscribe(applicationData => {
            let curators = applicationData.curators;
            this.pubLockedBy = null;
            for (let c of curators) {
                if (c.status === 'STARTED' && c.username !== this.user.username) {
                    this.pubLockedBy = c.username;
                }
            }
        });
    }

    selectPub(pub: any) {
        this.toggleFSLeft();
        this.selectedPub = pub;

        if (this.selectedPub) {
            if (this.selectedPub.githubLink) {
                this.testLockedBy()
            }
            else {
                this.modalRef = this.modalService.open(this.processingModal);
                this.githubService.getPubLink(this.selectedPub).subscribe(applicationData => {
                    this.selectedPub = applicationData;
                    this.testLockedBy();
                    this.modalRef.close();
                });
            }
        }
        this.pubEmitter.emit(pub);
    }

    toggleFSLeft() {
        this.showLHS = !this.showLHS;
        this.showBigRHS = false;
    }

    toggleFSRight() {
        this.showRHS = !this.showRHS;
        this.showBigRHS = false;
    }


    toggleExpand() {
        this.showBigRHS = !this.showBigRHS;
        this.showRHS = !this.showBigRHS;
        this.showLHS = this.showRHS;
        console.log("big rhs " + this.showBigRHS)

    }

    exportPub() {
        this.publicationService.downloadPub(this.selectedPub);
    }
}
