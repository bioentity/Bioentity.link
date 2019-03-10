import {Injectable} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';
import {environment} from '../environments/environment';
declare var ga: Function;

const LOG_ALL_ENVIRONMENTS = false ;

@Injectable()
export class GoogleAnalyticsService {


    constructor(public router: Router) {
        this.router.events.subscribe(event => {
            if(environment.production || LOG_ALL_ENVIRONMENTS) {
                try {
                    if (typeof ga === 'function') {
                        if (event instanceof NavigationEnd) {
                            ga('set', 'page', event.urlAfterRedirects);
                            ga('send', 'pageview');
                            console.log('%%% Google Analytics page view event %%%');
                        }
                    }
                } catch (e) {
                    console.log(e);
                }
            }
            else{
                console.log('suppressing routing event analytics tracking')
            }
        });

    }


    /**
     * Emit google analytics event
     * Fire event example:
     * this.emitEvent("testCategory", "testAction", "testLabel", 10);
     * @param {string} eventCategory
     * @param {string} eventAction
     * @param {string} eventLabel
     * @param {number} eventValue
     */
    public emitEvent(eventCategory: string,
                     eventAction: string,
                     eventLabel: string = null,
                     eventValue: number = null) {
        if(environment.production || LOG_ALL_ENVIRONMENTS) {
            if (typeof ga === 'function') {
                ga('send', 'event', {
                    eventCategory: eventCategory,
                    eventLabel: eventLabel,
                    eventAction: eventAction,
                    eventValue: eventValue
                });
            }
            else{
                console.log('suppressing analytics event tracking')
            }
        }
    }


}
