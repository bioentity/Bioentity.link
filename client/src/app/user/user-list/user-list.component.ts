import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {UserService} from "../user.service";
import {User} from "../user";
import {ActivatedRoute, Router} from "@angular/router";
import {Role} from "../role";

@Component({
    selector: 'user-list',
    templateUrl: './user-list.component.html',
    styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {

    users: Array<User>;
    roles: Array<Role>;
    addUser = new User(null,null,null,null);
    filters = ['All','Active','Inactive'];
    userFilter = 'Active';
    @Output() selectedUser = new EventEmitter<User>();

    constructor(private userService: UserService
        , private route: ActivatedRoute
        , private router: Router
    ) {
    }

    ngOnInit() {
        // TODO: set route for tab
        // this.router.navigate()
        this.getUsers();
        this.getRoles();
    }

    addNewUser(){
        this.userService.addUser(this.addUser).subscribe( users => {
            this.users = users ;
        });
    }

    getUsersWithFilter(filter) {
        this.userFilter = filter ;
        this.userService.getUsers(this.userFilter).subscribe( users => {
           this.users = users ;
        });
    }

    getUsers() {
        this.getUsersWithFilter('Active')
    }


    getRoles() {
        this.userService.getRoles().subscribe( roles => {
            this.roles = roles ;
        });
    }

    deactivate(user,filter){
        this.userService.deactivate(user,filter).subscribe( users => {
            this.users = users ;
        });
    }

    activate(user: User, filter: string) {
        this.userService.activate(user,filter).subscribe( users => {
            this.users = users ;
        });
    }
}
