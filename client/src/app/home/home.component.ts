import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../authentication/authentication.service";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

    constructor(public authenticationService: AuthenticationService) {
        this.authenticationService.handleAuthentication();
    }

    ngOnInit() {
    }

}
