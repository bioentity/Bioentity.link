import { DocumentNode, DefaultDOMElement as DOMElement } from 'substance'

/*
  ref

  One item in a bibliographic list.
*/
class Ref extends DocumentNode {

  /*
    Checks if ref is a plain text citation, with no formatting / tagging etc.
  */
  isPlain() {
    // TODO:
  }

  /*
    Get parsed DOM version of XML content
  */
  getDOM() {
    
    let doc = DOMElement.parseXML('<ref>' + this.xmlContent + '</ref>')
    return doc.children
  }

}

Ref.type = 'ref'

/*
  Content
  (label?, (citation-alternatives | element-citation | mixed-citation | nlm-citation | note | x)+)
*/
Ref.define({
  attributes: { type: 'object', default: {} },
  xmlContent: {type: 'string', default: ''}
})

export default Ref
