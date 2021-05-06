import { Container } from 'substance'

class DispQuote extends Container {}

DispQuote.type = 'disp-quote'

DispQuote.define({
  attributes: { type: 'object', default: {} }
})

export default DispQuote
