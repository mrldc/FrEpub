package com.folioreader.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.folioreader.model.MarkVo
import com.folioreader.model.sqlite.BookmarkTable
import com.folioreader.model.sqlite.HighLightTable
import com.folioreader.util.InputMethodUtils


class NoteDetailFragment(
    var bookId: String?, var noteId: Int?, var note: String?, var content: String,
    var noteType:String,
    var fragment: FolioPageFragment,var rangy:String?) : DialogFragment() {
    var tvBookmarkNote :TextView? = null
    var rlShowView :RelativeLayout? = null
    var rlEditView : LinearLayout? = null
    var tvEditNote :EditText? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog!!.window!!.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
        val rootView: View = inflater.inflate(R.layout.dialog_folio_bookmark, container, false)
        //展示笔记的布局
        rlShowView = rootView.findViewById(R.id.rl_bookmark_content)
        //编辑笔记的布局
        rlEditView = rootView.findViewById(R.id.edit_bookmark)
         tvEditNote = rootView.findViewById(R.id.et_page_note)
        tvEditNote!!.setText(note)
        var tvEditContent = rootView.findViewById<TextView>(R.id.tv_mark_content)
        tvEditContent.text = content
        var tvEditSave = rootView.findViewById<TextView>(R.id.tv_page_note_save)
        tvEditSave.setOnClickListener{
            //更新笔记
            if(tvEditNote!!.text != null){
                val newNote =  tvEditNote?.text.toString()
                //页笔记
                if(noteType == MarkVo.PageNoteType){
                    val updateResult = BookmarkTable.updateNote(newNote,noteId,activity)
                    if(updateResult){
                        Toast.makeText(activity,"保存成功",Toast.LENGTH_SHORT).show()
                        tvBookmarkNote!!.text = newNote
                        rlEditView!!.visibility = View.INVISIBLE
                        rlShowView!!.visibility = View.VISIBLE
                    }else{
                        Toast.makeText(activity,"保存失败",Toast.LENGTH_SHORT).show()
                    }
                }else if(noteType == MarkVo.HighlightMarkType){//段落笔记
                    val updateResult = if(noteId != null) {
                        //更新
                        var highlightImpl = HighlightImpl()
                        highlightImpl.id = noteId as Int
                        highlightImpl.note =newNote
                        HighLightTable.updateHighlight(highlightImpl)
                    }else{//插入
                        fragment.highlight(HighlightImpl.HighlightStyle.mark,newNote,false)
                    }
                    if(updateResult){
                        Toast.makeText(activity,"保存成功",Toast.LENGTH_SHORT).show()
                        tvBookmarkNote!!.text = newNote
                        rlEditView!!.visibility = View.INVISIBLE
                        rlShowView!!.visibility = View.VISIBLE
                        InputMethodUtils.close(tvEditNote)
                        dismiss()
                    }else{
                        Toast.makeText(activity,"保存失败",Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }
        var tvBookmarkContent = rootView.findViewById<TextView>(R.id.tv_bookmark_content)
        tvBookmarkContent!!.text = content
        tvBookmarkNote  = rootView.findViewById<TextView>(R.id.tv_page_note)
        tvBookmarkNote!!.text = note
        //删除按钮
        var tvDeleteNote = rootView.findViewById<TextView>(R.id.tv_bookmark_delete)
        if(noteId == null){
            tvDeleteNote.visibility=View.GONE
        }
        tvDeleteNote.setOnClickListener{
           var deleteResult = HighLightTable.deleteHighlight(noteId!!)
            if(deleteResult){
                Toast.makeText(activity,"删除成功",Toast.LENGTH_SHORT).show()
                dismiss()
                //清除标记
                fragment.unhighlightSelection(rangy)
            }else{
                Toast.makeText(activity,"删除失败",Toast.LENGTH_SHORT).show()
            }
        }
        //编辑
        var tvEdit = rootView.findViewById<TextView>(R.id.tv_edit)
        if(noteId == null){
            tvEdit.visibility=View.GONE
        }
        tvEdit.setOnClickListener{
            rlShowView!!.visibility = View.GONE
            rlEditView!!.visibility = View.VISIBLE
        }
        var tvCancel = rootView.findViewById<TextView>(R.id.tv_cancel)
        tvCancel.setOnClickListener{
            dismiss()
        }
        if(noteId == null){
            rlShowView!!.visibility = View.GONE
            rlEditView!!.visibility = View.VISIBLE
        }
        return rootView
    }
}