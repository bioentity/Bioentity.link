import {Component, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import {User} from "./user";
// import {UserListComponent} from "./user-list/user-list.component";
import {ActivatedRoute, Params} from "@angular/router";
import {UserService} from "./user.service";

@Component({
    selector: 'user',
    templateUrl: './user.component.html',
    styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

    selectedUser: any;
    // @ViewChild (UserListComponent) userListComponent;
    // @Output() userEmitter = new EventEmitter<User>();

    constructor(private route: ActivatedRoute, private userService: UserService) {
    }

    setUserForName(username: string){
        console.log('setting user for: '+ username)
        if (username) {
            this.userService.getUser(username).subscribe(applicationData => {
                this.selectedUser = applicationData;
            })
        }
    }

    ngOnInit() {
        this.route.params.subscribe(
            (params: Params) => {
                let username = params["username"];
                this.setUserForName(username)
            }
        );
    }

    selectUser(user: any) {
        console.log('SELECTING USER '+ user)
        this.selectedUser = user;
        // this.userEmitter.emit(user);
        //this.lexiconService.getLexicon(lexicon.id).subscribe(applicationData => {
        //});
    }
}
