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
import com.folioreader.model.sqlite.BookmarkTable


class NoteDetailFragment(var bookId: String, var noteId: Int, var note: String, var content: String) : DialogFragment() {
    var tvBookmarkNote :TextView? = null
    var rlShowView :RelativeLayout? = null
    var rlEditView : LinearLayout? = null
    var tvEditNote :EditText? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.dialog_folio_bookmark, container, false)
        //展示笔记的布局
        rlShowView = rootView.findViewById(R.id.rl_bookmark_content)
        //编辑笔记的布局
        rlEditView = rootView.findViewById(R.id.edit_bookmark)
         tvEditNote = rootView.findViewById(R.id.et_page_note)
        tvEditNote!!.setText(note)
        var tvEditContent = rootView.findViewById<TextView>(R.id.tv_mark_content)
        tvEditContent.text = note
        var tvEditSave = rootView.findViewById<TextView>(R.id.tv_page_note_save)
        tvEditSave.setOnClickListener{
            //更新笔记
            if(tvEditNote!!.text != null){
                val newNote =  tvEditNote?.text.toString()
                val updateResult = BookmarkTable.updateNote(newNote,noteId,activity)
                if(updateResult){
                    Toast.makeText(activity,"修改失败",Toast.LENGTH_SHORT).show()
                    tvBookmarkNote!!.text = newNote
                    rlEditView!!.visibility = View.INVISIBLE
                    rlShowView!!.visibility = View.VISIBLE
                }else{
                    Toast.makeText(activity,"修改失败",Toast.LENGTH_SHORT).show()
                }
            }

        }
        var tvBookmarkContent = rootView.findViewById<TextView>(R.id.tv_bookmark_content)
        tvBookmarkContent!!.text = content
        tvBookmarkNote  = rootView.findViewById<TextView>(R.id.tv_page_note)
        tvBookmarkNote!!.text = note
        //删除按钮
        var tvDeleteNote = rootView.findViewById<TextView>(R.id.tv_bookmark_delete)
        tvDeleteNote.setOnClickListener{
           var deleteResult = BookmarkTable.deleteBookmarkById(noteId,activity)
            if(deleteResult){
                Toast.makeText(activity,"删除成功",Toast.LENGTH_SHORT).show()
                dismiss()
            }else{
                Toast.makeText(activity,"删除失败",Toast.LENGTH_SHORT).show()
            }
        }
        //编辑
        var tvEdit = rootView.findViewById<TextView>(R.id.tv_edit)
        tvEdit.setOnClickListener{
            rlShowView!!.visibility = View.INVISIBLE
            rlEditView!!.visibility = View.VISIBLE
        }
        var tvCancel = rootView.findViewById<TextView>(R.id.tv_cancel)
        tvCancel.setOnClickListener{
            dismiss()
        }
        return rootView
    }
}