import {LexiconSource} from '../lexicon/lexicon-source';
import {Rule} from './rule';

export class RuleSet {
	id: number;
	name: String;
	rules: Rule[];
	lexiconSource: LexiconSource
}
