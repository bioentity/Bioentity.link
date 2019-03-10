import {Component, Input, OnInit} from '@angular/core';
import {User} from "../user";
import {ActivatedRoute, Params} from "@angular/router";
import {UserService} from "../user.service";
import {Role} from "../role";

@Component({
    selector: 'user-content',
    templateUrl: './user-content.component.html',
    styleUrls: ['./user-content.component.css']
})
export class UserContentComponent implements OnInit {

    @Input() selectedUser: User;
    private roles: any;
    showEditUser = true ;

    constructor(private route: ActivatedRoute, private userService: UserService) {
    }

    ngOnInit() {

        this.userService.getRoles().subscribe(roles => {
            this.roles = roles;
        });


        // this.route.params.subscribe(
        //     (params: Params) => {
        //         console.log(params);
        //         let username = params["username"];
        //         if (username) {
        //             this.userService.getUser(username).subscribe(applicationData => {
        //                 this.selectedUser = applicationData;
        //             })
        //         }
        //     }
        // );
    }

    findRole(id) {
        if (!this.roles) return null;
        for (let r of this.roles) {
            if (r.id == id) {
                return r;
            }
        }
        return null;
    }

    editUserEnd() {
        this.showEditUser = false;
    }

    updateUser(user:User) {
        this.userService.updateUser(user).subscribe( applicationData => {
            this.selectedUser = applicationData ;
            this.editUserEnd();
        });


    }

    editUserToggle() {
        this.showEditUser =  !this.showEditUser;
    }

    isUserRole(role: Role) {
        for(let aRole of this.roles){
            if(role.name == aRole.name){
                return true ;
            }
        }
        return false ;
    }
}
