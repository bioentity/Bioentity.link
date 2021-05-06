import { Component, ContainerEditor } from "substance";

class DispQuoteComponent extends Component {
  render($$) {
    let node = this.props.node;
    let el = $$("div").addClass("sc-disp-quote");

    el.append(
      $$(ContainerEditor, { node: node })
        .ref("contentEditor")
        .addClass("se-content")
    );
    return el;
  }
}

export default DispQuoteComponent;
