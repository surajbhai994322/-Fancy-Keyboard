package com.bw.fancykeyboard

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class BWFancyInputMethodService : InputMethodService() {
    private lateinit var root: LinearLayout
    private lateinit var keysPanel: LinearLayout
    private lateinit var stylesPanel: LinearLayout
    private var stylesOpen = false
    private var caps = true

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(5), dp(5), dp(8))
            setBackgroundColor(Color.rgb(24, 24, 29))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = TextView(this).apply {
            text = "BW FANCY KEYBOARD"
            setTextColor(Color.rgb(255, 190, 65)); textSize = 13f
            typeface = Typeface.DEFAULT_BOLD; setPadding(dp(7), dp(5), dp(7), dp(5))
        }
        header.addView(brand, LinearLayout.LayoutParams(0, dp(34), 1f))
        header.addView(makeButton("Fancy Styles", 12f) { toggleStyles() }, LinearLayout.LayoutParams(0, dp(38), 1.3f))
        root.addView(header)

        stylesPanel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE }
        val styleNames = listOf("Monospace", "Banana", "Table Chair", "Without Vowels", "Double Letter", "Alternative", "Reverse Alternative", "Reverse Text", "Dot Style")
        styleNames.chunked(3).forEach { group ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            group.forEach { name -> row.addView(makeButton(name, 11f) { applyStyle(name) }, LinearLayout.LayoutParams(0, dp(42), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }) }
            repeat(3 - group.size) { row.addView(View(this), LinearLayout.LayoutParams(0, dp(42), 1f)) }
            stylesPanel.addView(row)
        }
        root.addView(stylesPanel)

        keysPanel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        addKeyRow("qwertyuiop")
        addKeyRow("asdfghjkl")
        val row3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row3.addView(makeButton("⇧", 18f) { caps = !caps; refreshLetters() }, LinearLayout.LayoutParams(0, dp(46), 1.2f))
        "zxcvbnm".forEach { ch -> row3.addView(letterButton(ch), LinearLayout.LayoutParams(0, dp(46), 1f).apply { setMargins(dp(2), 0, dp(2), 0) }) }
        row3.addView(makeButton("⌫", 18f) { currentInputConnection?.deleteSurroundingText(1, 0) }, LinearLayout.LayoutParams(0, dp(46), 1.2f))
        keysPanel.addView(row3)
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        bottom.addView(makeButton("?123", 13f) { commit("123") }, LinearLayout.LayoutParams(0, dp(46), 1.2f))
        bottom.addView(makeButton(",", 18f) { commit(",") }, LinearLayout.LayoutParams(0, dp(46), .7f))
        bottom.addView(makeButton("English (US)", 12f) { commit(" ") }, LinearLayout.LayoutParams(0, dp(46), 4f))
        bottom.addView(makeButton(".", 18f) { commit(".") }, LinearLayout.LayoutParams(0, dp(46), .7f))
        bottom.addView(makeButton("↵", 18f) { currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER)); currentInputConnection?.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER)) }, LinearLayout.LayoutParams(0, dp(46), 1.2f))
        keysPanel.addView(bottom)
        root.addView(keysPanel)
        return root
    }

    private fun addKeyRow(chars: String) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        chars.forEach { ch -> row.addView(letterButton(ch), LinearLayout.LayoutParams(0, dp(46), 1f).apply { setMargins(dp(2), dp(3), dp(2), dp(3)) }) }
        keysPanel.addView(row)
    }

    private fun letterButton(ch: Char): Button = makeButton(if (caps) ch.uppercaseChar().toString() else ch.toString(), 18f) { commit(if (caps) ch.uppercaseChar().toString() else ch.toString()) }

    private fun refreshLetters() {
        // Rebuild keyboard to reflect caps state.
        val newView = onCreateInputView()
        setInputView(newView)
    }

    private fun toggleStyles() {
        stylesOpen = !stylesOpen
        stylesPanel.visibility = if (stylesOpen) View.VISIBLE else View.GONE
    }

    private fun applyStyle(style: String) {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)?.toString()
        if (!selected.isNullOrEmpty()) {
            ic.commitText(convert(selected, style), 1)
        } else {
            val before = ic.getTextBeforeCursor(500, 0)?.toString().orEmpty()
            val match = Regex("[^\\s]+$").find(before)
            if (match == null) return
            val word = match.value
            ic.deleteSurroundingText(word.length, 0)
            ic.commitText(convert(word, style), 1)
        }
        stylesOpen = false
        stylesPanel.visibility = View.GONE
    }

    private fun convert(input: String, style: String): String = when (style) {
        "Monospace" -> input.toList().joinToString(" ")
        "Banana" -> input.map { if (it.isWhitespace()) it.toString() else "$it$it" }.joinToString("").replace(Regex("(?<=\\S)(?=[^\\s])"), " ").replace(Regex(" +"), " ")
        "Table Chair" -> input.replace(Regex("\\b([\\p{L}\\p{N}]+)") ) { m -> m.value + m.value.last() }
        "Without Vowels" -> input.filterNot { it.lowercaseChar() in "aeiou" }
        "Double Letter" -> input.flatMap { ch -> if (ch.isWhitespace()) listOf(ch) else listOf(ch, ch) }.joinToString("")
        "Alternative" -> input.mapIndexed { i, c -> if (i % 2 == 0) c.lowercaseChar() else c.uppercaseChar() }.joinToString("")
        "Reverse Alternative" -> input.mapIndexed { i, c -> if (i % 2 == 0) c.uppercaseChar() else c.lowercaseChar() }.joinToString("")
        "Reverse Text" -> input.reversed()
        "Dot Style" -> input.toList().joinToString(".")
        else -> input
    }

    private fun commit(text: String) { currentInputConnection?.commitText(text, 1) }

    private fun makeButton(label: String, size: Float, action: () -> Unit): Button = Button(this).apply {
        text = label; textSize = size; isAllCaps = false; setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        background = GradientDrawable().apply { setColor(Color.rgb(55, 55, 65)); cornerRadius = dp(7).toFloat(); setStroke(dp(1), Color.rgb(85, 85, 98)) }
        setPadding(dp(2), 0, dp(2), 0)
        setOnClickListener { action() }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
