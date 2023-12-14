import { Annotation } from "substance";

class Roman extends Annotation {}

Roman.type = "roman";

Roman.define({
  attributes: { type: "object", default: {} },
});

export default Roman;
