import { Injectable } from '@angular/core';
import {Route, Router,ActivatedRoute, Params} from "@angular/router";

@Injectable()
export class UrlHelperService {

    constructor(private activatedRoute: ActivatedRoute) {
        this.activatedRoute.queryParams.subscribe((params: Params) => {
            // console.log("PARAMS: "+JSON.stringify(params));
            if(params.admin && params.admin==true){
                window["admin"]=true ;
            }
            else{
                window["admin"]=false;
            }
        });
    }

    getGlobalParameter(parameterString: string): string {
        let returnParam : string = null ;
        this.activatedRoute.queryParams.subscribe((params: Params) => {
            console.log(params);
            returnParam = params[parameterString];
        });
        return returnParam ;
    }

    asBoolean(input: any) : boolean{
        if(input && (input == 'true' || input == '1' || input  == 't' || input == 1 || input == true)){
            return true ;
        }
        else{
            return false
        }
    }
}
