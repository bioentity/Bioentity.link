import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {HashLocationStrategy, LocationStrategy} from "@angular/common";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {IndexComponent} from "./index/index.component";
import {AppComponent} from "./app.component";
import {AppRoutingModule} from "./app-routing.module";
import {AdminComponent} from "./admin/admin.component";
import {PublicationComponent} from "./publication/publication.component";
import {PublicationListComponent} from "./publication/publication-list/publication-list.component";
import {PublicationStatisticsComponent} from "./publication/statistics/statistics.component";
import {LexiconComponent} from "./lexicon/lexicon.component";
import {MarkupViewComponent} from "./markup/markup-view/markup-view.component";
import {SortOrderPipe} from "./pipes/sort-order.pipe";
import {MarkupListComponent} from "./markup/markup-list/markup-list.component";
import {MarkupDetailComponent} from "./markup/markup-detail/markup-detail.component";
import {PublicationMarkupComponent} from "./publication/publication-markup/publication-markup.component";
import {PublicationDetailComponent} from "./publication/publication-detail/publication-detail.component";
import {LexiconListComponent} from "./lexicon/lexicon-list/lexicon-list.component";
import {LexiconContentComponent} from "./lexicon/lexicon-content/lexicon-content.component";
import {PublicationContentComponent} from "./publication/publication-content/publication-content.component";
import {LexiconDetailComponent} from "./lexicon/lexicon-detail/lexicon-detail.component";
import {AngularFontAwesomeModule} from 'angular-font-awesome/angular-font-awesome';
import {MarkupModal} from './markup/markup-modal.component';
import {RuleService} from './rule/rule.service';
import {LexiconService} from './lexicon/lexicon.service';

import {
    CapitalizePipe, PipeFilterPipe, QuotePipe, ReplaceAllPipe, ReplacePipe, SubStringPipe, ToFixedPipe,
    TotalPipe
} from "./pipes/filters.pipe";
import {KeyWordComponent} from "./key-word/key-word.component";
import {KeyWordContentComponent} from "./key-word/key-word-content/key-word-content.component";
import {KeyWordListComponent} from "./key-word/key-word-list/key-word-list.component";
import {KeyWordDetailComponent} from "./key-word/key-word-detail/key-word-detail.component";
import {PublicationService} from "./publication/publication.service";
import {StatisticsService} from "./publication/statistics/statistics.service";
import {MarkupService} from "./markup/markup.service";
import {AuthenticationService} from "./authentication/authentication.service";
import {UrlHelperService} from "./url-helper.service";


import {AuthorComponent} from './author/author.component';
import {AdminListComponent} from './admin/admin-list/admin-list.component';
import {AdminContentComponent} from './admin/admin-content/admin-content.component';
import {AdminDetailComponent} from './admin/admin-detail/admin-detail.component';
import {SafePipe} from './pipes/safe.pipe';
import {IconPipe} from './pipes/icon.pipe';
import {PublicationStatusPipe} from './pipes/publication-status.pipe';
import {StatisticsSearchComponent} from './publication/statistics/statistics-search/statistics-search.component';
import {StatisticsSummaryComponent} from './publication/statistics/statistics-summary/statistics-summary.component';
import {StatisticsDetailComponent} from './publication/statistics/statistics-detail/statistics-detail.component';
import {PublicationLinksComponent} from './publication/publication-links/publication-links.component';
import {PublicationIndexComponent} from './publication/publication-index/publication-index.component';
import {SearchComponent} from './search/search.component';
import {SearchListComponent} from './search/search-list/search-list.component';
import {SearchContentComponent} from './search/search-content/search-content.component';
import {SearchDetailComponent} from './search/search-detail/search-detail.component';
import { SpeciesComponent } from './species/species.component';
import { SpeciesListComponent } from './species/species-list/species-list.component';
import { SpeciesContentComponent } from './species/species-content/species-content.component';
import { SpeciesDetailComponent } from './species/species-detail/species-detail.component';
import { CuratorComponent } from './curator/curator/curator.component';
import { HomeComponent } from './home/home.component';
import { CallbackComponent } from './callback/callback.component';
import { PublicLexiconComponent } from './public-lexicon/public-lexicon.component';
import {PublicLexiconService} from "./public-lexicon/public-lexicon.service";
import { UserComponent } from './user/user.component';
import { UserListComponent } from './user/user-list/user-list.component';
import { UserDetailComponent } from './user/user-detail/user-detail.component';
import { UserContentComponent } from './user/user-content/user-content.component';
import {UserService} from "./user/user.service";
import {GithubService} from "./publication/github.service";
import {GoogleAnalyticsService} from "./google-analytics.service";

@NgModule({
    declarations: [
        AppComponent,
        IndexComponent,
        PublicationComponent,
        PublicationListComponent,
        PublicationStatisticsComponent,
        AdminComponent,
        LexiconComponent,
        MarkupViewComponent,
        SortOrderPipe,
        MarkupListComponent,
        MarkupDetailComponent,
        PublicationMarkupComponent,
        PublicationDetailComponent,
        LexiconListComponent,
        LexiconContentComponent,
        PublicationContentComponent,
        LexiconDetailComponent,
        SubStringPipe,
        CapitalizePipe,
        TotalPipe,
        ReplacePipe,
        ToFixedPipe,
        QuotePipe,
        ReplaceAllPipe,
        PipeFilterPipe,
        KeyWordComponent,
        KeyWordContentComponent,
        KeyWordListComponent,
        KeyWordDetailComponent,
        AuthorComponent,
        AdminListComponent,
        AdminContentComponent,
        AdminDetailComponent,
        SafePipe,
        StatisticsSearchComponent,
        StatisticsSummaryComponent,
        StatisticsDetailComponent,
        IconPipe,
        PublicationStatusPipe,
        PublicationLinksComponent,
        MarkupModal,
        PublicationIndexComponent,
        SearchComponent,
        SearchListComponent,
        SearchContentComponent,
        SearchDetailComponent,
		SpeciesComponent,
        SpeciesListComponent,
        SpeciesContentComponent,
        SpeciesDetailComponent,
        CuratorComponent,
        HomeComponent,
        CallbackComponent,
        PublicLexiconComponent,
        UserComponent,
        UserListComponent,
        UserDetailComponent,
        UserContentComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        AppRoutingModule,
        AngularFontAwesomeModule,
        NgbModule.forRoot()
    ],
    providers: [{
        provide: LocationStrategy,
        useClass: HashLocationStrategy
    }, PublicationService, StatisticsService, MarkupService
        , AuthenticationService, UrlHelperService
        , RuleService
        , UserService
        , LexiconService
        , PublicLexiconService
        , GithubService
        , GoogleAnalyticsService
    ],
    bootstrap: [AppComponent],
    entryComponents: [MarkupModal]

})
export class AppModule {
}

