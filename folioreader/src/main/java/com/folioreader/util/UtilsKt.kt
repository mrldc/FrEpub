package com.folioreader.util

/**
 * @author : litao
 * @date   : 2023/2/22 14:38
 */
val Float.dp: Float
    get() =  Utils.dpToPxF(this)


val Int.dp: Int
    get() =  Utils.dpToPx(this)