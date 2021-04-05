import { TextNode } from 'substance'

class DispQuote extends TextNode {}

DispQuote.type = 'disp-quote'

DispQuote.define({
  attributes: { type: 'object', default: {} }
})

export default DispQuote
