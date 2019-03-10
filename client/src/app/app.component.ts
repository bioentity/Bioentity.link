import { Component } from '@angular/core';
import {AuthenticationService} from "./authentication/authentication.service";

@Component({
  selector: 'app',
  templateUrl: './app.component.html'
})
export class AppComponent {

    constructor(public authenticationService: AuthenticationService) {
        this.authenticationService.handleAuthentication();
    }
}
