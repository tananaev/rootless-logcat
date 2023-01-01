/*
 * Copyright 2016 - 2022 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tananaev.logcat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.tananaev.logcat.LineAdapter.*
import com.tananaev.logcat.StringUtils.containsIgnoreCase
import com.tananaev.logcat.StringUtils.indexOfIgnoreCase
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LineAdapter : RecyclerView.Adapter<LineViewHolder>() {

    private val linesAll: MutableList<Line> = LinkedList()
    private var linesFiltered: MutableList<Line> = LinkedList()
    var tag: String? = null
        private set
    var keyword: String? = null
        private set
    var searchWord: String? = null
        private set

    class LineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView

        init {
            textView = itemView.findViewById<View>(android.R.id.text1) as TextView
        }
    }

    val lines: List<Line>
        get() = linesFiltered

    fun clear() {
        linesAll.clear()
        linesFiltered.clear()
        notifyDataSetChanged()
    }

    fun addItems(lines: List<String?>) {
        val linesAll: MutableList<Line> = LinkedList()
        for (line in lines) {
            linesAll.add(Line(line!!))
        }
        this.linesAll.addAll(linesAll)
        val linesFiltered: List<Line> = filter(linesAll)
        this.linesFiltered.addAll(linesFiltered)
        notifyItemRangeInserted(this.linesFiltered.size - linesFiltered.size, linesFiltered.size)
    }

    private fun filter(lines: List<Line>): MutableList<Line> {
        val linesFiltered: MutableList<Line> = LinkedList()
        val hasKeyword = !TextUtils.isEmpty(keyword)
        val hasTag = !TextUtils.isEmpty(tag)
        if (hasKeyword || hasTag) {
            for (line in lines) {
                if (hasTag && !containsIgnoreCase(line.tag, tag)) {
                    continue
                }
                if (hasKeyword && !containsIgnoreCase(line.content, keyword)) {
                    continue
                }
                linesFiltered.add(line)
            }
        } else {
            linesFiltered.addAll(lines)
        }
        return linesFiltered
    }

    fun filter(tag: String?, keyword: String?) {
        this.tag = tag
        this.keyword = keyword
        linesFiltered = filter(linesAll)
        notifyDataSetChanged()
    }

    fun search(searchWord: String?) {
        this.searchWord = searchWord
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.line_list_item, parent, false)
        return LineViewHolder(view)
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        val item = linesFiltered[position]
        holder.itemView.tag = item
        holder.itemView.setOnLongClickListener(onItemLongClickListener)
        val content = item.content
        val context = holder.textView.context
        if (!TextUtils.isEmpty(keyword) || !TextUtils.isEmpty(searchWord)) {
            val spannableContent = SpannableString(content)
            if (!TextUtils.isEmpty(keyword)) {
                var index = 0
                var found: Int
                val filteredKeywordBackgroundColor =
                    context.resources.getColor(R.color.filtered_keyword_background)
                while (indexOfIgnoreCase(content, keyword, index).also { found = it } >= 0) {
                    spannableContent.setSpan(
                        BackgroundColorSpan(filteredKeywordBackgroundColor),
                        found,
                        found + keyword!!.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    index = found + keyword!!.length
                }
            }
            if (!TextUtils.isEmpty(searchWord)) {
                var index = 0
                var found: Int
                val searchWordBackgroundColor =
                    context.resources.getColor(R.color.search_word_background)
                while (indexOfIgnoreCase(content, searchWord, index).also { found = it } >= 0) {
                    spannableContent.setSpan(
                        BackgroundColorSpan(searchWordBackgroundColor),
                        found,
                        found + searchWord!!.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    index = found + searchWord!!.length
                }
            }
            holder.textView.text = spannableContent
        } else {
            holder.textView.text = content
        }
        holder.itemView.setBackgroundColor(context.resources.getColor(if (position % 2 == 0) R.color.row_bg_color_even else R.color.row_bg_color_odd))
        when (item.level) {
            'W' -> holder.textView.setTextColor(context.resources.getColor(R.color.colorWarning))
            'E', 'A' -> holder.textView.setTextColor(context.resources.getColor(R.color.colorError))
            else -> holder.textView.setTextColor(context.resources.getColor(R.color.colorNormal))
        }
    }

    override fun getItemCount(): Int {
        return linesFiltered.size
    }

    private val onItemLongClickListener = OnLongClickListener { v ->
        if (v.tag is Line) {
            val line = v.tag as Line
            val context = v.context
            val builder = AlertDialog.Builder(context)
            val menuItems = arrayOf(
                context.getString(R.string.menu_copy_text),
                context.getString(R.string.menu_pretty_json)
            )
            builder.setItems(menuItems) { dialog, which ->
                if (which == 0) {
                    setClipboardText(context, line.content)
                } else {
                    showPrettyJsonDialog(context, line.content)
                }
            }
            builder.show()
            return@OnLongClickListener true
        }
        false
    }

    private fun setClipboardText(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, R.string.message_done, Toast.LENGTH_SHORT).show()
    }

    private fun showPrettyJsonDialog(context: Context, text: String) {
        try {
            val index = text.indexOf("{")
            val jsonObject = JSONObject(text.substring(index))
            val jsonString = jsonObject.toString(2)
            val builder = AlertDialog.Builder(context)
            builder.setMessage(jsonString)
            builder.setNegativeButton(R.string.menu_copy_text) { dialog, which ->
                setClipboardText(
                    context,
                    jsonString
                )
            }
            builder.setPositiveButton(R.string.warning_close, null)
            builder.show()
        } catch (ex: IndexOutOfBoundsException) {
            Toast.makeText(context, R.string.message_not_json_string, Toast.LENGTH_SHORT).show()
        } catch (ex: JSONException) {
            Toast.makeText(context, R.string.message_not_json_string, Toast.LENGTH_SHORT).show()
        }
    }

}
