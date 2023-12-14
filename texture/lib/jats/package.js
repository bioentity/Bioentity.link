import JATSImporter from "./JATSImporter";
import JATSExporter from "./JATSExporter";

import AbstractPackage from "./abstract/package";
import AffPackage from "./aff/package";
import ArticlePackage from "./article/package";
import ArticleMetaPackage from "./article-meta/package";
import ArticleTitlePackage from "./article-title/package";
import ContribPackage from "./contrib/package";
import ContribGroupPackage from "./contrib-group/package";
import BackPackage from "./back/package";
import BodyPackage from "./body/package";
import BoldPackage from "./bold/package";
import CaptionPackage from "./caption/package";
import DispQuotePackage from "./disp-quote/package";
import ExtLinkPackage from "./ext-link/package";
import FigurePackage from "./figure/package";
import FootnotePackage from "./footnote/package";
import FrontPackage from "./front/package";
import GraphicPackage from "./graphic/package";
import ItalicPackage from "./italic/package";
import LabelPackage from "./label/package";
import ListPackage from "./list/package";
import ListItemPackage from "./list-item/package";
import MonospacePackage from "./monospace/package";
import ParagraphPackage from "./paragraph/package";
import RefPackage from "./ref/package";
import RefListPackage from "./ref-list/package";
import SectionPackage from "./section/package";
import SubscriptPackage from "./subscript/package";
import SuperscriptPackage from "./superscript/package";
import TablePackage from "./table/package";
import TitlePackage from "./title/package";
import TitleGroupPackage from "./title-group/package";
import XrefPackage from "./xref/package";
import RomanPackage from "./roman/package";

export default {
  name: "jats",
  configure: function (config) {
    config.import(AbstractPackage);
    config.import(AffPackage);
    config.import(ArticlePackage);
    config.import(ArticleMetaPackage);
    config.import(ArticleTitlePackage);
    config.import(ContribPackage);
    config.import(ContribGroupPackage);
    config.import(BackPackage);
    config.import(BodyPackage);
    config.import(BoldPackage);
    config.import(DispQuotePackage);
    config.import(ItalicPackage);
    config.import(CaptionPackage);
    config.import(ExtLinkPackage);
    config.import(FigurePackage);
    config.import(FootnotePackage);
    config.import(FrontPackage);
    config.import(GraphicPackage);
    config.import(LabelPackage);
    config.import(ListPackage);
    config.import(ListItemPackage);
    config.import(MonospacePackage);
    config.import(ParagraphPackage);
    config.import(RefPackage);
    config.import(RefListPackage);
    config.import(SectionPackage);
    config.import(SubscriptPackage);
    config.import(SuperscriptPackage);
    config.import(TablePackage);
    config.import(TitlePackage);
    config.import(TitleGroupPackage);
    config.import(XrefPackage);
    config.import(RomanPackage);

    config.addImporter("jats", JATSImporter);
    config.addExporter("jats", JATSExporter);
  },
};
