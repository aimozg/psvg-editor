@file:Suppress("unused")

package tinycolor

/**
 * Create a tinycolor instance of the color named.
 *
 * @param color - the color as a String to create an instance for.
 */
@JsModule("tinycolor2")
external fun tinycolor2(color: String): TinyColor

/**
 * Create a tinycolor instance with the given RGB values.
 *
 * @param color - the rgb values to apply to the new instance.
 */
@JsModule("tinycolor2")
external fun tinycolor2(color: ColorFormats.ColorFormat): TinyColor

/**
 * Create a tinycolor instance with the given RGB values.
 *
 * @param color - the rgb values to apply to the new instance.
 */
@JsModule("tinycolor2")
external fun tinycolor2(color: ColorFormats.RGB): TinyColor

/**
 * Create a tinycolor instance with the given RGBA values.
 *
 * @param color - the rgba values to apply to the new instance.
 */
@JsModule("tinycolor2")
external fun tinycolor2(color: ColorFormats.RGBA): TinyColor

/**
 * Create a tinycolor instance with the given HSL values.
 *
 * @param color - the hsl values to apply to the new instance.
 */
@JsModule("tinycolor2")
external fun tinycolor2(color: ColorFormats.HSL): TinyColor

/**
 * Create a tinycolor instance with the given HSLA values.
 *
 * @param color - the hsla values to apply to the new instance.
 */
@JsModule("tinycolor2")
external fun tinycolor2(color: ColorFormats.HSLA): TinyColor

/**
 * Create a tinycolor instance with the given HSV values.
 *
 * @param color - the hsv values to apply to the new instance.
 */
@JsModule("tinycolor2")
external fun tinycolor2(color: ColorFormats.HSV): TinyColor

/**
 * Create a tinycolor instance with the given HSVA values.
 *
 * @param color - the hsva values to apply to the new instance.
 */
@JsModule("tinycolor2")
external fun tinycolor2(color: ColorFormats.HSVA): TinyColor

@JsModule("tinycolor2")
external object TinyColor2 {
	/**
	 * Create a tinycolor instance based off the relative values.
	 * Works with any color formats
	 *
	 * @param ratio - the relative color/hue values to apply to the new instance.
	 */
	fun fromRatio(ratio: dynamic): TinyColor // any of the interfaces defined in the ColorFormats module.

	/**
	 * Compares the two colors and returns the difference between their brightness and color/hue
	 *
	 * @param firstColor - the first color to be used in the comparison.
	 * @param secondColor - the second color to be used in the comparison.
	 */
	fun readability(firstColor: TinyColor, secondColor: TinyColor): Readable.Readable

	/**
	 * Ensure that foreground and background color combinations provide sufficient contrast.
	 *
	 * @param foreColor - the fore color wanted.
	 * @param backColor - the back color wanted.
	 */
	fun isReadable(foreColor: TinyColor, backColor: TinyColor): Boolean

	/**
	 * Given a base color and a list of possible foreground or background colors for that base,
	 *  returns the most readable color.
	 *
	 * @param color - the base color.
	 * @param colorsToCompare - array of colors to pick the most readable one from.
	 */
	fun mostReadable(color: TinyColor, colorsToCompare: Array<TinyColor>): TinyColor

	/**/
	fun mix(color1: TinyColor, color2: TinyColor, amount: Double = noImpl): TinyColor

	/**
	 * Returns a random color
	 */
	fun random(): TinyColor

	/**
	 * key: hex value
	 * value: String name ex. hexnames["f00"] --> "red"
	 */
	object hexNames { 
		operator fun get(key: String): String 
	}

	/**
	 * key: 'real' color name
	 * value: hex value ex. names["red"] --> "f00"
	 */
	object names {
		operator fun get(key: String): String 
	}
}

external interface TinyColor {
	/**
	 * Return an indication whether the color was successfully parsed.
	 */
	fun isValid(): Boolean

	/**
	 * Return an indication whether the color's perceived brightness is light.
	 */
	fun isLight(): Boolean

	/**
	 * Return an indication whether the color's perceived brightness is dark.
	 */
	fun isDark(): Boolean

	/**
	 * Returns the format used to create the tinycolor instance.
	 */
	fun getFormat(): String

	/**
	 * Returns the input passed into the constructer used to create the tinycolor instance.
	 */
	fun getOriginalInput(): dynamic // any of the interfaces in ColorFormats or a String

	/**
	 * Returns the alpha value of the color
	 */
	fun getAlpha(): Number

	/**
	 * Returns the perceived brightness of the color, from 0-255.
	 */
	fun getBrightness(): Number

	/**
	 * Sets the alpha value on the current color.
	 *
	 * @param alpha - The new alpha value. The accepted range is 0-1.
	 */
	fun setAlpha(alpha: Number): TinyColor

	/**
	 * Returns the object as a HSVA object.
	 */
	fun toHsv(): ColorFormats.HSVA

	/**
	 * Returns the hsva values interpolated into a String with the following format:
	 * "hsva(xxx, xxx, xxx, xx)".
	 */
	fun toHsvString(): String

	/**
	 * Returns the object as a HSLA object.
	 */
	fun toHsl(): ColorFormats.HSLA

	/**
	 * Returns the hsla values interpolated into a String with the following format:
	 * "hsla(xxx, xxx, xxx, xx)".
	 */
	fun toHslString(): String

	/**
	 * Returns the hex value of the color.
	 */
	fun toHex(): String

	/**
	 * Returns the hex value of the color -with a # appened.
	 */
	fun toHexString(): String

	/**
	 * Returns the hex 8 value of the color.
	 */
	fun toHex8(): String

	/**
	 * Returns the hex 8  value of the color -with a # appened.
	 */
	fun toHex8String(): String

	/**
	 * Returns the object as a RGBA object.
	 */
	fun toRgb(): ColorFormats.RGBA

	/**
	 * Returns the RGBA values interpolated into a String with the following format:
	 * "RGBA(xxx, xxx, xxx, xx)".
	 */
	fun toRgbString(): String

	/**
	 * Returns the object as a RGBA object.
	 */
	fun toPercentageRgb(): ColorFormats.RGBA

	/**
	 * Returns the RGBA relative values interpolated into a String with the following format:
	 * "RGBA(xxx, xxx, xxx, xx)".
	 */
	fun toPercentageRgbString(): String

	/**
	 * The 'real' name of the color -if there is one.
	 */
	fun toName(): String

	/**
	 * Returns the color represented as a Microsoft filter for use in old versions of IE.
	 */
	fun toFilter(): String

	/**
	 * String representation of the color.
	 *
	 * @param format - The format to be used when displaying the String representation.
	 *  The accepted values are: "rgb", "prgb", "hex6", "hex3", "hex8", "name", "hsl", "hsv".
	 */
	fun toString(format: String = noImpl): String

	/**
	 * Lighten the color a given amount. Providing 100 will always return white.
	 *
	 * @param amount - The amount to lighten by. The valid range is 0 to 100.
	 *  Default value: 10.
	 */
	fun lighten(amount: Number = noImpl): TinyColor

	/**
	 * Brighten the color a given amount.
	 *
	 * @param amount - The amount to brighten by. The valid range is 0 to 100.
	 *  Default value: 10.
	 */
	fun brighten(amount: Number = noImpl): TinyColor

	/**
	 * Darken the color a given amount.
	 *  Providing 100 will always return black.
	 *
	 * @param amount - The amount to darken by. The valid range is 0 to 100.
	 *  Default value: 10.
	 */
	fun darken(amount: Number = noImpl): TinyColor

	/**
	 * Desaturate the color a given amount.
	 *  Providing 100 will is the same as calling greyscale.
	 *
	 * @param amount - The amount to desaturate by. The valid range is 0 to 100.
	 *  Default value: 10.
	 */
	fun desaturate(amount: Number = noImpl): TinyColor

	/**
	 * Saturate the color a given amount.
	 *
	 * @param amount - The amount to saturate by. The valid range is 0  to 100.
	 *  Default value: 10.
	 */
	fun saturate(amount: Number = noImpl): TinyColor

	/**
	 * Completely desaturates a color into greyscale.
	 * Same as calling desaturate(100).
	 */
	fun greyscale(): TinyColor

	/**
	 * Spin the hue a given amount. Calling with 0, 360, or -360 will do nothing.
	 *
	 * @param amount - The amount to spin by. The valid range is -360 to 360.
	 *  Default value: 0.
	 */
	fun spin(amount: Number = noImpl): TinyColor

	/**
	 * Gets an analogous color scheme based off of the current color.
	 *
	 * @param results - The amount of results to return.
	 *  Default value: 6.
	 * @param slices - The amount to slice the input color by.
	 *  Default value: 30.
	 */
	fun analogous(results: Number = noImpl, slices: Number = noImpl): Array<TinyColor>

	/**
	 * Gets a monochromatic color scheme based off of the current color.
	 *
	 * @param results - The amount of results to return.
	 *  Default value: 6.
	 */
	fun monochromatic(results: Number = noImpl): Array<TinyColor>

	/**
	 * Gets a split complement color scheme based off of the current color.
	 */
	fun splitcomplement(): Array<TinyColor>

	/**
	 * Gets a triad based off of the current color.
	 */
	fun triad(): Array<TinyColor>

	/**
	 * Gets a tetrad based off of the current color.
	 */
	fun tetrad(): Array<TinyColor>

	/**
	 * Gets the complement of the current color
	 */
	fun complement(): TinyColor

	/**
	 * Gets a new instance with the current color
	 */
	fun clone(): TinyColor
}

external sealed class ColorFormats {
	interface ColorFormat {}
	interface RGB : ColorFormat{
		val r: Double
		val g: Double
		val b: Double
	}
	interface RGBA : RGB {
		val a: Double
	}
	interface HSL : ColorFormat{
		val h: Double
		val s: Double
		val l: Double
	}
	interface HSLA : HSL {
		val a: Double
	}
	interface HSV : ColorFormat{
		val h: Double
		val s: Double
		val v: Double
	}
	interface HSVA : HSV {
		val a: Double
	}
}
external sealed class Readable {
	interface Readable {
		val brightness: Double
		val color: Double
	}
}