import DispQuote from './DispQuote'
import DispQuoteConverter from './DispQuoteConverter'
import DispQuoteComponent from './DispQuoteComponent'

export default {
  name: 'disp-quote',
  configure: function(config) {
    config.addNode(DispQuote)
    config.addComponent(DispQuote.type, DispQuoteComponent)
    config.addConverter('jats', DispQuoteConverter)
  }
}
