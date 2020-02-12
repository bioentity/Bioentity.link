/**
 * Created by nathandunn on 6/10/17.
 */
import { AnnotationCommand, request, SelectionState } from "substance";
import ItalicCommand from "../jats/italic/ItalicCommand";

class MarkupApp {


    constructor(xmlId) {
        this.words = [];
        this.xmlId = xmlId;
    }


    searchWords(termData, linkItalics, callback) {
        let documentSession = window.app.state.documentSession;
        let selectionState = documentSession.getSelectionState();
        let nodes = window.doc.getNodes();

        let superscriptNodes = [];
        let entityMatches = [];

        for (let node in nodes) {

            if (node.startsWith('superscript') || node.startsWith('subscript')) {
                superscriptNodes.push(nodes[node]);
                let paragraph = window.doc.get(nodes[node].path[0]);

                for (let w in termData) {
                    // Handle the superscript	
                    if (paragraph.content.substring(nodes[node].startOffset, nodes[node].endOffset) == termData[w].value) {
                        entityMatches.push({ path: nodes[node].path, startOffset: nodes[node].startOffset, endOffset: nodes[node].endOffset, term: termData[w], type: "superscript" });
                    }
                    // Handle the word preceding the superscript
                    if (paragraph.content.substring(paragraph.content.substring(0, nodes[node].startOffset).lastIndexOf(' ') + 1, nodes[node].startOffset) == termData[w].value) {
                        entityMatches.push({ path: [paragraph.id, 'content'], startOffset: nodes[node].startOffset - termData[w].value.length, endOffset: nodes[node].startOffset, term: termData[w], type: "basesup" })
                    }
                }
            } else if (node.startsWith('italic')) {
                let italic = nodes[node]
                if (italic.path[0].includes('paragraph')) {
                    let paragraph = window.doc.get(nodes[node].path[0]);

                    let terms = []
                    for (let w in termData) {
                        if (paragraph.content.substring(nodes[node].startOffset, nodes[node].endOffset).indexOf(termData[w].value) != -1) {
                            terms.push(termData[w])
                        }
                    }
                    entityMatches.push({ path: nodes[node].path, startOffset: nodes[node].startOffset, endOffset: nodes[node].endOffset, terms: terms, type: "italic" });
                }

            }

        }


        let totalWordHits = 0;

        window.parent.postMessage({
            action: 'clearWords',
            words: termData,
            xmlId: xmlId
        }, "*");

        for (let node in nodes) {
            if (node.startsWith('paragraph')) {
                let paragraphNode = window.doc.get(node);
                let hits;
                let wordHits = 0;
                for (let w in termData) {
                    let term = termData[w];
                    let reWord = term.value.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
                    let re = new RegExp("^" + reWord + "[^A-Za-z0-9_]|[^A-Za-df-qs-z0-9_]" + reWord + "[^A-Za-z0-9_]", "g");

                    while (hits = re.exec(paragraphNode.content)) {

                        let startOffset = hits.index + 1;
                        let endOffset = hits.index + 1 + term.value.length;
                        if (hits.index == 0) {
                            startOffset = 0;
                            endOffset = term.value.length;
                        }
                        // SGD has deltas 
                        // if(paragraphNode.content.substring(hits.index + term.value.length + 1, hits.index + term.value.length + 2) == "Δ") { 
                        // console.log("delta");
                        //  endOffset++;
                        // }	
                        entityMatches.push({ path: [paragraphNode.id, 'content'], startOffset: startOffset, endOffset: endOffset, term: term, type: "word" })
                    }
                }
            }
        }


        let italicCommand = new ItalicCommand({ name: "italic", nodeType: "italic" })

        for (let entityMatch in entityMatches) {
            let entity = entityMatches[entityMatch]
            if (linkItalics && entity.type != "italic"
            ) {
                console.log("Not linking " + entity.term.value + " because it isn't in italics")
                continue
            }

            if (entity.type == "italic") {
                /*
                let italicSelect = documentSession.createSelection({
                    type: 'property',
                    path: entity.path,
                    startOffset: entity.startOffset,
                    endOffset: entity.endOffset
                })

                selectionState.setSelection(italicSelect)

                try {

                    italicCommand.execute({
                        commandState: {
                            mode: 'delete'
                        },
                        selectionState: selectionState,
                        documentSession: documentSession
                    })
                } catch (e) {
                    console.log("Can't delete italic")
                }
                */
                for (let _term in entity.terms) {
                    let term = entity.terms[_term]
                    paragraphNode = window.doc.get(entity.path[0]);
                    let entityStart = paragraphNode.content
                        .substring(
                            entity.startOffset,
                            entity.endOffset
                        ).indexOf(term.value) + entity.startOffset
                    let entityEnd = entityStart + term.value.length


                    let sel = documentSession.createSelection({
                        type: 'property',
                        path: entity.path,
                        startOffset: entityStart,
                        endOffset: entityEnd
                    })

                    selectionState.setSelection(sel)
                    term.selection = sel;

                    let cmd = new ToggleExtLinkCommand();

                    let cmdState = cmd.getCommandState({
                        selectionState: selectionState,
                        documentSession: documentSession
                    });
                    if (cmdState.mode == "create") {
                        let res = cmd.execute({
                            commandState: {
                                mode: "create"
                            },
                            documentSession: documentSession,
                            selectionState: selectionState
                        });

                        // Save ext-link ID
                        term.extLinkId = res.anno.id;
                        documentSession.transaction(function (tx, args) {
                            tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
                            tx.set([res.anno.id, 'entityType'], "mu")
                        })
                        let saveTerm = JSON.parse(JSON.stringify(term));

                        window.parent.postMessage({
                            action: 'saveLink',
                            //  hit: hits,
                            term: saveTerm,
                            xmlId: xmlId
                        }, "*");
                    }

                }
                /*
                try {
                    italicSelect = documentSession.createSelection({
                        type: 'property',
                        path: entity.path,
                        startOffset: entity.startOffset,
                        endOffset: entity.endOffset
                    })
                    selectionState.setSelection(italicSelect)
                    res = italicCommand.execute({
                        commandState: {
                            mode: 'create'
                        },
                        selectionState: selectionState,
                        documentSession: documentSession
                    })

                } catch (e) {
                    console.log(e)
                }

                */
            } else {


                let term = entity.term;
                let startOffset = entity.startOffset
                let endOffset = entity.endOffset

                let sel = documentSession.createSelection({
                    type: 'property',
                    path: entity.path,
                    startOffset: startOffset,
                    endOffset: endOffset
                });
                // populate the hit
                term.selection = sel;

                selectionState.setSelection(sel);
                let cmd = new ToggleExtLinkCommand();

                let cmdState = cmd.getCommandState({
                    selectionState: selectionState,
                    documentSession: documentSession
                });

                //for(let node in subscriptNodes) {
                //		if(subscriptNodes[node].path[0] == entity.path[0]) { //} && subscriptNodes[node].startOffset > startOffset && subscriptNodes[node].startOffset < endOffset) {
                //	console.log(entity.path[0] + " " + startOffset + " " + endOffset + " " + subscriptNodes[node].path[0] + " " + subscriptNodes[node].startOffset + " " + subscriptNodes[node].endOffset)
                //	cmdState.mode = "subscript"	
                //}
                //	}

                if (cmdState.mode == "expand") {

                    if (entity.type == "superscript") {


                        startOffset++

                        sel = documentSession.createSelection({
                            type: 'property',
                            path: entity.path,
                            startOffset: startOffset,
                            endOffset: endOffset
                        });
                        selectionState.setSelection(sel);

                    } else if (entity.type == "basesup") {
                        endOffset--

                        sel = documentSession.createSelection({
                            type: 'property',
                            path: entity.path,
                            startOffset: startOffset,
                            endOffset: endOffset
                        });
                        selectionState.setSelection(sel);

                    }

                    try {
                        let res = cmd.execute({
                            commandState: {
                                mode: "create"
                            },
                            documentSession: documentSession,
                            selectionState: selectionState
                        });

                        // Save ext-link ID
                        term.extLinkId = res.anno.id;
                        documentSession.transaction(function (tx, args) {
                            tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
                            tx.set([res.anno.id, 'entityType'], "mu")
                        })

                        if (entityMatches[entityMatch].type == "superscript") {

                            documentSession.transaction(function (tx, args) {
                                tx.set([res.anno.id, 'startOffset'], startOffset - 1)
                                tx.set([res.anno.id, 'entityType'], "superscript")
                            })
                        } else if (entityMatches[entityMatch].type == "basesup") {
                            documentSession.transaction(function (tx, args) {
                                tx.set([res.anno.id, 'endOffset'], endOffset + 1)
                            })
                        }
                        term.selection = sel

                        let saveTerm = JSON.parse(JSON.stringify(term));

                        window.parent.postMessage({
                            action: 'saveLink',
                            hit: hits,
                            term: saveTerm,
                            xmlId: xmlId
                        }, "*");

                    } catch (error) {
                        console.log(error)
                        console.log(term)
                    }

                } else if (cmdState.mode == "create") {

                    let res = cmd.execute({
                        commandState: {
                            mode: "create"
                        },
                        documentSession: documentSession,
                        selectionState: selectionState//documentSession.getSelectionState()
                    });
                    // Save ext-link ID
                    term.extLinkId = res.anno.id;
                    documentSession.transaction(function (tx, args) {
                        tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
                        tx.set([res.anno.id, 'entityType'], "mu")

                    })
                    let saveTerm = JSON.parse(JSON.stringify(term));

                    try {
                        window.parent.postMessage({
                            action: 'saveLink',
                            //   hit: hits,
                            term: saveTerm,
                            xmlId: xmlId
                        }, "*");
                    }
                    catch (e) {
                        //alert('error saving link: ' + JSON.stringify(e));
                        console.log(e)
                    }

                }

                /*else {
                    
                    if (cmdState.mode == "delete") {
                        console.log('Error trying to annotate: ' + term.value + ' because it is already linked.');
                    } else if (cmdState.mode == "expand") {
                        console.log('Error trying to annotate: ' + term.value + ' because part of the word is already linked.');
                    } else if (cmdState.mode == "truncate") {
                        console.log('Error trying to annotate: ' + term.value + ' because it is part of the linked word.');
                    }
                    
                    //  ++numErrors;
                }*/
            }

        }


        //  ++wordHits;


        //if(wordHits > 0) {
        //    ++totalWordsFound;
        //totalWordHits += wordHits;
        //document.getElementById('message').innerHTML = "Searching: found " + totalWordsFound + " words, hits: " + totalWordHits;
        //}




        window.parent.postMessage({
            action: 'finishedLinking',
            totalHits: totalWordHits

        }, "*");

        /*
        let finalMessage = "Total words " + totalWordsFound + " Total links: " + totalWordHits;
        let detailButton = "  &nbsp;&nbsp;<button style='display: inline;' type='button' onclick='var e = document.getElementById(\"hitDetails\"); e.style.display = (e.style.display == \"block\") ? \"none\" : \"block\";'>Toggle details</button>";
        finalMessage += detailButton;
        finalMessage += "<hr/>";
        finalMessage += "<div id='hitDetails' style='display: block;'>";
        for (let word in wordsFound) {
            finalMessage += "<div style='font-size: smaller;display: inline;font-family: Courier;'>" + word;
            if (wordsFound[word] > 1) {
                finalMessage += " (" + wordsFound[word] + ")";
            }
            finalMessage += "; </div> ";
        }
        finalMessage += "</div>";
        */

        // force save of final XML
        documentSession.save();
    }

    // finalMessage += "</ul>";
    //document.getElementById('message').innerHTML = finalMessage;
    //console.log('total found: ' + totalWordsFound);
    //console.log('total errors: ' + numErrors);

    //if (callback) callback();

    //}

    getWords(callback) {
        let serverURL = window.location.protocol + '//' + window.location.hostname + ':8080/keyWordSet/sampleKeyWordSet';
        console.log('server URL: ' + serverURL);
        request('GET', serverURL, null, function (err, data) {
            if (err) {
                console.error(err);
                this.setState({
                    error: new Error('Loading failed')
                });
                return
            }
            if (data) {
                this.words = [];
                for (let d in data) {
                    this.words.push(data[d].value);
                }
                console.log('finished processing with words: ' + this.words.length);
                // this.render(this.words, cb);
                if (callback) callback(this.words);

                return this.words;
            }
        });
    }

    applyRule(rule, callback) {
        let documentSession = window.app.state.documentSession;
        let selectionState = documentSession.getSelectionState();
        let nodes = window.doc.getNodes();

        let italicNodes = []
        for (let node in nodes) {
            if (node.startsWith('italic')) {
                italicNodes.push(window.doc.get(node))
            }
        }

        //for (let r in ruleSet.rules) {
        //	let rule = ruleSet.rules[r];
        console.log("Trying " + " " + rule.name)
        if (rule.regEx == "") {
            console.log("No reg exp")
            return
        }
        if (rule.italic == "true") {
            console.log("italic")
            let c_h = 0
            let re = new RegExp(rule.regEx);

            for (let italic in italicNodes) {
                if (italicNodes[italic].path[0].startsWith('paragraph')) {
                    let paragraph = window.doc.get(italicNodes[italic].path[0])

                    let hit = re.exec(paragraph.content.substring(italicNodes[italic].startOffset, italicNodes[italic].endOffset));
                    if (hit) {
                        let sel = documentSession.createSelection({
                            type: 'property',
                            path: italicNodes[italic].path,
                            startOffset: italicNodes[italic].startOffset,
                            endOffset: italicNodes[italic].endOffset
                        });

                        selectionState.setSelection(sel);
                        let cmd = new ToggleExtLinkCommand();

                        let cmdState = cmd.getCommandState({
                            selectionState: selectionState,
                            documentSession: documentSession
                        });
                        if (cmdState.mode == "create") {
                            let res = cmd.execute({
                                commandState: {
                                    mode: 'create'
                                },
                                documentSession: documentSession,
                                selectionState: selectionState
                            });
                            documentSession.transaction(function (tx, args) {
                                //	tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
                                tx.set([res.anno.id, 'entityType'], "nomu")

                            })

                        } else if (cmdState.mode == "delete") {
                            let extLinkId = selectionState.getAnnotationsForType("ext-link")[0].id
                            documentSession.transaction(function (tx, args) {

                                //	tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
                                tx.set([extLinkId, 'entityType'], "rule")

                            })

                        } else {
                            console.log(cmdState.mode)
                        }
                        /*
                                                 let anno = documentSession.transaction(function(tx) {
                                                        tx.create({
                                                            type: 'ext-link',
                                                                path: italicNodes[italic].path,
                                                                startOffset: italicNodes[italic].startOffset,
                                                                endOffset: italicNodes[italic].endOffset,
                                                        })
                                                  })
                                            //	console.log(anno)
                                                let extLinkId = Object.getOwnPropertyNames(anno.created)[0]
                                                    documentSession.transaction(function(tx, args) {
                                                        tx.set([extLinkId, 'entityType'], "rule")
                                                    })*/

                        c_h++;
                        //	console.log("Italic Hit " + rule.name + " " + hit)
                    }
                }
            }
            console.log(c_h + " hits")
        } else {
            console.log("Not italic")
            let c_h = 0
            let re = new RegExp("\\b" + rule.regEx + "\\b", "g")
            for (let node in nodes) {
                if (node.startsWith('paragraph')) {
                    let paragraph = window.doc.get(node)
                    let hit
                    while (hit = re.exec(paragraph.content)) {
                        c_h++;
                        let sel = documentSession.createSelection({
                            type: 'property',
                            path: [paragraph.id, "content"],
                            startOffset: hit.index,
                            endOffset: hit.index + hit[0].length
                        });

                        selectionState.setSelection(sel);
                        let cmd = new ToggleExtLinkCommand();

                        let cmdState = cmd.getCommandState({
                            selectionState: selectionState,
                            documentSession: documentSession
                        });
                        if (cmdState.mode == "create") {
                            let res = cmd.execute({
                                commandState: {
                                    mode: 'create'
                                },
                                documentSession: documentSession,
                                selectionState: selectionState
                            });
                            documentSession.transaction(function (tx, args) {
                                //	tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
                                tx.set([res.anno.id, 'entityType'], "nomu")

                            })

                        } else if (cmdState.mode == "delete") {
                            let extLinkId = selectionState.getAnnotationsForType("ext-link")[0].id
                            documentSession.transaction(function (tx, args) {

                                //	tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
                                tx.set([extLinkId, 'entityType'], "rule")

                            })

                        }
                        /*			let anno = documentSession.transaction(function(tx) {
                                                        tx.create({
                                                            type: 'ext-link',
                                                                path: [paragraph.id, "content"],
                                                                startOffset: hit.index,
                                                                endOffset: hit.index + hit[0].length,
                                                        })
                                                        })
                                                    let extLinkId = Object.getOwnPropertyNames(anno.created)[0]
                                                    documentSession.transaction(function(tx, args) {
                                                        tx.set([extLinkId, 'entityType'], "nomu")
                                                    })*/


                    }
                }
            }
            console.log(c_h + " hits")
        }
        //}

    }

    applyRules2(ruleSet, callback) {
        let documentSession = window.app.state.documentSession;
        let selectionState = documentSession.getSelectionState();
        let nodes = window.doc.getNodes();

        let italicNodes = []
        for (let node in nodes) {
            if (node.startsWith('italic')) {
                italicNodes.push(window.doc.get(node))
            }
        }

        for (let r in ruleSet.rules) {
            let rule = ruleSet.rules[r];
            console.log("Trying " + " " + rule.name)
            if (rule.regEx == "") {
                console.log("No reg exp")
                continue
            }
            if (rule.italic == "true") {
                console.log("italic")
                let c_h = 0
                let re = new RegExp(rule.regEx);

                for (let italic in italicNodes) {
                    if (italicNodes[italic].path[0].startsWith('paragraph')) {
                        let paragraph = window.doc.get(italicNodes[italic].path[0])

                        let hit = re.exec(paragraph.content.substring(italicNodes[italic].startOffset, italicNodes[italic].endOffset));
                        if (hit) {

                            let sel = documentSession.createSelection({
                                type: 'property',
                                path: italicNodes[italic].path,
                                startOffset: italicNodes[italic].startOffset,
                                endOffset: italicNodes[italic].endOffset
                            });

                            selectionState.setSelection(sel);
                            let cmd = new ToggleExtLinkCommand();

                            let cmdState = cmd.getCommandState({
                                selectionState: selectionState,
                                documentSession: documentSession
                            });
                            if (cmdState.mode == "create") {
                                let res = cmd.execute({
                                    commandState: {
                                        mode: 'create'
                                    },
                                    documentSession: documentSession,
                                    selectionState: selectionState
                                });
                            } else {
                                console.log(hit)
                            }
                        }
                    }
                }
            }
        }
    }
}

class ToggleExtLinkCommand extends AnnotationCommand {

    // type = 'bold';
    // tagName = 'bold';

    constructor() {
        // super({name: 'bold', nodeType: 'bold'})
        super({ name: 'markup', nodeType: 'ext-link' })
        this.annotationData = {}
    }

    setHref(url) {
        this.annotationData.hrefLink = url
    }

    getAnnotationData() {
        return this.annotationData
    }

}

// ToggleStrongCommand.define({
//     attributes: { type: 'object', default: {} }
// });

// export default ToggleStrongCommand

export default MarkupApp
