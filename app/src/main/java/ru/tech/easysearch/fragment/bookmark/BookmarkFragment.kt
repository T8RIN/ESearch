package ru.tech.easysearch.fragment.bookmark

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R

class BookmarkFragment : DialogFragment(R.layout.bookmark_fragment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )
    }

}