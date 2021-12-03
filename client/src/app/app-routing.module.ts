import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IndexComponent } from "./index/index.component";
import { HomeComponent } from "./home/home.component";
import { PublicLexiconComponent } from "./public-lexicon/public-lexicon.component";
import { UserComponent } from "./user/user.component";

const routes: Routes = [
    { path: '', component: IndexComponent }
    , { path: 'home', component: HomeComponent }
    , { path: 'publication/:doiPrefix/:doiRoot/:doiSuffix', component: IndexComponent, pathMatch: 'full' }
    , { path: 'publication/:doiPrefix/:doiSuffix', component: IndexComponent, pathMatch: 'full' }
    , { path: 'publication/:pubId', component: IndexComponent, pathMatch: 'full' }
    , { path: 'keyWord/:kwSetId', component: IndexComponent, pathMatch: 'full' }
    , { path: 'lexica/:lexicaId', component: IndexComponent, pathMatch: 'full' }
    , { path: 'lexicon/public/:lexiconId', component: PublicLexiconComponent, pathMatch: 'full' }
    , { path: 'user/:username', component: IndexComponent, pathMatch: 'full' }
    // {path: '', component: IndexComponent}
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}