import {Component, Input, OnInit} from '@angular/core';
import {User} from "../user";
import {UserService} from "../user.service";
import {Publication} from "../../publication/publication";
import {GithubService} from "../../publication/github.service";

@Component({
    selector: 'user-detail',
    templateUrl: './user-detail.component.html',
    styleUrls: ['./user-detail.component.css']
})
export class UserDetailComponent implements OnInit {

    @Input() selectedUser: any;
    publications: any;
    activePubs: any;
    assignedPubs: any;
    finishedPubs: any;

    constructor(private userService: UserService, private githubService: GithubService) {
    }

    ngOnInit() {
        // console.log('inniting ' + this.selectedUser)
    }


    /**
     * Assign without setting the status
     */
    unassignUser(publication: Publication) {
        this.githubService.unassignUser(publication, this.selectedUser).subscribe(applicationData => {
            this.findPubchanges();
        });
    }

    findPubchanges() {
        if(this.selectedUser){
            this.userService.getActivePublications(this.selectedUser).subscribe(applicationData => {
                this.publications = applicationData;
                this.assignedPubs = this.publications.filter(p => p.curationStatus === 'ASSIGNED');
                this.activePubs = this.publications.filter(p => p.curationStatus === 'STARTED');
                this.finishedPubs = this.publications.filter(p => p.curationStatus === 'FINISHED');
            });
        }
        else{
            this.activePubs = [];
            this.assignedPubs= [];
            this.finishedPubs = [];
        }
    }

    ngOnChanges() {
        this.findPubchanges();
    }

}
