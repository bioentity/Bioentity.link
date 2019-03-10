/**
 * Created by nathandunn on 6/10/17.
 */
import {AnnotationCommand, request, SelectionState} from "substance";

class MarkupApp {


    constructor(xmlId) {
        this.words = [];
        this.xmlId = xmlId;
		
        // window.addEventListener("message", receiveMessage, false);
        //
        // function receiveMessage(event)
        // {
        //     if (event.origin !== "http://example.org:8080")
        //         return;
        //
        //     // ...
        // }
        // function receiveMessage(event)
        // {
        //     // alert(event);
        //     // Do we trust the sender of this message?  (might be
        //     // different from what we originally opened, for example).
        //     // if (event.origin !== "http://example.com")
        //     //     return;
        //
        //     // event.source is popup
        //     // event.data is "hi there yourself!  the secret response is: rheeeeet!"
        // }
        // window.addEventListener("message", receiveMessage, false);
    }


    render(data, callback) {

        // let words = ['organism','unc-5','elegans','epidermal'];
        // alert('rendering with dat: ' + JSON.stringify(data));
        // alert('rendering with words: ' + data.length);

        let cmd = new ToggleStrongCommand();
        let documentSession = window.app.state.documentSession;
        let selectionState = documentSession.getSelectionState();
        let nodes = window.doc.getNodes();


        document.getElementById('message').innerHTML = "Running markup...";
        // n.startsWith('article') // article junk
        // || n.startsWith('ext-link') // what we are tring to create
        // n.startsWith('body') // this is just the intro
        // || n.startsWith('heading') // this is the heading for each paragraph
        let numErrors = 0;
        let totalWordsFound = 0;
        let wordsFound = [];
        let totalWordHits = 0;

        for (let n in nodes) {
            // console.log(n);
            if (n.startsWith('paragraph')
            ) {
                let p = window.doc.get(n);
                // just find the first one
                for (let w in data) {
                    let wordHits = 0;
                    let word = data[w];
                    let found = 0;
                    var reWord = word.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
                    var re = new RegExp("\\W" + reWord + "\\W", "g");
                    var hits;
                    while (hits = re.exec(p.content)) {

                        let sel = documentSession.createSelection({
                            type: 'property',
                            path: [p.id, 'content'],
                            startOffset: hits.index + 1,
                            endOffset: hits.index + 1 + word.length
                        });
                        selectionState.setSelection(sel);
                        let cmdState = cmd.getCommandState({
                            selectionState: selectionState,
                            documentSession: documentSession
                        });
                        if (cmdState.mode == "create") {
                            cmd.execute({
                                commandState: {
                                    mode: 'create'
                                },
                                documentSession: documentSession,
                                selectionState: documentSession.getSelectionState()
                            });
                            console.log('Found' + word);
                        } else {
                            console.log("Annotated words: " + p.content.substring(selectionState.getAnnotationsForType(cmd.getAnnotationType())[0].startOffset, selectionState.getAnnotationsForType(cmd.getAnnotationType())[0].endOffset));
                            if (cmdState.mode == "delete") {
                                console.log('Error trying to annotate: ' + word + ' because it is already linked.');
                            } else if (cmdState.mode == "expand") {
                                console.log('Error trying to annotate: ' + word + ' because part of the word is already linked.');
                            } else if (cmdState.mode == "truncate") {
                                console.log('Error trying to annotate: ' + word + ' because it is part of the linked word.');
                            }
                            ++numErrors;
                        }

                        //found = p.content.indexOf(word,found+1);
                        ++wordHits;
                    }
                    if (wordHits > 0) {
                        ++totalWordsFound;
                        totalWordHits += wordHits;
                        wordsFound[word] = wordHits;
                        document.getElementById('message').innerHTML = "Searching: found " + totalWordsFound + " words, hits: " + totalWordHits;
                    }
                }
            }
        }
        let finalMessage = "Total words " + totalWordsFound + " Total links: " + totalWordHits;
        let detailButton = "  &nbsp;&nbsp;<button style='display: inline;' type='button' onclick='var e = document.getElementById(\"hitDetails\"); e.style.display = (e.style.display == \"block\") ? \"none\" : \"block\";'>Toggle details</button>";
        // finalMessage += "<ul>";
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


        // finalMessage += "</ul>";
        document.getElementById('message').innerHTML = finalMessage;
        console.log('total found: ' + totalWordsFound);
        console.log('total errors: ' + numErrors);

        if (callback) callback();
    }

    searchWords(termData, linkItalics, callback) {
        // alert('searching with hits : '+termData.length);
        // alert('searching with hits : '+JSON.stringify(termData));
        console.log('searching with hits: ' + termData.length);
//        console.log(termData);
        // for(t in terms){
        //     console.log(terms[t].value);
        // }
        // let words = ['organism','unc-5','elegans','epidermal'];
        // alert('rendering with dat: ' + JSON.stringify(data));
        // alert('rendering with words: ' + data.length);

       // let cmd = new ToggleStrongCommand();
        let documentSession = window.app.state.documentSession;
        let selectionState = documentSession.getSelectionState();
        let nodes = window.doc.getNodes();

		let subscriptNodes = [];
		let superscriptNodes = [];
		let italicNodes = {};
		let entityMatches = [];


		for (let node in nodes) {
			if(node.startsWith('subscript')) {
				subscriptNodes.push(nodes[node]);
			} else if (node.startsWith('superscript')) {
				superscriptNodes.push(nodes[node]);
				let paragraph = window.doc.get(nodes[node].path[0]);
				console.log("superscript 1 " + paragraph.content.substring(nodes[node].startOffset, nodes[node].endOffset))
				console.log("superscript 2 " + paragraph.content.substring(paragraph.content.substring(0, nodes[node].startOffset).lastIndexOf(' ') + 1, nodes[node].startOffset))
				for(let w in termData) {
				   // Handle the superscript	
					if(paragraph.content.substring(nodes[node].startOffset, nodes[node].endOffset) == termData[w].value) {
						entityMatches.push({path: nodes[node].path, startOffset: nodes[node].startOffset, endOffset: nodes[node].endOffset, term: termData[w], type: "superscript"});
					}
					// Handle the word preceding the superscript
					if(paragraph.content.substring(paragraph.content.substring(0, nodes[node].startOffset).lastIndexOf(' ') + 1, nodes[node].startOffset) == termData[w].value) {
						entityMatches.push({path: [paragraph.id, 'content'], startOffset: nodes[node].startOffset - termData[w].value.length, endOffset: nodes[node].startOffset, term: termData[w], type: "basesup"})
					}
				}
			} else if(node.startsWith('italic')) {
				let italic = nodes[node]
				italicNodes[italic.path[0] + ":" + italic.startOffset] = italic
				italicNodes[italic.path[0] + ":" + italic.endOffset] = italic
			}

		}

        document.getElementById('message').innerHTML = "Running markup...";
        // n.startsWith('article') // article junk
        // || n.startsWith('ext-link') // what we are tring to create
        // n.startsWith('body') // this is just the intro
        // || n.startsWith('heading') // this is the heading for each paragraph
        let numErrors = 0;
        let totalWordsFound = 0;
        let wordsFound = [];
        let totalWordHits = 0;

        window.parent.postMessage({
            action: 'clearWords',
            words: termData,
            xmlId: xmlId
        }, "*");

        for (let node in nodes) {
            if (node.startsWith('paragraph') ) {
                let paragraphNode = window.doc.get(node);
				//console.log(paragraphNode)
				let hits;
				let wordHits = 0;
				for (let w in termData) {
                    //let wordHits = 0;
                    let term = termData[w];
                    let found = 0;
                    let reWord = term.value.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
                    let re = new RegExp("^" + reWord + "[^A-Za-z0-9_;:-]|[^A-Za-z0-9_;:-]" + reWord + "[^A-Za-z0-9_;:-]", "g");
                    //let hits;
                    while (hits = re.exec(paragraphNode.content)) {

						let startOffset = hits.index + 1;
						let endOffset = hits.index + 1 + term.value.length;
						if (hits.index == 0) {
							startOffset = 0;
							endOffset = term.value.length;
						}
						
						entityMatches.push({path: [paragraphNode.id, 'content'], startOffset: startOffset, endOffset: endOffset, term: term, type: "word"})
					}
				}
				
				for(let entityMatch in entityMatches) {
						let entity = entityMatches[entityMatch]
//						if(entity.term.value == "frp1") {
						//	console.log(entity)
//						}
						if(linkItalics && !((entity.path[0] + ":" + entity.startOffset) in italicNodes) &&
							!((entity.path[0] + ":" + entity.endOffset) in italicNodes)
						) {
							console.log("Not linking " + entity.term.value + " because it isn't in italics")
							continue
						} 
						
						let term = entityMatches[entityMatch].term;
						let startOffset = entityMatches[entityMatch].startOffset
						let endOffset = entityMatches[entityMatch].endOffset

                        let sel = documentSession.createSelection({
                            type: 'property',
                            path: entityMatches[entityMatch].path,
                            startOffset: startOffset,
                            endOffset: endOffset
                        });
		                // populate the hit
                        term.selection = sel ;
					
                        selectionState.setSelection(sel);
						let cmd = new ToggleExtLinkCommand();

                        let cmdState = cmd.getCommandState({
                            selectionState: selectionState,
                            documentSession: documentSession
                        });

						for(let node in subscriptNodes) {
							if(subscriptNodes[node].path[0] == entity.path[0] && subscriptNodes[node].startOffset > startOffset && subscriptNodes[node].startOffset < endOffset) {
								//console.log(entity.path[0] + " " + startOffset + " " + endOffset + " " + subscriptNodes[node].path[0] + " " + subscriptNodes[node].startOffset + " " + subscriptNodes[node].endOffset)
								cmdState.mode = "subscript"	
							}
						}

						if(cmdState.mode == "expand") {
							
							if(entityMatches[entityMatch].type == "superscript") {
					

								startOffset++

								sel = documentSession.createSelection({
    	                        	type: 'property',
	    	                        path: entityMatches[entityMatch].path,
    	    	                    startOffset: startOffset,
        	    	                endOffset: endOffset
            	    	        });
								selectionState.setSelection(sel);

							} else if(entityMatches[entityMatch].type == "basesup") {
								endOffset--

								sel = documentSession.createSelection({
    	                        	type: 'property',
	    	                        path: entityMatches[entityMatch].path,
    	    	                    startOffset: startOffset,
        	    	                endOffset: endOffset
            	    	        });
								selectionState.setSelection(sel);

							}

							 let res = cmd.execute({
                                commandState: {
                                    mode: "create"
                                },
                                documentSession: documentSession,
                                selectionState: selectionState
                            });
							// Save ext-link ID
							term.extLinkId = res.anno.id;
                       		documentSession.transaction(function(tx, args) {
								tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
								tx.set([res.anno.id, 'entityType'], "mu")

							})

							if(entityMatches[entityMatch].type == "superscript") {
					
								documentSession.transaction(function(tx, args) {
									tx.set([res.anno.id, 'startOffset'], startOffset - 1)
									tx.set([res.anno.id, 'entityType'], "superscript")
								})

							} else if(entityMatches[entityMatch].type == "basesup") {


								documentSession.transaction(function(tx, args) {
									tx.set([res.anno.id, 'endOffset'], endOffset + 1)

								})

							}
	
					        let saveTerm = JSON.parse(JSON.stringify(term));

                            try{
                                window.parent.postMessage({
                                    action: 'saveLink'
                                    ,hit: hits
                                    ,term: saveTerm
                                    ,xmlId: xmlId
                                }, "*");
                            }
                            catch(e){
                                alert('error saving link: '+JSON.stringify(e));
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
       						documentSession.transaction(function(tx, args) {
								tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
								tx.set([res.anno.id, 'entityType'], "mu")

							})
		                   let saveTerm = JSON.parse(JSON.stringify(term));

                            try{
                                window.parent.postMessage({
                                    action: 'saveLink'
                                    ,hit: hits
                                    ,term: saveTerm
                                    ,xmlId: xmlId
                                }, "*");
                            }
                            catch(e){
                                alert('error saving link: '+JSON.stringify(e));
                            }

                        } else {
                            if (cmdState.mode == "delete") {
                                console.log('Error trying to annotate: ' + term.value + ' because it is already linked.');
                            } else if (cmdState.mode == "expand") {
                                console.log('Error trying to annotate: ' + term.value + ' because part of the word is already linked.');
                            } else if (cmdState.mode == "truncate") {
                                console.log('Error trying to annotate: ' + term.value + ' because it is part of the linked word.');
                            }
                            ++numErrors;
                        }

                        ++wordHits;
                    }
                    if (wordHits > 0) {
                        ++totalWordsFound;
                        totalWordHits += wordHits;
                        document.getElementById('message').innerHTML = "Searching: found " + totalWordsFound + " words, hits: " + totalWordHits;
                    }
                
            }
        }

		window.parent.postMessage({
            action: 'finishedLinking',
			totalHits: totalWordHits
           
        }, "*");


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


        // force save of final XML
        documentSession.save();


        // finalMessage += "</ul>";
        document.getElementById('message').innerHTML = finalMessage;
        console.log('total found: ' + totalWordsFound);
        console.log('total errors: ' + numErrors);

        if (callback) callback();
    }

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
			if(rule.regEx == "") {
				console.log("No reg exp")
				return
			}
			if(rule.italic == "true") {
				console.log("italic")
				let c_h = 0
				let re = new RegExp(rule.regEx);

				for (let italic in italicNodes) {
					if(italicNodes[italic].path[0].startsWith('paragraph')) {
		         		let paragraph = window.doc.get(italicNodes[italic].path[0])
						
						let hit = re.exec(paragraph.content.substring(italicNodes[italic].startOffset, italicNodes[italic].endOffset));
						if(hit) {
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
							documentSession.transaction(function(tx, args) {
							//	tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
								tx.set([res.anno.id, 'entityType'], "nomu")

							})

						} else if (cmdState.mode == "delete") {
							let extLinkId = selectionState.getAnnotationsForType("ext-link")[0].id
							documentSession.transaction(function(tx, args) {

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
					if(node.startsWith('paragraph')) {
						let paragraph = window.doc.get(node)
						let hit
						while(hit = re.exec(paragraph.content)) {
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
							documentSession.transaction(function(tx, args) {
							//	tx.set([res.anno.id, 'hrefLink'], term.lexica[0].link)
								tx.set([res.anno.id, 'entityType'], "nomu")

							})

						} else if (cmdState.mode == "delete") {
							let extLinkId = selectionState.getAnnotationsForType("ext-link")[0].id
							documentSession.transaction(function(tx, args) {

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
			if(rule.regEx == "") {
				console.log("No reg exp")
				continue
			}
			if(rule.italic == "true") {
				console.log("italic")
				let c_h = 0
				let re = new RegExp(rule.regEx);

				for (let italic in italicNodes) {
					if(italicNodes[italic].path[0].startsWith('paragraph')) {
		         		let paragraph = window.doc.get(italicNodes[italic].path[0])
						
						let hit = re.exec(paragraph.content.substring(italicNodes[italic].startOffset, italicNodes[italic].endOffset));
						if(hit) {
					
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
        super({name: 'markup', nodeType: 'ext-link'})
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
