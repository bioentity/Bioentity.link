import Roman from "./Roman";
import RomanConverter from "./RomanConverter";
import RomanTool from "./RomanTool";
import RomanCommand from "./RomanCommand";

export default {
  name: "roman",
  configure: function (config) {
    config.addNode(Roman);
    config.addConverter("jats", RomanConverter);
    config.addCommand(Roman.type, RomanCommand, { nodeType: Roman.type });
    config.addTool(Roman.type, RomanTool, { target: "annotations" });
    config.addIcon(Roman.type, { fontawesome: "fa-font" });
    config.addLabel(Roman.type, {
      en: "Roman",
    });
  },
};
