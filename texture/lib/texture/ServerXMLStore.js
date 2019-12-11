import {request} from "substance";

export default class ServerXMLStore {

    constructor(data) {
        this.data = data;
    }


    readXML(documentId, cb) {
        let serverURL = getServerBaseUrl() + '/publication/findByFileName/' + documentId + '.xml';

        console.log('read server URL: ' + serverURL);
        request('GET', serverURL, null, cb)
        // cb(null, this.data[documentId])
    }

    writeXML(documentId, xml, cb) {
        let serverURL = getServerBaseUrl() +  '/publication/storeByFileName/' + documentId + '.xml';
        console.log('write server URL: ' + serverURL);

        let data = {content: xml};

        request('PUT', serverURL, data, function(err, data) {
            if (err) return cb(err);
            cb(null, data);
            //if(data) {
            //    window.location.reload();
            //}   
        });
        
    }
}

