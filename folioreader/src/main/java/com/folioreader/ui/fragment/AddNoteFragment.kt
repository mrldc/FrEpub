package com.folioreader.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
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


class AddNoteFragment(
    var bookId: String?, var noteId: Int?, var note: String?, var content: String,
    var noteType:String,
    var fragment: FolioPageFragment,var rangy:String?) : DialogFragment() {
    var tvBookmarkNote :TextView? = null
    var rlShowView :RelativeLayout? = null
    var tvEditNote :EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setStyle(STYLE_NO_TITLE, R.style.MatchWidthDialog)

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var window = dialog!!.window;
        window!!.setGravity(Gravity.BOTTOM)
        window!!.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT))
        /*window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);*/
        //dialog!!.setCanceledOnTouchOutside(true)
        val rootView: View = inflater.inflate(R.layout.add_note_fragment, container, false)

         tvEditNote = rootView.findViewById(R.id.et_page_note)

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
                        this.dismiss()
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
                        InputMethodUtils.close(tvEditNote)
                        dismiss()
                    }else{
                        Toast.makeText(activity,"保存失败",Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }

        return rootView
    }

    override fun onStart() {
        super.onStart()

        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay!!.getMetrics(dm)
        dialog?.window?.setLayout(dm.widthPixels * 1, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}