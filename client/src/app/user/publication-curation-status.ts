import {User} from "./user";
import {Publication} from "../publication/publication";
import {CurationStatus} from "./curation-status.enum";

export class PublicationCurationStatus {

    user: User ;
    publication: Publication;
    status: CurationStatus;


}
