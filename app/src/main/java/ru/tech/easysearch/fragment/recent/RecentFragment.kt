package ru.tech.easysearch.fragment.recent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R

class RecentFragment : DialogFragment(R.layout.recent_fragment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_ESearch_Fullscreen)
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