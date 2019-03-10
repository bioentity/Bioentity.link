import { AnnotationTool } from 'substance'
import extend from 'lodash/extend'

class ExtLinkTool extends AnnotationTool {

	executeCommand(props) {
		console.log(this.context)
		let selectionState = this.context.documentSession.selectionState
		let paragraph = this.context.doc.get(selectionState.selection.path[0]).content

		let type = "normal"
	    if(this.props.mode == "expand" ) {

			let startOffset = selectionState.selection.startOffset
			let endOffset = selectionState.selection.endOffset
			if(paragraph.substring(selectionState.selection.startOffset - 1, selectionState.selection.startOffset) == " ") {
				type = "basesup"
				endOffset--
			} else if(paragraph.substring(selectionState.selection.endOffset + 1, selectionState.selection.endOffset) == " ") {
 				type = "superscript"
				startOffset++
			}
	
			let sel = this.context.documentSession.createSelection({
				type: 'property',
				path: selectionState.selection.path,
				startOffset: startOffset,
				endOffset: endOffset
			})
			selectionState.setSelection(sel)
			this.context.commandManager.commandStates["ext-link"].mode = "create"
		}		
		let info = this.context.commandManager.executeCommand(this.getCommandName(), extend({
		    mode: "create",
			selectionState: selectionState
	    }, props))
		if(this.props.mode == "expand") {
			if(type == "basesup") {
				this.context.documentSession.transaction(function(tx, args) {
					tx.set([info.anno.id, 'endOffset'], info.anno.endOffset + 1)
				})
			} else if(type == "superscript") {
				this.context.documentSession.transaction(function(tx, args) {
					tx.set([info.anno.id, 'startOffset'], info.anno.startOffset - 1)
					tx.set([info.anno.id, 'entitytype'], "superscript")
				})

			}
		}	

		let keyWord = {}
		keyWord.value = paragraph.substring(info.anno.startOffset, info.anno.endOffset)
		let link = {start: info.anno.startOffset, end: info.anno.endOffset, path: info.anno.path[0], extLinkId: info.anno.id, keyWord: keyWord}
		window.parent.postMessage({action: "createMarkup", term: link}, "*")

	}
}

export default ExtLinkTool
