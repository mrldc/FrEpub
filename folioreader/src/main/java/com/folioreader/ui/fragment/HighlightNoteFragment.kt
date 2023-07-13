package com.folioreader.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.folioreader.R
import com.folioreader.model.HighlightImpl
import com.folioreader.model.sqlite.BookmarkTable
import com.folioreader.model.sqlite.HighLightTable


class HighlightNoteFragment(var bookId: String, var highlightId: Int?, var note: String, var content: String,var pageNumber :Int,var href:String) : DialogFragment() {
    var tvBookmarkNote :TextView? = null

    var tvEditNote :EditText? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.include_folio_comment, container, false)

         tvEditNote = rootView.findViewById(R.id.et_page_note)
        tvEditNote!!.setText(note)
        var tvEditContent = rootView.findViewById<TextView>(R.id.tv_mark_content)
        tvEditContent.text = content
        var tvEditSave = rootView.findViewById<TextView>(R.id.tv_page_note_save)
        tvEditSave.setOnClickListener{
            //更新笔记
            if(tvEditNote!!.text != null){
                val newNote =  tvEditNote?.text.toString()
                val updateResult =if(highlightId != null) {
                    //更新
                    var highlightImpl = HighlightImpl()
                    highlightImpl.id = highlightId as Int
                    highlightImpl.note =newNote
                   HighLightTable.updateHighlight(highlightImpl)
                }else{//插入
                    var highlightImpl = HighlightImpl()
                    highlightImpl.bookId = bookId
                    highlightImpl.note = newNote
                    highlightImpl.type = HighLightTable.MARK_TYPE
                    highlightImpl.content = content
                    highlightImpl.pageNumber = pageNumber
                    HighLightTable.insertHighlight(highlightImpl) > 0
                }
                if(updateResult){
                    Toast.makeText(activity,"保存成功",Toast.LENGTH_SHORT).show()
                    tvBookmarkNote!!.text = newNote

                }else{
                    Toast.makeText(activity,"保存失败",Toast.LENGTH_SHORT).show()
                }
            }

        }


        return rootView
    }
}