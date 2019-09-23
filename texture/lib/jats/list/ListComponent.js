import { Component } from 'substance'
import renderNodeComponent from '../../util/renderNodeComponent'

class ListComponent extends Component {
  didMount() {
    super.didMount()
    let node = this.props.node
    node.on('nodes:changed', this.rerender, this)
  }

  dispose() {
    super.dispose()
    let node = this.props.node
    node.off(this)
  }

  render($$) {
    let node = this.props.node
    let doc = node.getDocument()
    let el = $$('div').addClass('sc-ref-list')

    // NOTE: We don't yet expose RefList.label to the editor
    if (node.title) {
      let titleNode = doc.get(node.title)
      el.append(
        renderNodeComponent(this, $$, titleNode, {
          disabled: this.props.disabled
        })
      )
    }

    // Ref elements
    let children = node.nodes
    children.forEach(function(nodeId) {
      let childNode = doc.get(nodeId)
      if (childNode.type !== 'unsupported') {
        el.append(
          renderNodeComponent(this, $$, childNode, {
            disabled: this.props.disabled
          })
        )
      } else {
        //console.info(childNode.type+ ' inside <list> currently not supported by the editor.')
        console.info(childNode)
      }
    }.bind(this))

    return el
  }
}

// Isolated Nodes config
ListComponent.fullWidth = true
ListComponent.noStyle = true

export default ListComponent
