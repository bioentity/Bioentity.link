import { Component, ContainerEditor, TextPropertyEditor } from 'substance'

class AbstractComponent extends Component {

  render($$) {
    let node = this.props.node
    let doc = node.getDocument()
    let el = $$('div').addClass('sc-section')

    if (node.title) {
      let title = doc.get(node.title)
      el.append(
        $$(TextPropertyEditor, { path: title.getTextPath() }).addClass('se-title').ref('titleEditor')
      )
    }
    el.append(
      $$(ContainerEditor, { node: node })//.ref('contentEditor')
        .addClass('se-content')

    )
    return el
  }
}

AbstractComponent.fullWidth = true
AbstractComponent.noStyle = true

export default AbstractComponent
