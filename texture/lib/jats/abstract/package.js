import Abstract from './Abstract'
import AbstractComponent from './AbstractComponent'
import AbstractConverter from './AbstractConverter'

export default {
  name: 'abstract',
  configure: function(config) {
    config.addNode(Abstract)
    config.addComponent('abstract', AbstractComponent)
    config.addConverter('jats', AbstractConverter)
  }
}
