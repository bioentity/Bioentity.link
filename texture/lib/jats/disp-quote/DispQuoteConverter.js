import JATS from '../JATS'
import XMLIterator from '../../util/XMLIterator'

export default {

  type: 'disp-quote',
  tagName: 'disp-quote',

  import: function(el, node, converter) {

    let children = el.getChildren()
    let iterator = new XMLIterator(children)

    iterator.optional('label', function(child) {
      node.label = converter.convertElement(child).id
    })
    iterator.optional('title', function(child) {
      node.title = converter.convertElement(child).id
    })

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
   
    if(node.label) {
      el.append(converter.convertNode(node.label))
    }
    if(node.title) {
      el.append(converter.convertNode(node.title))
    }
    node.nodes.forEach(function(nodeId) {
      el.append(converter.convertNode(nodeId))
    })
  
  }

}
