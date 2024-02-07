import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.fortinge.forprompt.R
import java.util.concurrent.RecursiveTask

class PrefRepository(val context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()

    private fun String.put(key:String, long: Long) {
        editor.putLong(key, long)
        editor.commit()
    }

    private fun String.put(key:String, int: Int) {
        editor.putInt(key, int)
        editor.commit()
    }

    private fun String.put(key:String, string: String) {
        editor.putString(key, string)
        editor.commit()
    }

    private fun String.put(key:String, boolean: Boolean) {
        editor.putBoolean(key, boolean)
        editor.commit()
    }

    private fun String.put(key:String, float: Float) {
        editor.putFloat(key, float)
        editor.commit()
    }

    private fun String.put(key: String, double: Double) {
        editor.putDouble(key, double)
        editor.commit()
    }

    fun getText() = pref.getString(PREF_TEXT, context.getString(R.string.prompt_default_text) )
    fun setText(text: String){
        PREF_TEXT.put(PREF_TEXT, text)
    }

    fun getFontSize() = pref.getFloat(PREF_FONT_SIZE, 70F)
    fun setFontSize(fontSize: Float) {
        PREF_FONT_SIZE.put(PREF_FONT_SIZE, fontSize)
    }

    fun getPromptSpeed() = pref.getFloat(PREF_PROMPT_SPEED,0F)
    fun setPromptSpeed(promptspeed: Float) {
        PREF_PROMPT_SPEED.put(PREF_PROMPT_SPEED, promptspeed)
    }

    fun getBold() = pref.getBoolean(PREF_BOLD, false)
    fun setBold(isbold: Boolean){
        PREF_BOLD.put(PREF_BOLD, isbold)
    }

    fun getAlignment() = pref.getInt(PREF_ALIGNMENT, 4)
    fun setAlignment(aligment: Int) {
        PREF_ALIGNMENT.put(PREF_ALIGNMENT, aligment)
    }

    fun getSpeedRate() = pref.getDouble(PREF_SPEED_RATE, 1.0)
    fun setSpeedRate(speedRate: Double) {
        PREF_SPEED_RATE.put(PREF_SPEED_RATE, speedRate)
    }

    fun getCountdownEnable() = pref.getBoolean(PREF_COUNTDOWN_ENABLE, false)
    fun setCountdownEnable(isEnable: Boolean) {
        PREF_COUNTDOWN_ENABLE.put(PREF_COUNTDOWN_ENABLE, isEnable)
    }

    fun getCountdownValue() = pref.getInt(PREF_COUNTDOWN_VALUE, 0)
    fun setCountdownValue(countdownValue: Int){
        PREF_COUNTDOWN_VALUE.put(PREF_COUNTDOWN_VALUE, countdownValue)
    }

    fun getPromptLoop() = pref.getBoolean(PREF_PROMPT_LOOP, false)
    fun setPromptLoop(isLoop: Boolean) {
        PREF_PROMPT_LOOP.put(PREF_PROMPT_LOOP, isLoop)
    }

    fun getShowScroll() = pref.getBoolean(PREF_SHOW_SCROLL, false)
    fun setShowScroll(isShowScroll: Boolean) {
        PREF_SHOW_SCROLL.put(PREF_SHOW_SCROLL, isShowScroll)
    }

    fun getMarginsEnable() = pref.getBoolean(PREF_MARGINS_ENABLE, false)
    fun setMarginsEnable(isMarginsEnable: Boolean) {
        PREF_MARGINS_ENABLE.put(PREF_MARGINS_ENABLE, isMarginsEnable)
    }

    fun getMarginsValue() = pref.getInt(PREF_MARGINS_VALUE,0)
    fun setMarginsValue(marginsValue: Int) {
        PREF_MARGINS_VALUE.put(PREF_MARGINS_VALUE, marginsValue)
    }

    fun getRTL() = pref.getBoolean(PREF_RTL, false)
    fun setRTL(isRTL: Boolean) {
        PREF_RTL.put(PREF_RTL, isRTL)
    }

    fun getTextColor() = pref.getInt(PREF_TEXT, 16777216)
    fun setTextColor(textColor: Int) {
        PREF_TEXT_COLOR.put(PREF_TEXT_COLOR, textColor)
    }


    fun getBackgroundColor() = pref.getInt(PREF_BACKGROUND_COLOR,-16777216)
    fun setBackgroundColor(backgroundColor: Int) {
        PREF_BACKGROUND_COLOR.put(PREF_BACKGROUND_COLOR, backgroundColor)
    }

    fun clearData() {
        editor.clear()
        editor.commit()
    }

    companion object{
        const val PREFERENCE_NAME = "MY_APP_PREF"
        const val PREF_TEXT = "PREF_TEXT"
        const val PREF_FONT_SIZE = "PREF_FONT_SIZE"
        const val PREF_PROMPT_SPEED = "PREF_PROMPT_SPEED"
        const val PREF_BOLD = "PREF_BOLD"
        const val PREF_ALIGNMENT = "PREF_ALIGNMENT"
        const val PREF_SPEED_RATE = "PREF_SPEED_RATE"
        const val PREF_COUNTDOWN_ENABLE = "PREF_COUNTDOWN_ENABLE"
        const val PREF_COUNTDOWN_VALUE = "PREF_COUNTDOWN_VALUE"
        const val PREF_PROMPT_LOOP = "PREF_PROMPT_LOOP"
        const val PREF_SHOW_SCROLL = "PREF_SHOW_SCROLL"
        const val PREF_MARGINS_ENABLE = "PREF_MARGINS_ENABLE"
        const val PREF_MARGINS_VALUE = "PREF_MARGINS_VALUE"
        const val PREF_RTL = "PREF_RTL"
        const val PREF_TEXT_COLOR = "PREF_TEXT_COLOR"
        const val PREF_BACKGROUND_COLOR = "PREF_BACKGROUND_COLOR"


    }

    fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
        putLong(key, java.lang.Double.doubleToRawLongBits(double))

    fun SharedPreferences.getDouble(key: String, default: Double) =
        java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))

}