import {displayFace, displayPassage, FPassage, TPassage, TPassageAction} from "./game";
import * as _face from "./face";
import {Appearance, Colors} from "./face";
import {tinycolor_ex as TCX} from "./tinycolor-ex";
require("underscore");
export const face = _face;
const curry = _.partial;

type TGender = "m"|"f";

interface CreatureData {
	gender: TGender;
	appearance: Appearance;
}

export let player: CreatureData = defaultPlayer();
function defaultPlayer(): CreatureData {
	let a = face.getDefaultAppearance();
	a.cheekSz = _.random(10) / 10;
	a.noseSz = _.random(10) / 10;
	return {
		gender: "m",
		appearance: a
	};
}
export function showPlayer() {
	displayFace(player.appearance);
}
export function playerMF<T>(m: T, f: T): T {
	return player.gender == "m" ? m : f;
}
export namespace storyLib {
	export const storyStart: FPassage = () => Chargen.chargenStart();
	namespace Chargen {
		export const chargenStart: FPassage = () => {
			player = defaultPlayer();
			return chargenMenu();
		};
		const chargenMenu: FPassage = () => {
			showPlayer();
			return [
				JSON.stringify(player.appearance),
				[
					['Restart', chargenStart] as TPassageAction,
					['Male', curry(gender, "m")] as TPassageAction,
					['Female', curry(gender, "f")] as TPassageAction
				].concat(
					Object.keys(Colors.Skin).map(c =>
						[c + ' skin', () => {
							player.appearance.skinColor = TCX(Colors.Skin[c]).toString();
							return chargenMenu();
						}] as TPassageAction)
				).concat(
					Object.keys(Colors.Eye).map(c =>
						[c + ' eyes', () => {
							player.appearance.eyeColor = TCX(Colors.Eye[c]).toString();
							return chargenMenu();
						}] as TPassageAction)
				)
			];
		};

		function gender(g?: TGender): TPassage {
			if (g) player.gender = g;
			return chargenMenu();
		}
	}
	const tutorial = () => [
		'You read the tutorial',
		[]
	] as TPassage;
}

$(() => {
	displayPassage(storyLib.storyStart());
});