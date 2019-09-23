import ListItemComponent from './ListItemComponent'
import ListItemConverter from './ListItemConverter'
import ListItem from './ListItem';

export default {
  name: 'list-item',
  configure: function(config) {
    config.addNode(ListItem)
    config.addComponent('list-item', ListItemComponent)
    config.addConverter('jats', ListItemConverter)
  }
}
