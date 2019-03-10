import { Component, ContainerEditor } from 'substance'

class BodyComponent extends Component {

  render($$) {
    let node = this.props.node
    let configurator = this.props.configurator
    let el = $$('div')
      .addClass('sc-body')
      .attr('data-id', this.props.node.id)

     let editor = $$(ContainerEditor, {
        disabled: this.props.disabled,
        node: node,
        commands: configurator.getSurfaceCommandNames(),
        textTypes: configurator.getTextTypes(),
		editing: "no"
      }).ref('body')
	editor.setAttribute('contenteditable', false)
	el.append(editor)
    return el
  }
}

export default BodyComponent
