import JATS from '../JATS'
import XMLIterator from '../../util/XMLIterator'

export default {

  type: 'list-item',
  tagName: 'list-item',

  /*
    Attributes
      disp-level Display Level of a Heading
      id Document Internal Identifier
      sec-type Type of Section
      specific-use Specific Use
      xml:base Base
      xml:lang Language

    Content
    (
      sec-meta?, label?, title?,
      ( address | alternatives | array |
        boxed-text | chem-struct-wrap | code | fig | fig-group |
        graphic | media | preformat | supplementary-material | table-wrap |
        table-wrap-group | disp-formula | disp-formula-group | def-list |
        list | tex-math | mml:math | p | related-article | related-object |
        ack | disp-quote | speech | statement | verse-group | x
      )*,
      (sec)*,
      (notes | fn-group | glossary | ref-list)*
    )
  */

  import: function(el, node, converter) {

    let children = el.getChildren()
    let iterator = new XMLIterator(children)


    iterator.manyOf(JATS.PARA_LEVEL, function(child) {
      node.nodes.push(converter.convertElement(child).id)
    })

    

    if (iterator.hasNext()) {
      throw new Error('Illegal JATS: ' + el.outerHTML)
    }
  },

  export: function(node, el, converter) {
    let $$ = converter.$$
    el.attr(node.xmlAttributes)
    node.nodes.forEach(function(nodeId) {
      el.append(converter.convertNode(nodeId))
    })

  
  }

}
