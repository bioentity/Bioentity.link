import List from './List'
import ListConverter from './ListConverter'
import ListComponent from './ListComponent'

export default {
  name: 'list',
  configure: function(config) {
    config.addNode(List)
    config.addComponent(List.type, ListComponent)
    config.addConverter('jats', ListConverter)
  }
}
